package com.pingidentity.sdk.pingoneverify.sample.qr_scanner;

import static com.pingidentity.sdk.pingoneverify.analytics.constants.AppEventConstants.CAMERA_CONFIG_RESOLUTION;
import static com.pingidentity.sdk.pingoneverify.analytics.constants.AppEventConstants.CAMERA_PERMISSION_GRANTED;
import static com.pingidentity.sdk.pingoneverify.analytics.constants.AppEventConstants.QR_SCANNER_CANCELLED;
import static com.pingidentity.sdk.pingoneverify.analytics.constants.AppEventConstants.QR_SCANNER_START;
import static com.pingidentity.sdk.pingoneverify.analytics.constants.AppEventConstants.QR_SCANNER_STOP;
import static com.pingidentity.sdk.pingoneverify.analytics.constants.AppEventType.CAMERA;
import static com.pingidentity.sdk.pingoneverify.analytics.constants.AppEventType.QR_SCANNER;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.camera2.CameraAccessException;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.pingidentity.sdk.pingoneverify.analytics.models.AppEvent;
import com.pingidentity.sdk.pingoneverify.analytics.storage.AppEventStorageImpl;
import com.pingidentity.sdk.pingoneverify.neo.listeners.PermissionListener;
import com.pingidentity.sdk.pingoneverify.sample.databinding.DialogQrScannerBinding;
import com.pingidentity.sdk.pingoneverify.ui.fragments.BaseFragment;
import com.pingidentity.sdk.pingoneverify.utils.DateUtil;

import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class QrScannerDialog extends BaseFragment {

    public interface Listener {
        void onQrScanned(String verificationUrl);
        void onQrCanceled();
    }

    public static final String TAG = QrScannerDialog.class.getName();

    private static final long TIMER_DELAY = 800;

    private float scaleX = 1f;
    private float scaleY = 1f;

    private ListenableFuture<ProcessCameraProvider> mCameraProviderFuture;
    private BarcodeBoxView mBarcodeBoxView;
    private DialogQrScannerBinding mBinding;
    private Listener mListener;

    public QrScannerDialog() {
        super(null);
    }

    public static QrScannerDialog newInstance(Listener listener) {
        QrScannerDialog dialogFragment = new QrScannerDialog();
        dialogFragment.mListener = listener;
        return dialogFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        AppEventStorageImpl.getInstance().addAppEvent(new AppEvent(QR_SCANNER_START, DateUtil.getTimestamp()), QR_SCANNER);
        mBinding = DialogQrScannerBinding.inflate(inflater, container, false);
        mBarcodeBoxView = new BarcodeBoxView(requireContext());
        mBinding.viewContainer.addView(mBarcodeBoxView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mBinding.btnClose.setOnClickListener(view -> {
            mListener.onQrCanceled();
            AppEventStorageImpl.getInstance().addAppEvent(new AppEvent(QR_SCANNER_CANCELLED, "TRUE"), QR_SCANNER);
            requireActivity().getSupportFragmentManager().popBackStack();
        });
        return mBinding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        checkPermission();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AppEventStorageImpl.getInstance().addAppEvent(new AppEvent(QR_SCANNER_STOP, DateUtil.getTimestamp()), QR_SCANNER);
        try {
            if (mCameraProviderFuture != null) {
                mCameraProviderFuture.get().unbindAll();
            }
        } catch (ExecutionException exception) {
            Log.e(TAG, "Failed to unbind camera", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    private void checkPermission() {
        checkPermission(new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                bindCameraUseCases();
                AppEventStorageImpl.getInstance().addAppEvent(new AppEvent(CAMERA_PERMISSION_GRANTED, "TRUE"), CAMERA);
            }

            @Override
            public void onPermissionDenied() {
                AppEventStorageImpl.getInstance().addAppEvent(new AppEvent(CAMERA_PERMISSION_GRANTED, "FALSE"), CAMERA);
                mListener.onQrCanceled();
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        }, PermissionType.CAMERA);
    }

    /// ///////////////////////////////////////
    /// ////// Camera Works ///////////////////
    /// ///////////////////////////////////////

    private void bindCameraUseCases() {
        mCameraProviderFuture = ProcessCameraProvider.getInstance(requireActivity());
        mCameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = mCameraProviderFuture.get();
                Preview previewUseCase = new Preview.Builder().build();
                ImageAnalysis analysisUseCase = getAnalysisUseCase();
                previewUseCase.setSurfaceProvider(mBinding.previewView.getSurfaceProvider());
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) QrScannerDialog.this, cameraSelector, previewUseCase, analysisUseCase);
                getCameraInfo(camera);
            } catch (Exception exception) {
                Log.e(TAG, "Error getting camera", exception);
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private ImageAnalysis getAnalysisUseCase() {
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build();
        BarcodeScanner scanner = BarcodeScanning.getClient(options);
        ImageAnalysis analysisUseCase = new ImageAnalysis.Builder().build();
        analysisUseCase.setAnalyzer(
                Executors.newSingleThreadExecutor(),
                imageProxy -> processImageProxy(scanner, imageProxy, analysisUseCase)
        );
        return analysisUseCase;
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void processImageProxy(BarcodeScanner barcodeScanner, ImageProxy imageProxy, ImageAnalysis analysisUseCase) {
        if (imageProxy.getImage() == null) {
            return;
        }
        Image image = imageProxy.getImage();
        InputImage inputImage = InputImage.fromMediaImage(image, imageProxy.getImageInfo().getRotationDegrees());
        barcodeScanner.process(inputImage)
                .addOnSuccessListener(barcodes -> processBarcode(barcodes, analysisUseCase, inputImage))
                .addOnFailureListener(exception -> Log.e(TAG, "Barcode scanner failed", exception))
                .addOnCompleteListener(task -> {
                    imageProxy.getImage().close();
                    imageProxy.close();
                });
    }

    /// ///////////////////////////////////////
    /// ////// Process Barcode ////////////////
    /// ///////////////////////////////////////

    private void processBarcode(List<Barcode> barcodes, ImageAnalysis analysisUseCase, InputImage inputImage) {
        if (barcodes == null || barcodes.isEmpty()) {
            mBarcodeBoxView.setRect(new RectF());
            return;
        }
        Barcode barcode = barcodes.get(0);
        if (barcode != null && barcode.getRawValue() != null) {
            scaleX = mBinding.previewView.getWidth() / (float) inputImage.getHeight();
            scaleY = mBinding.previewView.getHeight() / (float) inputImage.getWidth();
            mBarcodeBoxView.setRect(adjustBoundingRect(Objects.requireNonNull(barcode.getBoundingBox())));
            try {
                mCameraProviderFuture.get().unbind(analysisUseCase);
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        requireActivity().runOnUiThread(() -> {
                            mListener.onQrScanned(barcode.getRawValue());
                            requireActivity().getSupportFragmentManager().popBackStack();
                        });
                    }
                }, TIMER_DELAY);
            } catch (ExecutionException exception) {
                Log.e(TAG, "Failed to unbind camera in QR Scanner", exception);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /// ///////////////////////////////////////
    /// ////// QR Code frame //////////////////
    /// ///////////////////////////////////////

    private RectF adjustBoundingRect(Rect rect) {
        return new RectF(
                translateX((float) rect.left),
                translateY((float) rect.top),
                translateX((float) rect.right),
                translateY((float) rect.bottom)
        );
    }

    private Float translateX(Float x) {
        return x * scaleX;
    }

    private Float translateY(Float y) {
        return y * scaleY;
    }

    /// ///////////////////////////////////////
    /// ////// Metrics work ///////////////////
    /// ///////////////////////////////////////

    private void getCameraInfo(Camera camera) {
        try {
            String resolution = CameraUtil.getBackCameraResolution(camera, requireContext());
            AppEventStorageImpl.getInstance().addAppEvent(new AppEvent(CAMERA_CONFIG_RESOLUTION, resolution), CAMERA);
        } catch (CameraAccessException exception) {
            Log.e(TAG, "Failed to access camera", exception);
        }
    }

}
