package com.pingidentity.sample.P1VerifyApp.fragments.wizard;


import androidx.fragment.app.FragmentActivity;

import com.pingidentity.sample.P1VerifyApp.models.WizardItem;

import java.util.List;

public interface WizardContract {

    interface View {

        void showWizard(List<WizardItem> wizardData);

        void nextStep();

        void showAlert(int title, int message);

        void finishWizard();
    }

    interface Presenter {

        void getWizardData();

        void onDestroy();

        void captureSelfie(FragmentActivity activity);

        void captureLicense(FragmentActivity activity);

        void capturePassport(FragmentActivity activity);

        void onSkip();

    }

}
