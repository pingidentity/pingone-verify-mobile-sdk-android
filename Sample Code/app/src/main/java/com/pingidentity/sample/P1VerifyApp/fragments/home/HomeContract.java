package com.pingidentity.sample.P1VerifyApp.fragments.home;

import androidx.fragment.app.FragmentActivity;

import com.pingidentity.sample.P1VerifyApp.models.CardItem;

import java.util.List;

public interface HomeContract {

    interface View {

        void showDocuments(List<CardItem> data);

        void showValidationSuccess();

        void showValidationError();

        void showValidationInProgress();

        void hideValidation();

        void navigateToQR();

        void showAlert(int title, int message);

        void showAlert(String message);

    }

    interface Presenter {

        void initScreen();

        void onQRScanClicked();

        void checkValidation(FragmentActivity activity);

        void validateByCode(FragmentActivity activity, String code);

        void validateByUrl(FragmentActivity activity, String code);

        void updateSelfie(FragmentActivity activity);

        void updatePassport(FragmentActivity activity);

        void updateLicense(FragmentActivity activity);

        void dismissValidation();

        void onDestroy();

    }

}
