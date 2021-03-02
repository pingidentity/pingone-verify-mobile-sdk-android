package com.pingidentity.sample.P1VerifyApp.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;

import com.pingidentity.sample.P1VerifyApp.R;
import com.pingidentity.sample.P1VerifyApp.databinding.ViewValidationBinding;

public class ValidationView extends LinearLayout {

    private ViewValidationBinding mBinding;

    public ValidationView(Context context) {
        super(context);
        init();
    }

    public ValidationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ValidationView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mBinding = ViewValidationBinding.inflate(LayoutInflater.from(getContext()), this, true);
    }

    public void showValidationSuccess() {
        mBinding.progressStatus.setVisibility(View.GONE);
        mBinding.imgValidation.setVisibility(View.VISIBLE);
        mBinding.imgValidation.setImageDrawable(getContext().getDrawable(R.drawable.ic_validation_success));
        mBinding.viewValidationStatus.setBackgroundColor(getContext().getColor(R.color.success_color));
        mBinding.txtStatus.setTextColor(getContext().getColor(android.R.color.white));
        mBinding.txtStatus.setText(R.string.validation_success);
        mBinding.viewValidationStatus.setOnClickListener(null);
        showView();

    }

    public void showValidationError(final Runnable onClick) {
        mBinding.progressStatus.setVisibility(View.GONE);
        mBinding.imgValidation.setVisibility(View.VISIBLE);
        mBinding.imgValidation.setImageDrawable(getContext().getDrawable(R.drawable.ic_validation_error));
        mBinding.viewValidationStatus.setBackgroundColor(getContext().getColor(R.color.error_color));
        mBinding.txtStatus.setTextColor(getContext().getColor(android.R.color.white));
        mBinding.txtStatus.setText(R.string.validation_failure);
        mBinding.viewValidationStatus.setOnClickListener(v -> onClick.run());
        showView();
    }

    public void showValidationInProgress() {
        mBinding.progressStatus.setVisibility(View.VISIBLE);
        mBinding.imgValidation.setVisibility(View.GONE);
        mBinding.viewValidationStatus.setBackgroundColor(getContext().getColor(R.color.progress_color));
        mBinding.txtStatus.setTextColor(getContext().getColor(android.R.color.black));
        mBinding.txtStatus.setText(R.string.validation_progress);
        mBinding.viewValidationStatus.setOnClickListener(null);
        showView();
    }

    public void hideValidation(){
        mBinding.viewValidationStatus.setVisibility(View.GONE);
    }

    private void showView() {
        mBinding.viewValidationStatus.setVisibility(View.VISIBLE);
        TranslateAnimation animate = new TranslateAnimation(
                0, 0, mBinding.viewValidationStatus.getHeight(), 0);
        animate.setDuration(500);
        animate.setFillAfter(true);
        mBinding.viewValidationStatus.startAnimation(animate);
    }
}
