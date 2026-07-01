package com.pingidentity.sdk.pingoneverify.ui.providers;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.pingidentity.sdk.pingoneverify.PingOneVerifyClient;
import com.pingidentity.sdk.pingoneverify.errors.ClientBuilderError;
import com.pingidentity.sdk.pingoneverify.neo.contracts.CaptureResultReceiver;
import com.pingidentity.sdk.pingoneverify.neo.contracts.VerifyTransactionCoordinator;
import com.pingidentity.sdk.pingoneverify.neo.contracts.VerifyTransactionCoordinatorDelegate;
import com.pingidentity.sdk.pingoneverify.neo.errors.DocumentSubmissionError;
import com.pingidentity.sdk.pingoneverify.neo.settings.OtpCaptureSettings;
import com.pingidentity.sdk.pingoneverify.neo.errors.IdvError;
import com.pingidentity.sdk.pingoneverify.neo.models.AppThemeResponse;
import com.pingidentity.sdk.pingoneverify.neo.models.RetryFeedback;
import com.pingidentity.sdk.pingoneverify.neo.models.DocumentClass;
import com.pingidentity.sdk.pingoneverify.neo.models.DocumentSubmissionResponse;
import com.pingidentity.sdk.pingoneverify.neo.models.IdCaptureResult;
import com.pingidentity.sdk.pingoneverify.neo.models.SelfieCaptureResult;
import com.pingidentity.sdk.pingoneverify.neo.settings.DocumentCaptureSettings;
import com.pingidentity.sdk.pingoneverify.neo.settings.LocationCaptureSettings;
import com.pingidentity.sdk.provider.language.LanguagePackProviderContract;

import java.lang.ref.WeakReference;

/**
 * Built-in UI entry point — implements {@link VerifyTransactionCoordinatorDelegate}.
 * Owns Builder setup, holds a {@link DocumentCapturePresenter} for all fragment routing.
 * Mirrors iOS's {@code PingOneVerifyClientHelper}.
 *
 * Usage:
 *   PingOneVerifyHelper helper = new PingOneVerifyHelper(requireActivity(), verificationUrl);
 *   helper.start();
 */
public class PingOneVerifyHelper implements VerifyTransactionCoordinatorDelegate {

    private WeakReference<FragmentActivity> mActivityRef;
    private WeakReference<VerifyHelperCallback> mCallbackRef;
    private final DocumentCapturePresenter documentCapturePresenter = new DocumentCapturePresenter();
    private PingOneVerifyClient mClient;

    /**
     * Constructs the helper and begins async initialisation (URL fetch, language pack, app theme).
     * Call {@link #start()} once {@code onSuccess} has fired and the activity UI is ready.
     */
    public PingOneVerifyHelper(FragmentActivity activity, String verificationUrl, VerifyHelperCallback callback) {
        mActivityRef = new WeakReference<>(activity);
        mCallbackRef = new WeakReference<>(callback);
        documentCapturePresenter.attach(activity);
        new PingOneVerifyClient.Builder(verificationUrl, this)
                .setContext(activity)
                .build(new PingOneVerifyClient.Builder.BuilderCallback() {
                    @Override
                    public void onSuccess(PingOneVerifyClient client) {
                        mClient = client;
                        // Auto-start: show first capture screen as soon as client is ready
                        FragmentActivity a = mActivityRef.get();
                        if (a != null) {
                            initNavigation(a);
                            mClient.start();
                        }
                    }

                    @Override
                    public void onError(ClientBuilderError error) {
                        didFailWith(null, new DocumentSubmissionError.DocumentCaptureError(
                                "builder_error", error.getLocalizedMessage()));
                    }
                });
    }

    /**
     * Shows the first capture screen. Call after the {@code build} callback delivers the client.
     */
    public void start() {
        if (mClient == null) return;
        FragmentActivity activity = mActivityRef.get();
        if (activity == null) return;
        initNavigation(activity);
        mClient.start();
    }

    private void runOnMainThread(Runnable action) {
        FragmentActivity activity = mActivityRef.get();
        if (activity != null) {
            activity.runOnUiThread(action);
        } else {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(action);
        }
    }

        // --- Navigation lifecycle (kept for callers that manage navigation manually) ---

    public void initNavigation(FragmentActivity activity) {
        mActivityRef = new WeakReference<>(activity);
        documentCapturePresenter.attach(activity);
    }

    public void closeNavigation() {
        documentCapturePresenter.closeNavigation();
        mActivityRef.clear();
    }

    // --- VerifyTransactionCoordinatorDelegate — thin dispatchers to presenter ---

    @Override
    public void didReceiveAppTheme(VerifyTransactionCoordinator coordinator,
                                   @Nullable AppThemeResponse appTheme,
                                   @Nullable IdvError error) {
        runOnMainThread(() -> documentCapturePresenter.setAppTheme(appTheme));
    }

    @Override
    public void didReceiveLanguagePack(VerifyTransactionCoordinator coordinator,
                                       @Nullable LanguagePackProviderContract languageProvider,
                                       @Nullable IdvError error) {
        runOnMainThread(() -> documentCapturePresenter.setLanguageProvider(languageProvider));
    }

    @Override
    public void didCaptureSelfie(VerifyTransactionCoordinator coordinator,
                                 SelfieCaptureResult result) {
        runOnMainThread(() -> documentCapturePresenter.showSelfiePreview(coordinator, result));
    }

    @Override
    public void didCaptureGovernmentId(VerifyTransactionCoordinator coordinator,
                                        IdCaptureResult result) {
        runOnMainThread(() -> documentCapturePresenter.showIdPreview(coordinator, result));
    }

    @Override
    public void didCaptureGeolocation(VerifyTransactionCoordinator coordinator,
                                       float latitude, float longitude) {
        runOnMainThread(() -> {
            documentCapturePresenter.showWaitOverlay(coordinator);
            coordinator.submitGeolocation(latitude, longitude);
        });
    }

    @Override
    public void didSubmitDocument(VerifyTransactionCoordinator coordinator,
                                  DocumentSubmissionResponse response) {
        // Overlay shown by presenter callbacks before submit; hidden by should* handlers.
    }

    @Override
    public void shouldCaptureDocument(VerifyTransactionCoordinator coordinator,
                                      DocumentCaptureSettings settings) {
        runOnMainThread(() -> {
            documentCapturePresenter.hideWaitOverlay();
            switch (settings.getDocumentType()) {
                case GEOLOCATION -> documentCapturePresenter.captureLocation(coordinator, (LocationCaptureSettings) settings);
                case OTP -> documentCapturePresenter.captureOtp(coordinator, (OtpCaptureSettings) settings);
                case EMAIL, PHONE, SELFIE, GOVERNMENT_ID -> documentCapturePresenter.captureDocument(coordinator, settings);
                default -> { if (coordinator instanceof CaptureResultReceiver) {
                    ((CaptureResultReceiver) coordinator).captureError(
                        new DocumentSubmissionError.DocumentCaptureError("unsupported_type",
                                "Unsupported document type: " + settings.getDocumentType())); } }
            }
        });
    }

    @Override
    public void shouldRetryCapture(VerifyTransactionCoordinator coordinator,
                                   RetryFeedback message,
                                   DocumentCaptureSettings settings) {
        runOnMainThread(() -> {
            if (settings != null && settings.getDocumentType() == DocumentClass.GEOLOCATION) {
                documentCapturePresenter.showGeolocationRetry(coordinator);
                return;
            }
            documentCapturePresenter.hideWaitOverlay();
            documentCapturePresenter.onShowUploadRetry(
                coordinator,
                message,
                settings,
                onRetryDecision -> {
                    if (onRetryDecision) documentCapturePresenter.startCapture(coordinator, settings);
                    else coordinator.endVerification();
                }
        );
        });
    }

    @Override
    public void didSubmitOtp(VerifyTransactionCoordinator coordinator, boolean success) {
        runOnMainThread(() -> documentCapturePresenter.notifyOtpResult(success));
    }

    @Override
    public void didUpdateOtpSession(VerifyTransactionCoordinator coordinator,
                             DocumentCaptureSettings settings) {
        runOnMainThread(() -> documentCapturePresenter.notifyOtpSettingsUpdated(settings));
    }

    @Override
    public void didCompleteSubmission(VerifyTransactionCoordinator coordinator) {
        FragmentActivity activity = mActivityRef.get();
        VerifyHelperCallback callback = mCallbackRef != null ? mCallbackRef.get() : null;
        if (activity == null) return;
        activity.runOnUiThread(() -> {
            closeNavigation();
            if (callback != null) callback.onVerificationCompleted();
        });
    }

    @Override
    public void didFailWith(@Nullable VerifyTransactionCoordinator coordinator, DocumentSubmissionError error) {
        FragmentActivity activity = mActivityRef.get();
        VerifyHelperCallback callback = mCallbackRef != null ? mCallbackRef.get() : null;
        if (activity == null) return;
        activity.runOnUiThread(() -> {
            closeNavigation();
            if (callback != null) callback.onVerificationFailed(error);
        });
    }
}
