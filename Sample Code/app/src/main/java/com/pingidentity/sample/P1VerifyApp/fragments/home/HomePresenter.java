package com.pingidentity.sample.P1VerifyApp.fragments.home;


import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.pingidentity.did.sdk.idvalidation.NotificationHandler;
import com.pingidentity.did.sdk.idvalidation.UserData;
import com.pingidentity.did.sdk.idvalidation.VerificationResult;
import com.pingidentity.did.sdk.idvalidation.errors.IdvError;
import com.pingidentity.did.sdk.idvalidation.utils.BackgroundThreadHandler;
import com.pingidentity.did.sdk.types.ProviderMessages;
import com.pingidentity.p1verifyidschema.DriverLicense;
import com.pingidentity.p1verifyidschema.IdCard;
import com.pingidentity.p1verifyidschema.Passport;
import com.pingidentity.sample.P1VerifyApp.R;
import com.pingidentity.sample.P1VerifyApp.callbacks.DocumentCaptureListener;
import com.pingidentity.sample.P1VerifyApp.di.Injector;
import com.pingidentity.sample.P1VerifyApp.models.CardItem;
import com.pingidentity.sample.P1VerifyApp.models.DocumentType;
import com.pingidentity.sample.P1VerifyApp.storage.CardRepository;
import com.pingidentity.sample.P1VerifyApp.utils.CaptureUtil;
import com.pingidentity.sample.P1VerifyApp.utils.IdvHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

public class HomePresenter implements HomeContract.Presenter, NotificationHandler {

    @Inject
    CardRepository mRepository;

    private HomeContract.View mView;

    public HomePresenter(HomeContract.View view) {
        Injector.getAppComponent().inject(this);
        this.mView = view;
        IdvHelper.getInstance().setNotificationHandler(this);
    }

    @Override
    public void initScreen() {
        List<CardItem> data = new ArrayList<>();
        if (mRepository.getIdCard() != null)
            data.add(new CardItem(DocumentType.SELFIE, mRepository.getIdCard().getSelfie()));
        if (mRepository.getDriverLicense() != null)
            data.add(new CardItem(DocumentType.LICENSE, mRepository.getDriverLicense().getFrontImage()));
        if (mRepository.getPassport() != null)
            data.add(new CardItem(DocumentType.PASSPORT, mRepository.getPassport().getFrontImage()));
        if (mRepository.getValidationStatus())
            mView.showValidationSuccess();
        mView.showDocuments(data);
    }

    @Override
    public void onQRScanClicked() {
        if (checkDocumentsCompletion()) {
            mView.navigateToQR();
        }
    }

    @Override
    public void onDestroy() {
        mView = null;
    }

    ////////////////////////////////////////////
    ///////////// Validation work //////////////
    ////////////////////////////////////////////

    @Override
    public void checkValidation(FragmentActivity activity) {
        if (!checkDocumentsCompletion()) return;
        IdvHelper.getInstance().checkVerificationStatus(activity);
    }

    @Override
    public void validateByCode(FragmentActivity activity, String code) {
        confirmValidation(activity, code, null);
    }

    @Override
    public void validateByUrl(FragmentActivity activity, String url) {
        confirmValidation(activity, null, url);
    }

    @Override
    public void dismissValidation() {
        mRepository.setValidationStatus(false);
        mView.hideValidation();
    }

    private void confirmValidation(FragmentActivity activity, String code, String url) {
        if (!checkDocumentsCompletion()) return;
        final IdvHelper.DataVerificationRequest dataVerificationRequest
                = new IdvHelper.DataVerificationRequest(activity, getCards(), code, url,
                verifyStatus -> BackgroundThreadHandler.postOnMainThread(() -> {
                    mRepository.setValidationStatus(true);
                    switch (verifyStatus) {
                        case SUCCESS:
                            mRepository.setValidationStatus(true);
                            mView.showValidationSuccess();
                            break;
                        case FAIL:
                            mRepository.setValidationStatus(false);
                            mView.showValidationError();
                            break;
                        case IN_PROGRESS:
                            mRepository.setValidationStatus(false);
                            mView.showValidationInProgress();
                            break;
                    }
                }),
                throwable -> BackgroundThreadHandler.postOnMainThread(() -> mView.showValidationError()));

        IdvHelper.getInstance().submitDataForVerification(dataVerificationRequest);
    }

    private List<IdCard> getCards() {
        List<IdCard> cards = new ArrayList<>();
        if (mRepository.getDriverLicense() != null) {
            cards.add(mRepository.getDriverLicense());
        } else if (mRepository.getPassport() != null) {
            cards.add(mRepository.getPassport());
        }
        if (mRepository.getIdCard() != null) {
            cards.add(mRepository.getIdCard());
        }
        return cards;
    }

    ////////////////////////////////////////////
    /////////// NotificationHandler ////////////
    ////////////////////////////////////////////

    @Override
    public void handleResult(@NonNull VerificationResult verificationResult) {
        switch (verificationResult.getValidationStatus()) {
            case SUCCESS:
                mRepository.setValidationStatus(true);
                mView.showValidationSuccess();
                break;
            case FAIL:
                mRepository.setValidationStatus(false);
                mView.showValidationError();
                break;
            case IN_PROGRESS:
                mRepository.setValidationStatus(false);
                mView.showValidationInProgress();
                break;
        }

        final List<ProviderMessages> providerMessages = verificationResult.getProviderMessagesList();
        if (providerMessages != null && !providerMessages.isEmpty()) {
            providerMessages.forEach(messages -> {
                Log.e("HomePresenter", "Validation Errors: " + messages.toString());
            });
        }

        final UserData userData = verificationResult.getUserData();
        if (userData == null || userData.isEmpty()) {
            return;
        }
        updateIdCardFrom(userData);
    }

    @Override
    public void handleError(@NonNull IdvError idvError) {
        Log.e("TAG", "Error checking status: ", idvError);
        BackgroundThreadHandler.postOnMainThread(() -> mView.showError(null, idvError.getMessage()));
    }

    private void updateDriverLicenseFrom(@NonNull final UserData userData) {
        final DriverLicense driverLicense = mRepository.getDriverLicense();
        if (driverLicense == null) {
            return;
        }

        driverLicense.setFirstName(userData.getFirstName().isEmpty() ? driverLicense.getFirstName() : userData.getFirstName());
        driverLicense.setLastName(userData.getLastName().isEmpty() ? driverLicense.getLastName() : userData.getLastName());
        driverLicense.setBirthDate(userData.getBirthDate().isEmpty() ? driverLicense.getBirthDate() : userData.getBirthDate());
        driverLicense.setAddressStreet(userData.getAddressStreet().isEmpty() ? driverLicense.getAddressStreet() : userData.getAddressStreet());
        driverLicense.setAddressCity(userData.getAddressCity().isEmpty() ? driverLicense.getAddressCity() : userData.getAddressCity());
        driverLicense.setAddressState(userData.getAddressState().isEmpty() ? driverLicense.getAddressState() : userData.getAddressState());
        driverLicense.setAddressZip(userData.getAddressZip().isEmpty() ? driverLicense.getAddressZip() : userData.getAddressZip());
        driverLicense.setCountry(userData.getCountry().isEmpty() ? driverLicense.getCountry() : userData.getCountry());
        driverLicense.setIdNumber(userData.getIdNumber().isEmpty() ? driverLicense.getIdNumber() : userData.getIdNumber());
        driverLicense.setExpirationDate(userData.getExpirationDate().isEmpty() ? driverLicense.getExpirationDate() : userData.getExpirationDate());
        driverLicense.setIssueDate(userData.getIssueDate().isEmpty() ? driverLicense.getIssueDate() : userData.getIssueDate());

        mRepository.saveDriverLicense(driverLicense);
    }

    private void updatePassportFrom(@NonNull final UserData userData) {
        final Passport passport = mRepository.getPassport();
        if (passport == null) {
            return;
        }

        passport.setFirstName(userData.getFirstName().isEmpty() ? passport.getFirstName() : userData.getFirstName());
        passport.setLastName(userData.getLastName().isEmpty() ? passport.getLastName() : userData.getLastName());
        passport.setBirthDate(userData.getBirthDate().isEmpty() ? passport.getBirthDate() : userData.getBirthDate());
        passport.setCountry(userData.getCountry().isEmpty() ? passport.getCountry() : userData.getCountry());
        passport.setIdNumber(userData.getIdNumber().isEmpty() ? passport.getIdNumber() : userData.getIdNumber());
        passport.setExpirationDate(userData.getExpirationDate().isEmpty() ? passport.getExpirationDate() : userData.getExpirationDate());

        mRepository.savePassport(passport);
    }

    private void updateIdCardFrom(@NonNull final UserData userData) {
        Objects.requireNonNull(userData);

        switch (userData.getCardType()) {
            case DriverLicense.CARD_TYPE_DL:
                updateDriverLicenseFrom(userData);
                break;
            case Passport.CARD_TYPE_PASSPORT:
                updatePassportFrom(userData);
                break;
        }
    }

    ////////////////////////////////////////////
    ///////////// Update documents /////////////
    ////////////////////////////////////////////

    @Override
    public void updateSelfie(FragmentActivity activity) {
        new CaptureUtil().startSelfieDialog(activity, new DocumentCaptureListener() {
            @Override
            public void onComplete() {
                dismissValidation();
                initScreen();
            }

            @Override
            public void onCancel() {

            }
        });
    }

    @Override
    public void updateLicense(FragmentActivity activity) {
        new CaptureUtil().startLicenseDialog(activity, new DocumentCaptureListener() {
            @Override
            public void onComplete() {
                dismissValidation();
                initScreen();
            }

            @Override
            public void onCancel() {

            }
        });
    }

    @Override
    public void updatePassport(FragmentActivity activity) {
        new CaptureUtil().startPassportDialog(activity, new DocumentCaptureListener() {
            @Override
            public void onComplete() {
                dismissValidation();
                initScreen();
            }

            @Override
            public void onCancel() {

            }
        });
    }

    private boolean checkDocumentsCompletion() {
        if (mRepository.getIdCard() != null && (mRepository.getDriverLicense() != null || mRepository.getPassport() != null)) {
            return true;
        } else {
            mView.showError(R.string.insufficient_info, R.string.insufficient_info_desc);
        }
        return false;
    }

}
