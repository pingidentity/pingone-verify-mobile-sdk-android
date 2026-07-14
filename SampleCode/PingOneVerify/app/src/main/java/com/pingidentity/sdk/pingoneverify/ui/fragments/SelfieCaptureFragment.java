package com.pingidentity.sdk.pingoneverify.ui.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.pingidentity.sdk.pingoneverify.neo.contracts.VerifyTransactionCoordinator;
import com.pingidentity.sdk.pingoneverify.neo.models.AppThemeResponse;
import com.pingidentity.sdk.pingoneverify.sample.R;
import com.pingidentity.sdk.pingoneverify.sample.databinding.DialogSelfieCaptureBinding;
import com.pingidentity.sdk.pingoneverify.utils.FeatureFlags;
import com.pingidentity.sdk.provider.language.LanguagePackProviderContract;

/**
 * Confirm/retake screen shown after selfie capture completes.
 * Launched by DocumentCapturePresenter.showSelfiePreview().
 * Submit actions use presenter-supplied callbacks — no coordinator calls from this fragment.
 */
public class SelfieCaptureFragment extends BaseFragment implements View.OnClickListener {

    private DialogSelfieCaptureBinding mBinding;
    private byte[] mCapturedPayload;
    private byte[] mCapturedPhotoBytes;
    private Runnable mOnConfirm;
    private Runnable mOnRetake;

    public SelfieCaptureFragment(VerifyTransactionCoordinator coordinator) {
        super(coordinator);
    }

    public static SelfieCaptureFragment newInstance(@NonNull AppThemeResponse appTheme,
                                                    VerifyTransactionCoordinator coordinator,
                                                    @NonNull LanguagePackProviderContract languagePresenter,
                                                    @NonNull Runnable onConfirm,
                                                    @NonNull Runnable onRetake) {
        SelfieCaptureFragment fragment = new SelfieCaptureFragment(coordinator);
        fragment.mAppTheme = appTheme;
        fragment.mLanguageProvider = languagePresenter;
        fragment.mOnConfirm = onConfirm;
        fragment.mOnRetake = onRetake;
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mBinding = DialogSelfieCaptureBinding.inflate(inflater, container, false);
        mBinding.setTheme(mAppTheme);
        mBinding.setLanguageProvider(mLanguageProvider);
        setClickListeners();
        return mBinding.getRoot();
    }

    public void showSelfiePreview(byte[] photoBytes, byte[] payloadBytes) {
        mCapturedPayload = payloadBytes;
        mCapturedPhotoBytes = photoBytes;
        if (mBinding != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.length);
            showPhotoConfirmation(bitmap);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCapturedPhotoBytes != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(mCapturedPhotoBytes, 0, mCapturedPhotoBytes.length);
            showPhotoConfirmation(bitmap);
        }
    }

    private void showPhotoConfirmation(Bitmap bitmap) {
        mBinding.viewPhoto.setVisibility(View.VISIBLE);
        mBinding.imgCapturedPhoto.setImageBitmap(bitmap);
        mBinding.bottomView.setVisibility(View.VISIBLE);
    }

    private void setClickListeners() {
        if (FeatureFlags.isVerifyTrustEnabled(requireContext())) {
            mBinding.btnCancel.setVisibility(View.GONE);
        } else {
            mBinding.btnCancel.setOnClickListener(this);
        }
        mBinding.btnRetake.setOnClickListener(this);
        mBinding.btnNext.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == null || skipClickEvent()) return;
        if (R.id.btn_cancel == v.getId()) {
            closeFragment();
        } else if (R.id.btn_retake == v.getId()) {
            mCapturedPayload = null;
            mCapturedPhotoBytes = null;
            mBinding.viewPhoto.setVisibility(View.GONE);
            mBinding.bottomView.setVisibility(View.GONE);
            closeFragment();
            mOnRetake.run();
        } else if (R.id.btn_next == v.getId()) {
            mOnConfirm.run();
        }
    }

    private void closeFragment() {
        requireActivity().getSupportFragmentManager().popBackStack();
    }
}
