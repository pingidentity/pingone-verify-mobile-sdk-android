package com.pingidentity.sample.P1VerifyApp.utils;


import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.google.firebase.messaging.FirebaseMessaging;
import com.pingidentity.did.sdk.idvalidation.IdvService;
import com.pingidentity.did.sdk.idvalidation.NotificationHandler;
import com.pingidentity.did.sdk.idvalidation.errors.IdvServiceInitializationError;
import com.pingidentity.did.sdk.idvalidation.utils.BackgroundThreadHandler;
import com.pingidentity.did.sdk.types.VerifyStatus;
import com.pingidentity.p1verifyidschema.IdCard;

import java.util.List;
import java.util.Objects;

@SuppressWarnings("unused")
public class IdvHelper {

    public static final String TAG = IdvHelper.class.getCanonicalName();

    // TODO: Must set this value
    // Refer to instruction here: https://apidocs.pingidentity.com/pingone/platform/v1/api/#installation-and-configuration-1
    private static final String APPLICATION_ID = "";

    private static IdvHelper shared;

    private IdvHelper() {
    }

    public static IdvHelper getInstance() {
        if (shared == null) {
            shared = new IdvHelper();
        }

        return shared;
    }

    private final MutableLiveData<IdvService> idvService = new MutableLiveData<>();
    private NotificationHandler handler;

    private static final BackgroundThreadHandler backgroundThreadHandler = BackgroundThreadHandler.singleBackgroundThreadHandler();

    public void setNotificationHandler(@NonNull final NotificationHandler handler) {
        this.handler = Objects.requireNonNull(handler);
    }

    public void updateLifeCycleOwner(@NonNull final LifecycleOwner owner) {

        P1FirebaseMessagingService.getNotificationData().observe(owner, notificationData -> {
            if (notificationData == null) {
                return;
            }

            P1FirebaseMessagingService.clearNotificationData();
            addOneTimeIdvServiceObserver(service -> service.processNotification(notificationData, Objects.requireNonNull(handler)));
        });

    }

    private void initIdvServiceWithVerificationCode(@NonNull final FragmentActivity activity, @NonNull final String verificationCode, @NonNull final Consumer<Throwable> errorHandler) {
        final IdvService.Builder builder = IdvService.Builder.initWithNewConfigFor(verificationCode);
        initIdvServiceUsingBuilder(activity, builder, errorHandler);
    }

    private void initIdvServiceWithQrUrl(@NonNull final FragmentActivity activity, @NonNull final String qrUrl, @NonNull final Consumer<Throwable> errorHandler) {
        final IdvService.Builder builder = IdvService.Builder.initWithNewConfigFromQr(qrUrl);
        initIdvServiceUsingBuilder(activity, builder, errorHandler);
    }

    private void initIdvServiceWithSavedState(@NonNull final FragmentActivity activity, @NonNull final Consumer<Throwable> errorHandler) {
        final IdvService.Builder builder = IdvService.Builder.initWithSavedConfig();
        initIdvServiceUsingBuilder(activity, builder, errorHandler);
    }

    private void initIdvServiceUsingBuilder(@NonNull final FragmentActivity activity, @NonNull final IdvService.Builder builder, @NonNull final Consumer<Throwable> errorHandler) {
        Objects.requireNonNull(activity);
        Objects.requireNonNull(builder);
        Objects.requireNonNull(errorHandler);
        backgroundThreadHandler.post(() -> getPushToken(pushToken -> builder.setPushNotificationToken(pushToken)
                .setApplicationId(APPLICATION_ID)
                .create(activity, IdvHelper.this.idvService::postValue, errorHandler)));
    }

    private static void getPushToken(@Nullable Consumer<String> resultConsumer) {
        final String pushToken = P1FirebaseMessagingService.getPushToken().getValue();
        if (pushToken == null) {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Log.e(TAG, "Failed to fetch current token", task.getException());
                    return;
                }
                final String token = task.getResult();
                if (token != null) {
                    P1FirebaseMessagingService.updatePushToken(token);
                }
                Log.d(TAG, "Push Token retrieved: " + token);
                if (resultConsumer != null) {
                    resultConsumer.accept(token);
                }
            });
        } else {
            if (resultConsumer != null) {
                resultConsumer.accept(pushToken);
            }
        }
    }

    public static class DataVerificationRequest {

        final FragmentActivity activity;
        final List<IdCard> cards;
        final String verificationCode;
        final String qrUrl;
        final Consumer<VerifyStatus> resultConsumer;
        final Consumer<Throwable> errorConsumer;

        public DataVerificationRequest(@NonNull final FragmentActivity activity,
                                       @NonNull final List<IdCard> cards,
                                       final String verificationCode,
                                       final String qrUrl,
                                       @NonNull final Consumer<VerifyStatus> resultConsumer,
                                       @NonNull final Consumer<Throwable> errorConsumer) {
            if (verificationCode == null && qrUrl == null) {
                throw new IllegalArgumentException("Must pass one of verificationCode or qrUrl");
            }
            this.activity = Objects.requireNonNull(activity);
            this.cards = Objects.requireNonNull(cards);
            this.verificationCode = verificationCode;
            this.qrUrl = qrUrl;
            this.resultConsumer = Objects.requireNonNull(resultConsumer);
            this.errorConsumer = Objects.requireNonNull(errorConsumer);
        }
    }

    public void submitDataForVerification(@NonNull final DataVerificationRequest request) {
        BackgroundThreadHandler.postOnMainThread(() -> {
            idvService.setValue(null);
            addOneTimeIdvServiceObserver(idvService ->
                    idvService.submitDataForVerification(request.cards, request.resultConsumer, request.errorConsumer));

            if (request.verificationCode != null) {
                initIdvServiceWithVerificationCode(request.activity, request.verificationCode, request.errorConsumer);
            } else if (request.qrUrl != null) {
                initIdvServiceWithQrUrl(request.activity, request.qrUrl, request.errorConsumer);
            } else {
                throw new IllegalStateException("Invalid DataVerificationRequest - missing qrUrl & verificationCode");
            }
        });
    }

    public void checkVerificationStatus(@NonNull final FragmentActivity activity) {
        final Consumer<IdvService> serviceConsumer = idvService -> idvService.checkVerificationStatus(handler);
        addOneTimeIdvServiceObserver(serviceConsumer);

        if (this.idvService.getValue() == null) {
            initIdvServiceWithSavedState(activity, throwable -> handler.handleError(new IdvServiceInitializationError(throwable)));
        }
    }

    private void addOneTimeIdvServiceObserver(@NonNull final Consumer<IdvService> consumer) throws IdvServiceInitializationError {
        if (idvService == null) {
            throw new IdvServiceInitializationError("Must initialize IdvService by scanning a qr code or by using a verification code or saved state.");
        }

        BackgroundThreadHandler.postOnMainThread(() -> idvService.observeForever(new Observer<IdvService>() {
            @Override
            public void onChanged(IdvService service) {
                if (service == null) {
                    return;
                }

                idvService.removeObserver(this);
                consumer.accept(service);
            }
        }));
    }

}
