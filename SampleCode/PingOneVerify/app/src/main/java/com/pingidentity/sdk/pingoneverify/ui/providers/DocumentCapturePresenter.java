package com.pingidentity.sdk.pingoneverify.ui.providers;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.pingidentity.sdk.pingoneverify.contracts.DocumentCaptureContract;
import com.pingidentity.sdk.pingoneverify.neo.settings.OtpCaptureSettings;
import com.pingidentity.sdk.pingoneverify.utils.DocumentSubmissionTimer;
import com.pingidentity.sdk.pingoneverify.neo.contracts.VerifyTransactionCoordinator;
import com.pingidentity.sdk.pingoneverify.neo.models.AppThemeResponse;
import com.pingidentity.sdk.pingoneverify.neo.models.RetryFeedback;
import com.pingidentity.sdk.pingoneverify.neo.models.DocumentClass;
import com.pingidentity.sdk.pingoneverify.neo.settings.DocumentCaptureSettings;
import com.pingidentity.sdk.pingoneverify.sample.R;
import com.pingidentity.sdk.pingoneverify.neo.settings.SelfieCaptureSettings;
import com.pingidentity.sdk.pingoneverify.neo.settings.LocationCaptureSettings;
import com.pingidentity.sdk.pingoneverify.ui.UiConstants;
import com.pingidentity.sdk.pingoneverify.ui.fragments.BaseFragment;
import com.pingidentity.sdk.pingoneverify.ui.fragments.DocumentCaptureDialog;
import com.pingidentity.sdk.pingoneverify.ui.fragments.IDCaptureDialog;
import com.pingidentity.sdk.pingoneverify.ui.fragments.EmailPhoneVerificationDialog;
import com.pingidentity.sdk.pingoneverify.ui.fragments.GeolocationRetryFragment;
import com.pingidentity.sdk.pingoneverify.ui.fragments.LocationFragment;
import com.pingidentity.sdk.pingoneverify.ui.fragments.OtpDialog;
import com.pingidentity.sdk.pingoneverify.ui.fragments.ProcessingDialog;
import com.pingidentity.sdk.pingoneverify.ui.fragments.SelfieCaptureFragment;
import com.pingidentity.sdk.pingoneverify.ui.fragments.UploadFailureDialog;
import com.pingidentity.sdk.pingoneverify.ui.utils.UiUtil;
import com.pingidentity.sdk.pingoneverify.utils.PingOneVerifyClientUtils;
import com.pingidentity.sdk.provider.language.LanguagePackProviderContract;

import android.util.Base64;

import com.pingidentity.sdk.pingoneverify.neo.models.IdCaptureResult;
import com.pingidentity.sdk.pingoneverify.neo.models.SelfieCaptureResult;

import java.lang.ref.WeakReference;
import java.util.function.Consumer;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Concrete implementation of {@link DocumentCaptureContract}.
 * Owns all fragment launching and UI routing for the built-in verification flow.
 *
 * Android equivalent of iOS's {@code DocumentCapturePresenter} class.
 * Held by {@link PingOneVerifyHelper} — never used directly by core or integrators.
 */
public class DocumentCapturePresenter implements DocumentCaptureContract {

    private WeakReference<FragmentActivity> mActivityRef = new WeakReference<>(null);
    AppThemeResponse mAppTheme;
    LanguagePackProviderContract mLanguageProvider;
    private DocumentClass mDocumentType;
    private OtpDialog mActiveOtpDialog;
    private int mInitialEntryCount = 0;

    private final Set<String> tagSet = new HashSet<>();

    void attach(FragmentActivity activity) {
        mActivityRef = new WeakReference<>(activity);
        mInitialEntryCount = activity.getSupportFragmentManager().getBackStackEntryCount();
    }

    void setAppTheme(AppThemeResponse appTheme) {
        mAppTheme = appTheme;
    }

    void setLanguageProvider(LanguagePackProviderContract languageProvider) {
        mLanguageProvider = languageProvider;
    }

    private void postDelayedOnMain(Runnable action) {
        new Handler(Looper.getMainLooper()).postDelayed(action, 1000L);
    }

    // Navigation methods (absorbed from NavigationUtil)
    public void removeCompletedCaptureViewControllers() {
        FragmentActivity activity = mActivityRef.get();
        if (activity == null) return;
        FragmentManager fm = activity.getSupportFragmentManager();
        if (fm.getBackStackEntryCount() == mInitialEntryCount) return;
        int currentSize = fm.getBackStackEntryCount();
        for (int i = mInitialEntryCount; i < currentSize - 1; i++) {
            FragmentManager.BackStackEntry current = fm.getBackStackEntryAt(i);
            FragmentManager.BackStackEntry next = fm.getBackStackEntryAt(i + 1);
            if (current.getName() == null || next.getName() == null) continue;
            if ((current.getName().contains(UiConstants.Tags.EMAIL_PHONE_VERIFICATION_DIALOG) && next.getName().contains(UiConstants.Tags.OTP_DIALOG)) ||
                (current.getName().contains(UiConstants.Tags.DOCUMENT_CAPTURE_DIALOG) && next.getName().contains(UiConstants.ID_CAPTURE_TAG))) {
                fm.popBackStack(next.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
                fm.popBackStack(current.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        }
    }

    public void closeNavigation() {
        FragmentActivity activity = mActivityRef.get();
        if (activity == null) return;
        DocumentSubmissionTimer.getInstance().stop();
        clearBackStack();
        mActiveOtpDialog = null;
    }

    private void clearBackStack() {
        FragmentActivity activity = mActivityRef.get();
        if (activity == null) return;
        int count = activity.getSupportFragmentManager().getBackStackEntryCount();
        if (count == 0 || mInitialEntryCount >= count) return;
        try {
            FragmentManager.BackStackEntry entry =
                activity.getSupportFragmentManager().getBackStackEntryAt(mInitialEntryCount);
            activity.getSupportFragmentManager().popBackStackImmediate(
                entry.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } catch (IllegalArgumentException e) {
            Log.e("DocumentCapturePresenter", "Error clearing backstack", e);
        }
    }

    public void closeCurrentScreen() {
        FragmentActivity activity = mActivityRef.get();
        if (activity == null) return;
        activity.getSupportFragmentManager().popBackStack();
    }

    // --- DocumentCaptureContract ---

    @Override
    public void captureDocument(VerifyTransactionCoordinator coordinator, DocumentCaptureSettings settings) {
        FragmentActivity activity = mActivityRef.get();
        if (activity == null) return;
        removeCompletedCaptureViewControllers();
        BaseFragment fragment = switch (settings.getDocumentType()) {
            case EMAIL, PHONE -> EmailPhoneVerificationDialog.newInstance(
                    settings.getDocumentType(), coordinator, settings, mAppTheme, mLanguageProvider,
                    destination -> {
                        showWaitOverlay(coordinator);
                        if (settings.getDocumentType() == DocumentClass.EMAIL) coordinator.submitEmail(destination);
                        else coordinator.submitPhone(destination);
                    });
            case SELFIE -> {
                // Auth flow: skip instruction screen and go directly to capture
                if (((SelfieCaptureSettings) settings).isAuthFlow()) {
                    startCapture(coordinator, settings);
                    yield null;
                }
                yield DocumentCaptureDialog.newInstance(coordinator, mAppTheme, settings, mLanguageProvider,
                        () -> startCapture(coordinator, settings));
            }
            default -> DocumentCaptureDialog.newInstance(coordinator, mAppTheme, settings, mLanguageProvider,
                    () -> startCapture(coordinator, settings));
        };
        if (fragment != null) {
            startFragment(activity, fragment, null, settings.getDocumentType());
        }
    }

    @Override
    public void captureLocation(VerifyTransactionCoordinator coordinator, LocationCaptureSettings settings) {
        FragmentActivity activity = mActivityRef.get();
        if (activity == null) return;
        LocationFragment fragment = LocationFragment.newInstance(mAppTheme, settings, coordinator, mLanguageProvider,
                () -> showGeolocationRetry(coordinator));
        startFragment(activity, fragment, null, DocumentClass.GEOLOCATION);
    }

    void showGeolocationRetry(VerifyTransactionCoordinator coordinator) {
        FragmentActivity activity = mActivityRef.get();
        if (activity == null) return;
        ProcessingDialog processingDialog = ProcessingDialog.newInstance(mAppTheme, mLanguageProvider);
        startFragment(activity, processingDialog, UiConstants.Tags.PROCESSING_DIALOG);
        postDelayedOnMain(() -> {
            hideWaitOverlay();
            FragmentActivity a = mActivityRef.get();
            if (a == null) return;
            GeolocationRetryFragment fragment = GeolocationRetryFragment.newInstance(mAppTheme, coordinator, mLanguageProvider);
            startFragment(a, fragment, null, DocumentClass.GEOLOCATION);
        });
    }

    @Override
    public void captureOtp(VerifyTransactionCoordinator coordinator, OtpCaptureSettings settings) {
        FragmentActivity activity = mActivityRef.get();
        if (activity == null) return;
        OtpDialog otpDialog = OtpDialog.newInstance(mAppTheme, settings, coordinator, mLanguageProvider,
                passcode -> {
                    showWaitOverlay(coordinator);
                    DocumentClass otpType = settings.getChannel();
                    coordinator.submitOtp(passcode, otpType);
                });
        mActiveOtpDialog = otpDialog;
        startFragment(activity, otpDialog, OtpDialog.TAG);
    }

    void startCapture(VerifyTransactionCoordinator coordinator, DocumentCaptureSettings settings) {
        FragmentActivity activity = mActivityRef.get();
        if (activity == null) return;
        switch (settings.getDocumentType()) {
            case SELFIE -> coordinator.captureSelfie(activity);
            case GOVERNMENT_ID -> coordinator.captureGovernmentId(activity);
            default -> {}
        }
    }

    public void notifyOtpResult(boolean success) {
        hideWaitOverlay();
        if (mActiveOtpDialog != null) {
            mActiveOtpDialog.onOtpResponse(success);
        }
    }

    public void notifyOtpSettingsUpdated(DocumentCaptureSettings settings) {
        if (mActiveOtpDialog != null) {
            mActiveOtpDialog.onOtpSettingsUpdated(settings);
        }
    }


    public void showIdPreview(VerifyTransactionCoordinator coordinator, IdCaptureResult result) {
        FragmentActivity activity = mActivityRef.get();
        if (activity == null) return;
        Consumer<Map<String, String>> onConfirm = map -> {
            closeCurrentScreen();
            showWaitOverlay(coordinator);
            coordinator.submitGovernmentId(new IdCaptureResult(map, result.getIdType()));
        };
        IDCaptureDialog dialog = IDCaptureDialog.newInstance(mAppTheme, result.getIdType(), coordinator, mLanguageProvider, onConfirm);
        dialog.showResult(result.getDocumentData());
        startFragment(activity, dialog, null, DocumentClass.GOVERNMENT_ID);
    }

    public void showSelfiePreview(VerifyTransactionCoordinator coordinator, SelfieCaptureResult result) {
        FragmentActivity activity = mActivityRef.get();
        if (activity == null) return;
        Runnable onConfirm = () -> {
            closeCurrentScreen();
            showWaitOverlay(coordinator);
            coordinator.submitSelfie(result);
        };
        Runnable onRetake = () -> startCapture(coordinator, new SelfieCaptureSettings(45, true));
        // Convert base64 strings back to bytes for the preview display
        byte[] photoBytes = result.getSelfie() != null ? Base64.decode(result.getSelfie(), Base64.NO_WRAP) : new byte[0];
        byte[] payloadBytes = result.getIadPayload() != null ? Base64.decode(result.getIadPayload(), Base64.NO_WRAP) : null;
        SelfieCaptureFragment fragment = SelfieCaptureFragment.newInstance(mAppTheme, coordinator, mLanguageProvider, onConfirm, onRetake);
        startFragment(activity, fragment, null, DocumentClass.SELFIE);
        fragment.showSelfiePreview(photoBytes, payloadBytes);
    }

    @Override
    public void showWaitOverlay(VerifyTransactionCoordinator coordinator) {
        FragmentActivity activity = mActivityRef.get();
        if (activity == null) return;
        ProcessingDialog processingDialog = ProcessingDialog.newInstance(mAppTheme, mLanguageProvider);
        startFragment(activity, processingDialog, UiConstants.Tags.PROCESSING_DIALOG);
    }

    @Override
    public void hideWaitOverlay() {
        removeFragmentByTag(UiConstants.Tags.PROCESSING_DIALOG);
    }

    @Override
    public void onShowUploadRetry(VerifyTransactionCoordinator coordinator,
                                  RetryFeedback message,
                                  DocumentCaptureSettings settings,
                                  Consumer<Boolean> onRetryDecision) {
        FragmentActivity activity = mActivityRef.get();
        if (activity == null) return;
        UploadFailureDialog uploadFailureDialog = UploadFailureDialog.newInstance(mAppTheme, onRetryDecision, message, settings, coordinator, mLanguageProvider);
        startFragment(activity, uploadFailureDialog, UiConstants.Tags.UPLOAD_FAILURE_DIALOG);
    }

    @Override
    public void removeFragmentByTag(String tag) {
        FragmentActivity activity = mActivityRef.get();
        if (activity == null) return;
        if (tagSet.contains(tag)) {
            activity.getSupportFragmentManager().popBackStackImmediate(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            tagSet.remove(tag);
        }
    }

    // --- Internal fragment management ---

    private void startFragment(FragmentActivity activity, Fragment fragment, String tagName) {
        startFragment(activity, fragment, tagName, null);
    }

    private void startFragment(FragmentActivity activity, Fragment fragment, String tagName, DocumentClass documentType) {
        String uniqueName = PingOneVerifyClientUtils.getFragmentTag(documentType, tagName);
        if (tagName != null) {
            if (tagSet.contains(tagName)) {
                removeFragmentByTag(tagName);
            }
            tagSet.add(tagName);
        } else if (documentType != null) {
            if (mDocumentType != null) {
                clearBackStack();
            }
            mDocumentType = documentType;
            tagSet.clear();
        }
        List<Fragment> fragments = activity.getSupportFragmentManager().getFragments();
        if (!fragments.isEmpty()) {
            UiUtil.updateFragmentAccessibility(fragments.get(0), false);
        }
        activity.getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(android.R.id.content, fragment, uniqueName)
                .addToBackStack(uniqueName)
                .commitAllowingStateLoss();
    }
}
