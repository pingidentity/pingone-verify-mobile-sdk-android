package com.pingidentity.sample.P1VerifyApp.utils;


import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.pingidentity.p1verifyidschema.DriverLicense;
import com.pingidentity.p1verifyidschema.Passport;
import com.pingidentity.p1verifyidschema.Selfie;
import com.pingidentity.sample.P1VerifyApp.callbacks.DocumentCaptureListener;
import com.pingidentity.sample.P1VerifyApp.di.Injector;
import com.pingidentity.sample.P1VerifyApp.storage.CardRepository;
import com.shocard.sholib.idcapture.driverlicense.DriverLicenseScannerActivity;
import com.shocard.sholib.idcapture.driverlicense.DriverLicenseScannerDialogFragment;
import com.shocard.sholib.idcapture.listeners.DriverLicenseScannerListener;
import com.shocard.sholib.livefaceverification.LiveFaceVerificationActivity;
import com.shocard.sholib.livefaceverification.LiveFaceVerificationDialogFragment;
import com.shocard.sholib.livefaceverification.listeners.LiveFaceVerificationListener;
import com.shocard.sholib.livefaceverification.livefaceverifier.SCLiveFaceVerificationAccuracy;
import com.shocard.sholib.livefaceverification.livefaceverifier.SCLiveFaceVerificationStep;
import com.shocard.sholib.passport_capture.listeners.PassportCaptureListener;
import com.shocard.sholib.passport_capture.passportcapture.PassportCaptureActivity;
import com.shocard.sholib.passport_capture.passportcapture.PassportCaptureDialogFragment;

import javax.inject.Inject;

public class CaptureUtil implements LiveFaceVerificationListener, DriverLicenseScannerListener, PassportCaptureListener {

    @Inject
    CardRepository mRepository;

    private DocumentCaptureListener mCaptureListener;

    public CaptureUtil() {
        Injector.getAppComponent().inject(this);
    }

    //////////////////////////////////////
    ///////////// Capture selfie /////////
    //////////////////////////////////////

    public void startSelfieDialog(FragmentActivity activity, DocumentCaptureListener documentCaptureListener) {
        this.mCaptureListener = documentCaptureListener;
        Bundle bundle = new LiveFaceVerificationActivity.LFVBundle.Builder()
                .setAccuracy(SCLiveFaceVerificationAccuracy.medium)
                .setVerificationSteps(SCLiveFaceVerificationStep.lfv_smile, SCLiveFaceVerificationStep.lfv_straight_face)
                .setVerificationTimeInMillis(2000)
                .create();
        LiveFaceVerificationDialogFragment.start(activity, bundle, this);
    }

    @Override
    public void onComplete(@NonNull Bundle bundle) {
        final Selfie selfie = LiveFaceVerificationActivity.LFVBundle.getResultSelfie(bundle);
        selfie.setSelfie(ImageUtil.resize(selfie.getSelfie(), 1024));
        if (mRepository.getIdCard() != null) {
            selfie.setCardId(mRepository.getIdCard().getCardId());
        }
        mRepository.saveIDCard(selfie);
        mCaptureListener.onComplete();
    }

    @Override
    public void onCancelled() {
        mCaptureListener.onCancel();
    }

    //////////////////////////////////////
    ///////////// Capture license ////////
    //////////////////////////////////////

    public void startLicenseDialog(FragmentActivity activity, DocumentCaptureListener documentCaptureListener) {
        this.mCaptureListener = documentCaptureListener;
        Bundle dlsBundle = new DriverLicenseScannerActivity.DLSBundle.Builder()
                .setCheckHasFace(true)
                .create();
        DriverLicenseScannerDialogFragment.start(activity, dlsBundle, this);
    }

    @Override
    public void onDriverLicenseCaptured(Bundle bundle) {
        final DriverLicense driverLicense = DriverLicenseScannerActivity.DLSBundle.getDriverLicense(bundle);
        final Bitmap frontImage = driverLicense.getFrontImage();
        final Bitmap backImage = driverLicense.getBackImage();
        if (frontImage != null) {
            final int biggestSide = Math.max(frontImage.getHeight(), frontImage.getWidth());
            final float frontImageScaleFactor = (float) biggestSide / 1024f;
            driverLicense.setFrontImage(ImageUtil.resize(frontImage, frontImageScaleFactor), ImageUtil.translateRect(driverLicense.getFaceCoordinates(), frontImageScaleFactor, 10));
        }
        if (backImage != null) {
            driverLicense.setBackImage(ImageUtil.resize(backImage, 1024));
        }

        if (mRepository.getDriverLicense() != null) {
            driverLicense.setCardId(mRepository.getDriverLicense().getCardId());
        }

        mRepository.saveDriverLicense(driverLicense);
        mCaptureListener.onComplete();
    }

    @Override
    public void onCanceled() {
        mCaptureListener.onCancel();
    }

    //////////////////////////////////////
    ///////////// Capture passport ///////
    //////////////////////////////////////

    public void startPassportDialog(FragmentActivity activity, DocumentCaptureListener documentCaptureListener) {
        this.mCaptureListener = documentCaptureListener;
        Bundle dlsBundle = new PassportCaptureActivity.PCBundle.Builder()
                .setCheckHasFace(true)
                .create();
        PassportCaptureDialogFragment.start(activity, dlsBundle, this);
    }

    @Override
    public void onPassportCaptured(Bundle bundle) {
        final Passport passport = PassportCaptureActivity.PCBundle.getPassport(bundle);
        final Bitmap frontImage = passport.getFrontImage();
        if (frontImage != null) {
            final int biggestSide = Math.max(frontImage.getHeight(), frontImage.getWidth());
            final float frontImageScaleFactor = (float) biggestSide / 1024f;
            passport.setFrontImage(ImageUtil.resize(frontImage, frontImageScaleFactor), ImageUtil.translateRect(passport.getFaceCoordinates(), frontImageScaleFactor, 10));
        }

        if (mRepository.getPassport() != null) {
            passport.setCardId(mRepository.getPassport().getCardId());
        }

        mRepository.savePassport(passport);
        mCaptureListener.onComplete();
    }

}
