package com.pingidentity.sdk.pingoneverify.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.pingidentity.sdk.pingoneverify.neo.contracts.VerifyTransactionCoordinator;
import com.pingidentity.sdk.pingoneverify.neo.models.AppThemeResponse;
import com.pingidentity.sdk.pingoneverify.neo.models.DocumentClass;
import com.pingidentity.sdk.pingoneverify.neo.settings.DocumentCaptureSettings;
import com.pingidentity.sdk.pingoneverify.sample.R;
import com.pingidentity.sdk.pingoneverify.sample.databinding.DialogDocumentCaptureBinding;
import com.pingidentity.sdk.pingoneverify.ui.UiConstants;
import com.pingidentity.sdk.pingoneverify.ui.utils.BindingAdapters;
import com.pingidentity.sdk.pingoneverify.utils.VerifySessionTimer;
import com.pingidentity.sdk.provider.language.LanguagePackProviderContract;

public class DocumentCaptureDialog extends BaseFragment {

    public static final String TAG = UiConstants.Tags.DOCUMENT_CAPTURE_DIALOG;

    private DocumentClass mDocument;

    private DocumentCaptureSettings mCaptureSettings;

    private DialogDocumentCaptureBinding mBinding;
    private Runnable mOnCapture;

    public DocumentCaptureDialog(VerifyTransactionCoordinator coordinator) {
        super(coordinator);
    }

    public static DocumentCaptureDialog newInstance(VerifyTransactionCoordinator coordinator, AppThemeResponse appTheme, DocumentCaptureSettings captureSettings, LanguagePackProviderContract languagePresenter, Runnable onCapture) {
        DocumentCaptureDialog scannerViewController = new DocumentCaptureDialog(coordinator);
        scannerViewController.mAppTheme = appTheme;
        scannerViewController.mCaptureSettings = captureSettings;
        scannerViewController.mLanguageProvider = languagePresenter;
        scannerViewController.mOnCapture = onCapture;
        return scannerViewController;
    }

    @Nullable
    private final VerifySessionTimer.Timer.TimerObserver mTimerObserver =
            new VerifySessionTimer.Timer.TimerObserver() {
                @Override public void onTick(int millisRemaining) {
                    if (mBinding != null) mBinding.txtTimeRemaining.setText(getTimeRemainingString(millisRemaining, R.string.idv_timer_label));
                }
                @Override public void onFinish() {
                    if (mBinding != null) mBinding.txtTimeRemaining.setText(getTimeRemainingString(null, R.string.idv_timer_label));
                }
            };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DialogDocumentCaptureBinding.inflate(inflater, container, false);

        // Be sure to add the theme to the view
        mBinding.setTheme(mAppTheme);
        mBinding.setLanguageProvider(mLanguageProvider);

        mDocument = mCaptureSettings.getDocumentType();

        setStartButton(mBinding);
        setScreenContent(mBinding);

        VerifySessionTimer.getInstance().getSessionTimer().addObserver(mTimerObserver);

        mBinding.btnCancel.setOnClickListener(this::onCancel);
        setDocumentClass(mDocument);

        return mBinding.getRoot();
    }

    private void setScreenContent(DialogDocumentCaptureBinding binding) {
        switch (mDocument) {
            case GOVERNMENT_ID:
                BindingAdapters.INSTANCE.setProviderText(binding.txtTitle, mLanguageProvider, R.string.idv_documentCapture_header_governmentId);
                binding.txtInstruction.setText(getLanguageString(R.string.idv_documentCapture_description_governmentId));
                binding.btnCapture.setText(getLanguageString(R.string.idv_dataCapture_button));
                break;
            case SELFIE:
                BindingAdapters.INSTANCE.setProviderText(binding.txtTitle, mLanguageProvider, R.string.idv_documentCapture_header_selfie);
                binding.txtInstruction.setText(getLanguageString(R.string.idv_documentCapture_description_selfie));
                binding.layoutIcContainer.setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.idv_selfie));
                binding.btnCapture.setText(getLanguageString(R.string.idv_dataCapture_button));
                break;
        }
        binding.btnSkip.setVisibility(mCaptureSettings.isOptional() ? View.VISIBLE : View.GONE);
        binding.btnSkip.setOnClickListener(view -> mCoordinator.skipDocument(mDocument));
    }

    private void setStartButton(DialogDocumentCaptureBinding binding) {
        binding.btnCapture.setOnClickListener(view -> {
            if (skipClickEvent()) return;
            mOnCapture.run();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        VerifySessionTimer.getInstance().getSessionTimer().removeObserver(mTimerObserver);
        mBinding = null;
    }

}
