package com.pingidentity.sdk.pingoneverify.ui.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.pingidentity.sdk.pingoneverify.analytics.models.AppEvent;
import com.pingidentity.sdk.pingoneverify.neo.contracts.CaptureResultReceiver;
import com.pingidentity.sdk.pingoneverify.neo.contracts.VerifyTransactionCoordinator;
import com.pingidentity.sdk.pingoneverify.neo.errors.DocumentSubmissionError;
import com.pingidentity.sdk.pingoneverify.neo.listeners.IDCaptureResultListener;
import com.pingidentity.sdk.pingoneverify.neo.models.AppThemeResponse;
import com.pingidentity.sdk.pingoneverify.neo.models.DocumentClass;
import com.pingidentity.sdk.pingoneverify.sample.databinding.DialogIdcaptureBinding;
import com.pingidentity.sdk.pingoneverify.sample.databinding.LayoutUploadCheckBinding;
import com.pingidentity.sdk.provider.language.LanguagePackProviderContract;

import java.util.ArrayList;
import com.pingidentity.sdk.pingoneverify.neo.models.DocumentKeys;
import java.util.function.Consumer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IDCaptureDialog extends BaseFragment {
    public static final String TAG = IDCaptureDialog.class.getName();
    private static final String CAPTURE_VENDOR = "MicroBlink";

    private HashMap<String, String> mResult;
    private String mDocumentType;

    private byte[] mFrontImageBytes;
    private byte[] mBackImageBytes;

    private DialogIdcaptureBinding mRootBinding;
    private LayoutUploadCheckBinding mCheckBinding;

    public IDCaptureDialog(VerifyTransactionCoordinator coordinator) {
        super(coordinator);
    }

    private Consumer<Map<String, String>> mOnConfirm;

    public static IDCaptureDialog newInstance(AppThemeResponse appTheme, String type,
                                              VerifyTransactionCoordinator coordinator,
                                              LanguagePackProviderContract languagePresenter,
                                              Consumer<Map<String, String>> onConfirm) {
        IDCaptureDialog dialogFragment = new IDCaptureDialog(coordinator);
        dialogFragment.mAppTheme = appTheme;
        dialogFragment.mLanguageProvider = languagePresenter;
        dialogFragment.mDocumentType = type;
        dialogFragment.mOnConfirm = onConfirm;
        return dialogFragment;
    }

    private final IDCaptureResultListener mIdCaptureResultListener = new IDCaptureResultListener() {
        @Override
        public void onIdCaptured(HashMap<String, String> documentMap, String documentType,
                                 byte[] frontImageBytes, byte[] backImageBytes,
                                 int numImages, boolean hasFace, int faceSide) {
            mResult = documentMap;
            mDocumentType = documentType;
            mFrontImageBytes = frontImageBytes;
            mBackImageBytes = backImageBytes;
            addScanMetrics(numImages, hasFace, faceSide);
            showDocumentUpload();
        }

        @Override
        public void onIdCaptureCancelled() {
            handleListenerResultCallback(null);
            requireActivity().getSupportFragmentManager().popBackStack();
        }

        @Override
        public void onIdCaptureError(String code, String message) {
            handleListenerErrorCallback(
                    new DocumentSubmissionError.DocumentCaptureError(code, message));
        }
    };

    private void checkDocumentUpload() {
        mRootBinding.container.setVisibility(View.GONE);
        mCheckBinding.rootView.setVisibility(View.VISIBLE);
        if (mFrontImageBytes != null) {
            mCheckBinding.ivDocumentFront.setImageBitmap(
                    BitmapFactory.decodeByteArray(mFrontImageBytes, 0, mFrontImageBytes.length));
        }
        if (mBackImageBytes != null) {
            mCheckBinding.ivDocumentBack.setVisibility(View.VISIBLE);
            mCheckBinding.ivDocumentBack.setImageBitmap(
                    BitmapFactory.decodeByteArray(mBackImageBytes, 0, mBackImageBytes.length));
        }
    }

    private void showDocumentUpload() {
        checkDocumentUpload();
    }

    private void restartCapture() {
        if (mCoordinator != null && getActivity() != null) {
            mCoordinator.captureGovernmentId(getActivity());
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootBinding = DialogIdcaptureBinding.inflate(inflater, container, false);
        setupConfirmUploadView();
        addIdCaptureMetrics();
        return mRootBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mResult != null) showDocumentUpload();
    }

    private void setupConfirmUploadView() {
        mCheckBinding = mRootBinding.uploadCheck;
        mCheckBinding.setTheme(mAppTheme);
        mCheckBinding.setLanguageProvider(mLanguageProvider);
        mCheckBinding.btnCheckConfirm.setOnClickListener(v -> {
            if (skipClickEvent()) return;
            DocumentClass documentType = mDocumentType != null
                    ? DocumentClass.getEnumValue(mDocumentType) : DocumentClass.OTHER;
            if (documentType == DocumentClass.OTHER) {
                DocumentSelectFragment dialog = DocumentSelectFragment.newInstance(mAppTheme, mLanguageProvider, type -> {
                    if (type == null) return;
                    proceedToSubmitDocument();
                });
                dialog.show(requireActivity().getSupportFragmentManager(), DocumentSelectFragment.class.getName());
                return;
            }
            proceedToSubmitDocument();
        });
        mCheckBinding.btnCancel.setOnClickListener(v -> cancelCurrentCapture());
        mCheckBinding.btnCheckRetry.setOnClickListener(v -> cancelCurrentCapture());
    }

    private void cancelCurrentCapture() {
        if (skipClickEvent()) {
            return;
        }
        restartCapture();
    }

    private void proceedToSubmitDocument() {
        handleListenerResultCallback(mResult);
    }

    private void proceedToSubmitDocument(HashMap<String, String> documentResultMap, DocumentClass docType) {
        handleListenerResultCallback(documentResultMap);
    }

    /** Called by DocumentCapturePresenter to pre-populate the result before showing the confirm screen. */
    public void showResult(Map<String, String> result) {
        mResult = new HashMap<>(result);
        String type = result.get(DocumentKeys.TYPE.toString());
        if (type != null) mDocumentType = type;
        String frontBase64 = result.get(DocumentKeys.FRONT_IMAGE.toString());
        if (frontBase64 != null) {
            try { mFrontImageBytes = Base64.decode(frontBase64, Base64.NO_WRAP); } catch (Exception ignored) {}
        }
        String backBase64 = result.get(DocumentKeys.BACK_IMAGE.toString());
        if (backBase64 != null) {
            try { mBackImageBytes = Base64.decode(backBase64, Base64.NO_WRAP); } catch (Exception ignored) {}
        }
        // View may not be inflated yet if called before onCreateView — defer to onViewCreated.
        if (mRootBinding != null) showDocumentUpload();
    }

    private void handleListenerResultCallback(@Nullable Map<String, String> documentResultMap) {
        if (documentResultMap != null) {
            Log.d(TAG, "Document captured successfully.");
            mOnConfirm.accept(documentResultMap);
        } else {
            Log.d(TAG, "Document captured cancelled.");
            mCoordinator.endVerification();
        }
    }

    private void handleListenerErrorCallback(DocumentSubmissionError.DocumentCaptureError error) {
        if (mCoordinator instanceof CaptureResultReceiver) {
            ((CaptureResultReceiver) mCoordinator).captureError(error);
        }
    }

    private void addScanMetrics(int numImages, boolean hasFace, int faceSide) {
        if (mDocumentType != null && mDocumentType.equalsIgnoreCase(DocumentClass.PASSPORT.toString())) {
            // TODO: analytics — core concern, remove from ui
            // AppEventStorageImpl.getInstance().addAppEvent(
                    // TODO: analytics — core concern, remove from ui
                    // new AppEvent(ID_CAPTURE_MRZ_CAPTURED, DateUtil.getTimestamp()), ID_CAPTURE);
        } else if (mDocumentType != null && mDocumentType.equalsIgnoreCase(DocumentClass.DRIVER_LICENSE.toString())) {
            // TODO: analytics — core concern, remove from ui
            // AppEventStorageImpl.getInstance().addAppEvent(
                    // TODO: analytics — core concern, remove from ui
                    // new AppEvent(ID_CAPTURE_BARCODE_CAPTURED, DateUtil.getTimestamp()), ID_CAPTURE);
        }

        if (mFrontImageBytes != null) {
            Bitmap front = BitmapFactory.decodeByteArray(mFrontImageBytes, 0, mFrontImageBytes.length);
            sendImageMetadata(front, "1");
        }
        if (mBackImageBytes != null) {
            Bitmap back = BitmapFactory.decodeByteArray(mBackImageBytes, 0, mBackImageBytes.length);
            sendImageMetadata(back, "2");
        }

        if (hasFace) {
            // TODO: analytics — core concern, remove from ui
            // AppEventStorageImpl.getInstance().addAppEvent(
                    // TODO: analytics — core concern, remove from ui
                    // new AppEvent(String.format(ID_CAPTURE_IMAGE_HAS_FACE, faceSide), "TRUE"), ID_CAPTURE);
        }

        String numberOfImages = String.valueOf(numImages);
        List<AppEvent> events = new ArrayList<>();
        // TODO: analytics — core concern, remove from ui
        // events.add(new AppEvent(ID_CAPTURE_TOTAL_IMAGES, numberOfImages));
        // TODO: analytics — core concern, remove from ui
        // events.add(new AppEvent(String.format(ID_CAPTURE_PAGE_CAPTURED, numberOfImages), DateUtil.getTimestamp()));
        // TODO: analytics — core concern, remove from ui
        // events.add(new AppEvent(ID_CAPTURE_CLASSIFICATION, mDocumentType != null ? mDocumentType : ""));
        // TODO: analytics — core concern, remove from ui
        // events.add(new AppEvent(ID_CAPTURE_STOP, DateUtil.getTimestamp()));
        // TODO: analytics — core concern, remove from ui
        // events.add(new AppEvent(DEVICE_METADATA_SDK_VERSION, Constants.BLINK_ID_SDK_VERSION));
        // TODO: analytics — core concern, remove from ui
        // AppEventStorageImpl.getInstance().addAppEvents(events, ID_CAPTURE);
    }

    private void addIdCaptureMetrics() {
        List<AppEvent> metrics = new ArrayList<>();
        // TODO: analytics — core concern, remove from ui
        // metrics.add(new AppEvent(ID_CAPTURE_VENDOR, CAPTURE_VENDOR));
        // TODO: analytics — core concern, remove from ui
        // metrics.add(new AppEvent(ID_CAPTURE_START, DateUtil.getTimestamp()));
        // TODO: analytics — core concern, remove from ui
        // AppEventStorageImpl.getInstance().addAppEvents(metrics, ID_CAPTURE);
    }

    private void sendImageMetadata(Bitmap bitmap, String page) {
        List<AppEvent> metrics = new ArrayList<>();
        // TODO: analytics — core concern, remove from ui
        // metrics.add(new AppEvent(String.format(ID_CAPTURE_IMAGE_SIZE, page), bitmap.getWidth() + " x " + bitmap.getHeight()));
        // TODO: analytics — core concern, remove from ui
        // metrics.add(new AppEvent(String.format(ID_CAPTURE_PAGE_CAPTURED, page), DateUtil.getTimestamp()));
        // TODO: analytics — core concern, remove from ui
        // AppEventStorageImpl.getInstance().addAppEvents(metrics, ID_CAPTURE);
    }
}
