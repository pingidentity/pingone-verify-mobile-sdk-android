package com.pingidentity.sdk.pingoneverify.ui.fragments;

import static com.pingidentity.sdk.pingoneverify.analytics.constants.AppEventConstants.DATA_CAPTURE_CANCELLED;
import static com.pingidentity.sdk.pingoneverify.analytics.constants.AppEventConstants.DATA_CAPTURE_OTP_CANCELLED;
import static com.pingidentity.sdk.pingoneverify.analytics.constants.AppEventConstants.EVENT_RESULT_TRUE;
import static com.pingidentity.sdk.pingoneverify.analytics.constants.AppEventConstants.ID_CAPTURE_CANCELLED;
import static com.pingidentity.sdk.pingoneverify.analytics.constants.AppEventType.DATA_CAPTURE;
import static com.pingidentity.sdk.pingoneverify.analytics.constants.AppEventType.ID_CAPTURE;
import static com.pingidentity.sdk.pingoneverify.ui.utils.ComponentFragment.applyWindowsInsetListener;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.pingidentity.sdk.pingoneverify.analytics.constants.AppEventType;
import com.pingidentity.sdk.pingoneverify.analytics.models.AppEvent;
import com.pingidentity.sdk.pingoneverify.analytics.storage.AppEventStorageImpl;
import com.pingidentity.sdk.pingoneverify.neo.contracts.VerifyTransactionCoordinator;
import com.pingidentity.sdk.pingoneverify.neo.listeners.PermissionListener;
import com.pingidentity.sdk.pingoneverify.neo.models.AppThemeResponse;
import com.pingidentity.sdk.pingoneverify.neo.models.DocumentClass;
import com.pingidentity.sdk.pingoneverify.sample.R;
import com.pingidentity.sdk.pingoneverify.ui.utils.DateFormatter;
import com.pingidentity.sdk.pingoneverify.ui.utils.UiUtil;
import com.pingidentity.sdk.pingoneverify.utils.DocumentSubmissionTimer;
import com.pingidentity.sdk.pingoneverify.utils.PingOneVerifyClientUtils;
import com.pingidentity.sdk.provider.language.LanguagePackProviderContract;
import com.pingidentity.sdk.provider.language.ext.FormatArgs;

import java.util.HashMap;

public class BaseFragment extends Fragment {

    private static final String TAG = BaseFragment.class.getName();

    protected AppThemeResponse mAppTheme;
    protected LanguagePackProviderContract mLanguageProvider;
    protected VerifyTransactionCoordinator mCoordinator;
    protected boolean isClickEnabled = true;

    private PermissionListener mPermissionListener;
    private PermissionType mPermissionType;
    private OnBackPressedCallback callback;

    @NonNull
    private DocumentClass documentClass;
    public BaseFragment(VerifyTransactionCoordinator coordinator) {
        this.mCoordinator = coordinator;
    }

    // --- Lifecycle ---

    @Override
    public void onCreate(Bundle save) {
        super.onCreate(save);
        setBackPressedCallback();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        applyWindowsInsetListener(this);
        UiUtil.updateFragmentAccessibility(this, true);
    }

    @Override
    public void onStart() {
        super.onStart();
        pollForMessages(true);
    }

    @Override
    public void onStop() {
        pollForMessages(false);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // TODO: analytics — core concern, remove from ui
        // try {
        //     AppEventManagerImpl.getInstance().flushAppEvents();
        // } catch (AppEventError.AppEventApiNotInitializedError |
        //          AppEventError.AppEventListEmptyError error) {
        //     Log.e(TAG, "Failed to submit events", error);
        // }
        callback.remove();
    }

    // --- Click / network ---

    protected boolean skipClickEvent() {
        if (!isNetworkEnabled()) {
            return true;
        }
        boolean clickEnabled = isClickEnabled;
        if (clickEnabled) {
            UiUtil.clickDelayHandler(requireView(), result -> isClickEnabled = result);
        }
        return !clickEnabled;
    }

    public boolean isNetworkEnabled() {
        if (PingOneVerifyClientUtils.isInternetAvailable()) {
            return true;
        }
        Toast.makeText(requireContext(), getLanguageString(R.string.idv_internet_error), Toast.LENGTH_SHORT).show();
        return false;
    }

    // --- Language ---

    protected String getLanguageString(int resourceId, FormatArgs... values) {
        if (mLanguageProvider != null) {
            return mLanguageProvider.getStringForResource(resourceId, getString(resourceId), values);
        }
        return getString(resourceId);
    }

    // --- Permissions ---

    public void checkPermission(PermissionListener permissionListener, PermissionType type) {
        checkPermission(permissionListener, type, false);
    }

    public void checkPermission(PermissionListener permissionListener, PermissionType type, boolean askExplicit) {
        String permission = type.getPermission();
        if (!askExplicit && ContextCompat.checkSelfPermission(requireActivity(), permission) == PackageManager.PERMISSION_GRANTED) {
            permissionListener.onPermissionGranted();
        } else {
            this.mPermissionListener = permissionListener;
            if (shouldShowRequestPermissionRationale(permission)) {
                if (type == PermissionType.LOCATION) {
                    mPermissionListener.onPermissionDenied();
                } else {
                    displayNeverAskAgainDialog(false, type);
                }
            } else {
                launchPermission(type);
            }
        }
    }

    public void displayNeverAskAgainDialog(boolean canRetry, PermissionType type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        switch (type) {
            case CAMERA:
                builder.setTitle(getLanguageString(R.string.idv_permission_camera_title));
                builder.setMessage(getLanguageString(R.string.idv_permission_dialog_message_camera));
                break;
            case LOCATION:
                builder.setTitle(getLanguageString(R.string.idv_permission_location_title));
                builder.setMessage(getLanguageString(R.string.idv_permission_dialog_message_location));
                break;
        }
        builder.setCancelable(false);
        builder.setNegativeButton(getLanguageString(R.string.idv_data_cancel), (d, i) -> mPermissionListener.onPermissionDenied());
        int positiveTextId = canRetry ? R.string.idv_data_retry : R.string.idv_permission_dialog_settings;
        builder.setPositiveButton(getLanguageString(positiveTextId), (d, i) -> {
            if (canRetry) {
                launchPermission(type);
            } else {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        });
        builder.create().show();
    }

    private void launchPermission(PermissionType type) {
        this.mPermissionType = type;
        permissionResult.launch(type.getPermission());
    }

    private final ActivityResultLauncher<String> permissionResult = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), result -> {
                if (Boolean.TRUE.equals(result)) {
                    mPermissionListener.onPermissionGranted();
                } else {
                    mPermissionListener.onPermissionDenied();
                }
            });

    // --- Back press / cancel ---

    private void setBackPressedCallback() {
        callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (BaseFragment.this instanceof ProcessingDialog) {
                    // Do nothing: API in progress
                } else {
                    onCancel(null);
                }
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    public void onCancel(@Nullable View view) {
        if (view != null && skipClickEvent()) {
            return;
        }
        // TODO: analytics — core concern, remove from ui
        // reportCanceledAppEvent();
        cancelAndClose();
    }

    private void cancelAndClose() {
        if (mCoordinator != null) mCoordinator.endVerification();
        DocumentSubmissionTimer.getInstance().stop();
    }

    // --- Document type ---

    public void setDocumentClass(DocumentClass document) {
        documentClass = document;
    }

    public HashMap<String, String> getDocumentTypeMap() {
        return PingOneVerifyClientUtils.getDocumentTypeMap(documentClass);
    }

    // --- UI helpers ---

    public void hideKeyboard() {
        if (getActivity() != null && getActivity().getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        }
    }

    public String getTimeRemainingString(Integer time, Integer label) {
        if (time == null) return "";
        String minLabel = DateFormatter.INSTANCE.getMinutes(requireContext(), mLanguageProvider, time, R.string.idv_time_min);
        String secLabel = DateFormatter.INSTANCE.getSeconds(requireContext(), mLanguageProvider, time, R.string.idv_time_sec);
        return DateFormatter.INSTANCE.getTimeDifferenceFormatted(requireContext(), mLanguageProvider, minLabel, secLabel, label);
    }

    public void reportCanceledAppEvent() {
        String appEventKey;
        AppEventType appEventType = DATA_CAPTURE;
        if (this instanceof LocationFragment) {
            appEventKey = DATA_CAPTURE_CANCELLED;
        } else if (this instanceof EmailPhoneVerificationDialog) {
            appEventKey = DATA_CAPTURE_OTP_CANCELLED;
        } else if (this instanceof DocumentCaptureDialog) {
            appEventKey = ID_CAPTURE_CANCELLED;
            appEventType = ID_CAPTURE;
        } else {
            appEventKey = TAG + "_CANCELLED";
        }
        AppEventStorageImpl.getInstance().addAppEvent(new AppEvent(appEventKey, EVENT_RESULT_TRUE), appEventType);
    }

    public void pollForMessages(boolean start) {
        // Override where needed
    }

    // --- Permission type enum ---

    public enum PermissionType {
        CAMERA,
        LOCATION;

        String getPermission() {
            switch (this) {
                case LOCATION:
                    return Manifest.permission.ACCESS_FINE_LOCATION;
                default:
                    return Manifest.permission.CAMERA;
            }
        }
    }
}
