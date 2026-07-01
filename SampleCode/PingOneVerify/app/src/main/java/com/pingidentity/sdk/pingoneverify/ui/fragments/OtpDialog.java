package com.pingidentity.sdk.pingoneverify.ui.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.pingidentity.sdk.pingoneverify.analytics.models.AppEvent;
import com.pingidentity.sdk.pingoneverify.neo.contracts.VerifyTransactionCoordinator;
import com.pingidentity.sdk.pingoneverify.neo.models.AppThemeResponse;
import com.pingidentity.sdk.pingoneverify.neo.models.DocumentClass;
import com.pingidentity.sdk.pingoneverify.neo.settings.DocumentCaptureSettings;
import com.pingidentity.sdk.pingoneverify.neo.settings.OtpCaptureSettings;
import java.util.function.Consumer;
import com.pingidentity.sdk.pingoneverify.sample.R;
import com.pingidentity.sdk.pingoneverify.sample.databinding.DialogOtpBinding;
import com.pingidentity.sdk.pingoneverify.sample.databinding.IncorrectOtpToastBinding;
import com.pingidentity.sdk.pingoneverify.ui.UiConstants;
import com.pingidentity.sdk.pingoneverify.utils.DateUtil;
import com.pingidentity.sdk.pingoneverify.utils.VerifySessionTimer;
import com.pingidentity.sdk.pingoneverify.utils.PingOneVerifyClientUtils;
import com.pingidentity.sdk.provider.language.LanguagePackProviderContract;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OtpDialog extends BaseFragment {
    public static final String TAG = UiConstants.Tags.OTP_DIALOG;

    private DialogOtpBinding mBinding;

    private DocumentClass mDocument;
    private boolean mCanResend;
    private String mResendCooldown;
    private String mOtpDestination;
    private final Handler mResendHandler = new Handler(Looper.getMainLooper());
    private Runnable mResendRunnable;
    private final VerifySessionTimer.Timer.TimerObserver mOtpTimerObserver = new VerifySessionTimer.Timer.TimerObserver() {
        @Override
        public void onTick(int millisRemaining) {
            if (mBinding != null) mBinding.txtTimer.setText(getTimeRemainingString(millisRemaining, R.string.idv_otp_time));
        }
        @Override
        public void onFinish() {
            if (mBinding != null) mBinding.txtTimer.setText("");
        }
    };
    private int mOtpCount = 1;
    private int mOtpTries = 0;

    public OtpDialog(VerifyTransactionCoordinator coordinator) {
        super(coordinator);
    }

    private Consumer<String> mOnSubmit;

    public static OtpDialog newInstance(AppThemeResponse appTheme, DocumentCaptureSettings settings,
                                        VerifyTransactionCoordinator coordinator,
                                        LanguagePackProviderContract languagePresenter,
                                        Consumer<String> onSubmit) {
        OtpDialog otpDialog = new OtpDialog(coordinator);
        otpDialog.mOnSubmit = onSubmit;
        otpDialog.mAppTheme = appTheme;
        otpDialog.mLanguageProvider = languagePresenter;
        if (settings instanceof OtpCaptureSettings) {
            OtpCaptureSettings otpSettings = (OtpCaptureSettings) settings;
            otpDialog.mDocument = otpSettings.getChannel();
            otpDialog.mCanResend = otpSettings.getCanResend();
                otpDialog.mResendCooldown = otpSettings.getResendCooldown();
            otpDialog.mOtpDestination = otpSettings.getOtpDestination();
        } else {
            otpDialog.mDocument = settings.getDocumentType();
        }

        return otpDialog;
    }

    @Override
    public void onCreate(Bundle save) {
        super.onCreate(save);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DialogOtpBinding.inflate(inflater, container, false);
        // Be sure to add the theme to the view
        mBinding.setTheme(mAppTheme);
        mBinding.setLanguageProvider(mLanguageProvider);
        // TODO: analytics — core concern, remove from ui
        // addAppEvents();
        setContent();
        mBinding.edtVerificationCode.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO) {
                submitPasscode();
                return true;
            }
            return false;
        });
        mBinding.btnSendVerificationCode.setOnClickListener(view -> submitPasscode());
        mBinding.btnResend.setOnClickListener(view -> {
            if (skipClickEvent()) {
                return;
            }
            resendOtp();
        });

        mBinding.btnCancel.setOnClickListener(this::onCancel);

        if (!mCanResend) {
            mBinding.btnResend.setVisibility(View.GONE);
        }
        mBinding.scrollView.getViewTreeObserver().addOnGlobalLayoutListener(() -> mBinding.scrollView.post(() -> mBinding.scrollView.fullScroll(View.FOCUS_DOWN)));
        mBinding.scrollView.setOnTouchListener((v, event) -> {
            hideKeyboard();
            return true;
        });
        setCodeListener();
        return mBinding.getRoot();
    }

    private void setCodeListener() {
        mBinding.edtVerificationCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //do nothing
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                boolean isValid = isCodeValid();
                mBinding.btnSendVerificationCode.setAlpha(isValid ? 1f : .5f);
                mBinding.btnSendVerificationCode.setEnabled(isValid);

            }

            @Override
            public void afterTextChanged(Editable editable) {
                //do nothing
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        VerifySessionTimer.getInstance().getOtpTimer().removeObserver(mOtpTimerObserver);
        if (mResendRunnable != null) mResendHandler.removeCallbacks(mResendRunnable);

        List<AppEvent> events = new ArrayList<>();
        // TODO: analytics — core concern, remove from ui
        // events.add(new AppEvent(DATA_CAPTURE_OTP_STOP, mDocument.toString() + "_" + DateUtil.getCurrentDate()));
        // TODO: analytics — core concern, remove from ui
        // events.add(new AppEvent(DATA_CAPTURE_NUMBER_OF_OTPS, mDocument.toString() + "_" + mOtpCount));
        // TODO: analytics — core concern, remove from ui
        // events.add(new AppEvent(DATA_CAPTURE_OTP_TRIES, mDocument.toString() + "_" + mOtpTries));
        // TODO: analytics — core concern, remove from ui
        // AppEventStorageImpl.getInstance().addAppEvents(events, DATA_CAPTURE);
        // TODO: analytics — core concern, remove from ui
        // AppEventManagerImpl.getInstance().flushAppEvents();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void setContent() {
        mBinding.txtTitle.setText(getLanguageString(R.string.idv_otp_description));
        if (mOtpDestination != null && !mOtpDestination.isEmpty()) {
            mBinding.txtValue.setVisibility(View.VISIBLE);
            mBinding.txtValue.setText(mOtpDestination);
        } else {
            mBinding.txtValue.setVisibility(View.GONE);
        }
        VerifySessionTimer.getInstance().getOtpTimer().addObserver(mOtpTimerObserver);
        setResendTimer();
    }

    private void setResendTimer() {
        if (mResendRunnable != null) mResendHandler.removeCallbacks(mResendRunnable);
        Date date = DateUtil.getDateFromSting(mResendCooldown);
        if (date != null) {
            int time = PingOneVerifyClientUtils.getRemainingDocumentSubmissionTime(date);
            if (time > 0) {
                mResendRunnable = this::setResendAvailable;
                mResendHandler.postDelayed(mResendRunnable, time);
            }
        }
    }

    private void resendOtp() {
        mOtpCount++;
        mCoordinator.resendOtp(mDocument);
    }

    private void setResendAvailable() {
        mBinding.btnResend.setEnabled(true);
    }

    protected void setResendDisabled() {
        mBinding.btnResend.setEnabled(false);
        setResendTimer();
    }

    void showErrorToast() {
        Toast toast = new Toast(getContext());
        IncorrectOtpToastBinding binding = IncorrectOtpToastBinding.inflate(LayoutInflater.from(getContext()));
        binding.setLanguageProvider(mLanguageProvider);
        binding.executePendingBindings();
        toast.setView(binding.getRoot());
        toast.show();
    }

    private boolean isCodeValid() {
        return mBinding.edtVerificationCode.getText() != null && mBinding.edtVerificationCode.getText().length() == 6;
    }

    private void submitPasscode() {
        if (skipClickEvent()) {
            return;
        }
        mBinding.edtVerificationCode.onEditorAction(EditorInfo.IME_ACTION_DONE);
        if (isCodeValid()) {
            mOtpTries++;
            mOnSubmit.accept(mBinding.edtVerificationCode.getText().toString());
            mBinding.btnSendVerificationCode.setButtonEnabled(false);
        }
    }

    private void addAppEvents() {
        // TODO: analytics — core concern, remove from ui
        // AppEvent startAppEvent = new AppEvent(DATA_CAPTURE_OTP_START, mDocument.toString() + "_" + DateUtil.getCurrentDate());
        // TODO: analytics — core concern, remove from ui
        // AppEventStorageImpl.getInstance().addAppEvent(startAppEvent, DATA_CAPTURE);
    }

    public void onOtpResponse(boolean isSuccess) {
        requireActivity().runOnUiThread(() -> {
            if (isSuccess) {
                // verification accepted — flow continues via showNextPresenter
            } else {
                showErrorToast();
                mBinding.edtVerificationCode.getText().clear();
                mBinding.btnSendVerificationCode.setButtonEnabled(true);
            }
        });
    }

    public void onOtpSettingsUpdated(DocumentCaptureSettings settings) {
        requireActivity().runOnUiThread(() -> {
            if (settings instanceof OtpCaptureSettings) {
                OtpCaptureSettings otpSettings = (OtpCaptureSettings) settings;
                mCanResend = otpSettings.getCanResend();
                mResendCooldown = otpSettings.getResendCooldown();
            }
            setResendDisabled();
            if (!mCanResend) {
                mBinding.btnResend.setVisibility(View.GONE);
            }
        });
    }

}
