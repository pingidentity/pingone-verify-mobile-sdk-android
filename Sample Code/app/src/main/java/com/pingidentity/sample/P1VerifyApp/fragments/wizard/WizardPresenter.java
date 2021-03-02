package com.pingidentity.sample.P1VerifyApp.fragments.wizard;

import android.content.Context;

import androidx.fragment.app.FragmentActivity;

import com.pingidentity.sample.P1VerifyApp.R;
import com.pingidentity.sample.P1VerifyApp.callbacks.DocumentCaptureListener;
import com.pingidentity.sample.P1VerifyApp.di.Injector;
import com.pingidentity.sample.P1VerifyApp.models.WizardItem;
import com.pingidentity.sample.P1VerifyApp.models.DocumentType;
import com.pingidentity.sample.P1VerifyApp.storage.CardRepository;
import com.pingidentity.sample.P1VerifyApp.utils.CaptureUtil;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class WizardPresenter implements WizardContract.Presenter {

    @Inject
    Context mContext;
    @Inject
    CardRepository mRepository;

    private WizardContract.View mView;

    private final List<DocumentType> mCompletedSteps;
    private List<WizardItem> items;

    public WizardPresenter(WizardContract.View view) {
        Injector.getAppComponent().inject(this);
        this.mView = view;
        this.mCompletedSteps = new ArrayList<>();
        items = new ArrayList<>();
    }

    @Override
    public void getWizardData() {
        items.add(new WizardItem(
                DocumentType.SELFIE,
                mContext.getString(R.string.wizard_selfie_title),
                mContext.getString(R.string.wizard_selfie_descriptions),
                mContext.getString(R.string.wizard_selfie_action),
                mContext.getString(R.string.wizard_info_title),
                mContext.getString(R.string.wizard_selfie_info),
                mContext.getString(R.string.wizard_ok),
                R.drawable.ic_wizard_selfie
        ));
        items.add(new WizardItem(
                DocumentType.LICENSE,
                mContext.getString(R.string.wizard_license_title),
                mContext.getString(R.string.wizard_license_descriptions),
                mContext.getString(R.string.wizard_license_action),
                mContext.getString(R.string.wizard_info_title),
                mContext.getString(R.string.wizard_license_info),
                mContext.getString(R.string.wizard_ok),
                R.drawable.ic_wizard_license
        ));
        items.add(new WizardItem(
                DocumentType.PASSPORT,
                mContext.getString(R.string.wizard_passport_title),
                mContext.getString(R.string.wizard_passport_descriptions),
                mContext.getString(R.string.wizard_passport_action),
                mContext.getString(R.string.wizard_info_title),
                mContext.getString(R.string.wizard_passport_info),
                mContext.getString(R.string.wizard_ok),
                R.drawable.ic_wizard_passport
        ));
        mView.showWizard(items);
    }

    @Override
    public void captureSelfie(FragmentActivity activity) {
        new CaptureUtil().startSelfieDialog(activity, new DocumentCaptureListener() {
            @Override
            public void onComplete() {
                mCompletedSteps.add(DocumentType.SELFIE);
                checkNextAction();
            }

            @Override
            public void onCancel() {

            }
        });
    }

    @Override
    public void captureLicense(FragmentActivity activity) {
        new CaptureUtil().startLicenseDialog(activity, new DocumentCaptureListener() {
            @Override
            public void onComplete() {
                mCompletedSteps.add(DocumentType.LICENSE);
                checkNextAction();
            }

            @Override
            public void onCancel() {

            }
        });
    }

    @Override
    public void capturePassport(FragmentActivity activity) {
        new CaptureUtil().startPassportDialog(activity, new DocumentCaptureListener() {
            @Override
            public void onComplete() {
                mCompletedSteps.add(DocumentType.PASSPORT);
                checkNextAction();
            }

            @Override
            public void onCancel() {

            }
        });
    }

    @Override
    public void onSkip() {
        if (mCompletedSteps.contains(DocumentType.SELFIE) && (mCompletedSteps.contains(DocumentType.LICENSE) || mCompletedSteps.contains(DocumentType.PASSPORT))) {
            mRepository.setUserAuthorized();
            mView.finishWizard();
        } else {
            mView.showAlert(R.string.insufficient_info, R.string.insufficient_info_desc);
        }
    }

    @Override
    public void onDestroy() {
        mView = null;
    }

    private void checkNextAction() {
        if (mCompletedSteps.size() == items.size()) {
            mRepository.setUserAuthorized();
            mView.finishWizard();
        } else
            mView.nextStep();
    }

}
