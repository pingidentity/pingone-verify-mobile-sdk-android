package com.pingidentity.sdk.pingoneverify.ui.fragments;

import static com.pingidentity.sdk.pingoneverify.neo.models.DocumentClass.EMAIL;
import static com.pingidentity.sdk.pingoneverify.neo.models.DocumentClass.PHONE;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.pingidentity.sdk.pingoneverify.models.Constants;
import java.util.function.Consumer;
import com.pingidentity.sdk.pingoneverify.ui.Country;
import com.pingidentity.sdk.pingoneverify.neo.contracts.VerifyTransactionCoordinator;
import com.pingidentity.sdk.pingoneverify.neo.models.AppThemeResponse;
import com.pingidentity.sdk.pingoneverify.neo.models.DocumentClass;
import com.pingidentity.sdk.pingoneverify.neo.settings.DocumentCaptureSettings;
import com.pingidentity.sdk.pingoneverify.sample.R;
import com.pingidentity.sdk.pingoneverify.sample.databinding.DialogInformationCaptureBinding;
import com.pingidentity.sdk.pingoneverify.neo.settings.EmailCaptureSettings;
import com.pingidentity.sdk.pingoneverify.neo.settings.PhoneCaptureSettings;
import com.pingidentity.sdk.pingoneverify.ui.UiConstants;
import com.pingidentity.sdk.pingoneverify.ui.utils.BindingAdapters;
import com.pingidentity.sdk.pingoneverify.utils.DocumentSubmissionTimer;
import com.pingidentity.sdk.pingoneverify.ui.utils.CountryUtil;
import com.pingidentity.sdk.pingoneverify.utils.PingOneVerifyClientUtils;
import com.pingidentity.sdk.provider.language.LanguagePackProviderContract;

import java.util.HashMap;
import java.util.List;
import java.util.List;
import java.util.Map;

public class EmailPhoneVerificationDialog extends BaseFragment {
    public static final String TAG = UiConstants.Tags.EMAIL_PHONE_VERIFICATION_DIALOG;
    private static final int TYPE_TEXT = 1;
    private static final int TYPE_EDIT = 2;
    private static final int TYPE_SPINNER = 3;
    private DialogInformationCaptureBinding mBinding;
    private DocumentClass mDocument;
    private DocumentCaptureSettings mSettings;
    private int mInputType;
    private Country mCountry;
    private List<Country> mCountriesList;
    private CountryPickerDialog mCountryDialog;

    public EmailPhoneVerificationDialog(VerifyTransactionCoordinator coordinator) {
        super(coordinator);
    }

    private Consumer<String> mOnSubmit;

    private final DocumentSubmissionTimer.Observer mTimerObserver =
            new DocumentSubmissionTimer.Observer() {
                @Override public void onTick(int millisRemaining) {
                    if (mBinding != null) mBinding.txtTimeRemaining.setText(getTimeRemainingString(millisRemaining, R.string.idv_timer_label));
                }
                @Override public void onFinish() {}
            };

    public static EmailPhoneVerificationDialog newInstance(DocumentClass document, VerifyTransactionCoordinator coordinator, DocumentCaptureSettings settings, AppThemeResponse appTheme, LanguagePackProviderContract languageProvider, Consumer<String> onSubmit) {
        EmailPhoneVerificationDialog emailPhoneViewController = new EmailPhoneVerificationDialog(coordinator);
        emailPhoneViewController.mOnSubmit = onSubmit;
        emailPhoneViewController.mAppTheme = appTheme;
        emailPhoneViewController.mDocument = document;
        emailPhoneViewController.mLanguageProvider = languageProvider;
        emailPhoneViewController.mSettings = settings;
        return emailPhoneViewController;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DialogInformationCaptureBinding.inflate(inflater, container, false);
        requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        // Be sure to add the theme to the view
        mBinding.setTheme(mAppTheme);
        mBinding.setLanguageProvider(mLanguageProvider);
        DocumentSubmissionTimer.getInstance().addObserver(mTimerObserver);
        addAppEvents();

        setDocumentClass(mDocument);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setScreenContent();
        mBinding.btnSkip.setVisibility(mSettings.isOptional() ? View.VISIBLE : View.GONE);
        mBinding.btnCancel.setOnClickListener(this::onCancel);
        mBinding.btnCapture.setOnClickListener(v -> onSubmit());
        mBinding.btnSkip.setOnClickListener(v -> mCoordinator.skipDocument(mDocument));

        mBinding.scrollView.getViewTreeObserver().addOnGlobalLayoutListener(() -> mBinding.scrollView.post(() -> mBinding.scrollView.fullScroll(View.FOCUS_DOWN)));
        mBinding.scrollView.setOnTouchListener((v, event) -> {
            hideKeyboard();
            return true;
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        DocumentSubmissionTimer.getInstance().removeObserver(mTimerObserver);
        // TODO: analytics — core concern, remove from ui
        // AppEvent stopAppEvent = new AppEvent(DATA_CAPTURE_STOP, mDocument.toString() + "_" + DateUtil.getCurrentDate());
        // TODO: analytics — core concern, remove from ui
        // AppEventStorageImpl.getInstance().addAppEvent(stopAppEvent, DATA_CAPTURE);
    }

    private void onSubmit() {
        if (skipClickEvent()) {
            return;
        }
        mBinding.btnCapture.setButtonEnabled(false);
        Map<String, String> document = new HashMap<>();
        String destination = null;
        if (mInputType == TYPE_TEXT) {
            destination = mBinding.txtDestination.getText().toString();
        }
        if (mInputType == TYPE_SPINNER) {
            destination = mBinding.spinnerDestination.getItemAtPosition(mBinding.spinnerDestination.getSelectedItemPosition()).toString();
        }
        if (mInputType == TYPE_EDIT) {
            if (mSettings.getDocumentType() == PHONE) {
                destination = getFormattedNumber();
            }
            if (mSettings.getDocumentType() == EMAIL) {
                destination = mBinding.edtDestination.getText().toString();
            }
        }
        if (destination != null) mOnSubmit.accept(destination);
        hideKeyboard();
    }

    private void setValidationListener(EditText view) {
        view.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //do nothing
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (mInputType == TYPE_EDIT) {
                    if (mDocument == PHONE) {
                        validatePhoneNumber();
                    } else if (mDocument == EMAIL) {
                        updateButtonState(PingOneVerifyClientUtils.isEmailValid(charSequence.toString()));
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //do nothing
            }
        });
    }

    private void validatePhoneNumber() {
        if (mCountry == null) return;
        String fullNumber = mCountry.getDialCode() + mBinding.edtPhoneNumber.getText().toString();
        updateButtonState(PingOneVerifyClientUtils.isPhoneValid(fullNumber));
    }

    private void updateButtonState(boolean isValid) {
        mBinding.btnCapture.setButtonEnabled(isValid);
    }

    private String getFormattedNumber() {
        String text = mBinding.edtPhoneNumber.getText().toString();
        return mCountry.getDialCode() + Constants.HYPHEN + text.replace(Constants.SPACE, "");
    }

    private void setScreenContent() {
        if (mDocument.equals(EMAIL)) {
            setStateEmail();
        } else if (mDocument.equals(PHONE)) {
            setStatePhone();
        }

        mBinding.btnCapture.setButtonEnabled(mInputType == TYPE_SPINNER || mInputType == TYPE_TEXT);
    }

    private void setStateEmail() {
        BindingAdapters.INSTANCE.setProviderText(mBinding.txtTitle, mLanguageProvider, R.string.idv_emailCapture_title);
        mBinding.btnCapture.setText(getLanguageString(R.string.idv_emailCapture_button));
        mBinding.layoutIcContainer.setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.idv_email));
        EmailCaptureSettings emailSettings = (EmailCaptureSettings) mSettings;
        String emailRequirementValue = emailSettings.getRequirementValue();
        List<String> emailRequirementOptions = emailSettings.getRequirementOptions();
        if (emailRequirementValue != null || emailRequirementOptions != null) {
            mBinding.viewEmail.setVisibility(View.GONE);
            if (emailRequirementValue != null) {
                setupTextType(emailRequirementValue);
                mBinding.txtInstruction.setText(getLanguageString(R.string.idv_emailCapture_description_value));
            } else if (emailRequirementOptions != null && !emailRequirementOptions.isEmpty()) {
                setupSpinnerType(emailRequirementOptions);
                mBinding.txtInstruction.setText(getLanguageString(R.string.idv_emailCapture_description_option));
            }
        } else {
            setEditTypeEmail();
            setValidationListener(mBinding.edtDestination);
            mBinding.txtInstruction.setText(getLanguageString(R.string.idv_emailCapture_description));
        }
    }

    private void setStatePhone() {
        mCountriesList = CountryUtil.getCountryCodes(requireContext());
        mCountriesList.stream().filter(
                country -> country.getCode().equals(Constants.COUNTRY_CODE_DEFAULT)
        ).findFirst().ifPresent(this::updateCountry);
        BindingAdapters.INSTANCE.setProviderText(mBinding.txtTitle, mLanguageProvider, R.string.idv_phoneCapture_title);
        mBinding.btnCapture.setText(getLanguageString(R.string.idv_phoneCapture_button));
        mBinding.layoutIcContainer.setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.idv_phone));
        mBinding.txtCountryCode.setOnClickListener(view -> {
            mCountryDialog = CountryPickerDialog.newInstance(mAppTheme, mLanguageProvider, mCountriesList, country -> {
                updateCountry(country);
                validatePhoneNumber();
                clearCountryDialog();
            });
            mCountryDialog.show(requireActivity().getSupportFragmentManager(), CountryPickerDialog.TAG);
        });
        PhoneCaptureSettings phoneSettings = (PhoneCaptureSettings) mSettings;
        String phoneRequirementValue = phoneSettings.getRequirementValue();
        List<String> phoneRequirementOptions = phoneSettings.getRequirementOptions();
        if (phoneRequirementValue != null || phoneRequirementOptions != null) {
            mBinding.viewEmail.setVisibility(View.GONE);
            if (phoneRequirementValue != null) {
                setupTextType(phoneRequirementValue);
                mBinding.txtInstruction.setText(getLanguageString(R.string.idv_phoneCapture_description_value));
            } else if (phoneRequirementOptions != null && !phoneRequirementOptions.isEmpty()) {
                setupSpinnerType(phoneRequirementOptions);
                mBinding.txtInstruction.setText(getLanguageString(R.string.idv_phoneCapture_description_option));
            }
        } else {
            setEditTypePhone();
            setValidationListener(mBinding.edtPhoneNumber);
            mBinding.txtInstruction.setText(getLanguageString(R.string.idv_phoneCapture_description));
        }
    }

    private void updateCountry(Country country) {
        if (country != null) {
            mCountry = country;
            mBinding.txtCountryCode.setText(mCountry.getDialCode());
        }
    }

    private void setEditTypeEmail() {
        mInputType = TYPE_EDIT;
        mBinding.viewEmail.setVisibility(View.VISIBLE);
        mBinding.viewSpinnerDestination.setVisibility(View.GONE);
        mBinding.txtDestination.setVisibility(View.GONE);
    }

    private void setEditTypePhone() {
        mInputType = TYPE_EDIT;
        mBinding.viewPhoneNumber.setVisibility(View.VISIBLE);
        mBinding.viewEmail.setVisibility(View.GONE);
        mBinding.viewSpinnerDestination.setVisibility(View.GONE);
        mBinding.txtDestination.setVisibility(View.GONE);
    }

    private void setupTextType(String data) {
        mInputType = TYPE_TEXT;
        mBinding.txtDestination.setText(data);
        mBinding.viewSpinnerDestination.setVisibility(View.GONE);
        mBinding.txtDestination.setVisibility(View.VISIBLE);
    }

    private void setupSpinnerType(List<String> data) {
        mInputType = TYPE_SPINNER;
        mBinding.viewSpinnerDestination.setVisibility(View.VISIBLE);
        mBinding.txtDestination.setVisibility(View.GONE);
        ArrayAdapter<String> ad = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, data);
        mBinding.spinnerDestination.setAdapter(ad);
    }

    private void addAppEvents() {
        // TODO: analytics — core concern, remove from ui
        // AppEvent startAppEvent = new AppEvent(DATA_CAPTURE_START, mDocument.toString() + "_" + DateUtil.getCurrentDate());
        // boolean hasRequirements = false; ...
        // AppEvent hasRequirementsAppEvent = new AppEvent(DATA_CAPTURE_REQUIREMENTS, ...);
        // AppEventStorageImpl.getInstance().addAppEvents(events, DATA_CAPTURE);
    }

    @Override
    public void onStop() {
        super.onStop();
        clearCountryDialog();
    }

    private void clearCountryDialog() {
        if (mCountryDialog != null) {
            mCountryDialog.dismissAllowingStateLoss();
            mCountryDialog = null;
        }
    }

}