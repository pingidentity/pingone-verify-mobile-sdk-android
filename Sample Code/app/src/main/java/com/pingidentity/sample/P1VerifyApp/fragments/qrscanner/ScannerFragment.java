package com.pingidentity.sample.P1VerifyApp.fragments.qrscanner;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.util.Size;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.pingidentity.did.sdk.idvalidation.utils.BackgroundThreadHandler;
import com.pingidentity.sample.P1VerifyApp.R;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.pingidentity.sample.P1VerifyApp.callbacks.QRClickListener;
import com.pingidentity.sample.P1VerifyApp.databinding.FragmentScannerBinding;
import com.shocard.sholib.camera.CameraWrapper;
import com.shocard.sholib.camera.QRGraphic;

import static com.pingidentity.sample.P1VerifyApp.fragments.qrscanner.ManualCodeDialog.DIALOG_RESULT_CODE;
import static com.pingidentity.sample.P1VerifyApp.fragments.qrscanner.ManualCodeDialog.DIALOG_RESULT_EXTRA;


public class ScannerFragment extends Fragment implements CameraWrapper.CameraWrapperListener {

    private static final int RESULT_CODE = 548;
    private static  final String DIALOG_TAG = "dlg1";

    private QRClickListener mCallback;

    private BarcodeDetector qrDetector;

    private FragmentScannerBinding binding;

    public ScannerFragment() {
        // Required empty public constructor
    }

    public void setupListener(QRClickListener callback) {
        this.mCallback = callback;
    }

    ///////////////////////////////////////
    ///////////// Lifecycle ///////////////
    ///////////////////////////////////////

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentScannerBinding.inflate(inflater, container, false);
        qrDetector = getBarcodeDetector();

        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();

        setupClickListeners();

        if (qrDetector == null) {
            showAlert(getString(R.string.scanner_error), getString(R.string.scanner_error_message), () -> getParentFragmentManager().popBackStack());
            return binding.getRoot();
        }

        QRGraphic qrGraphic = new QRGraphic(binding.graphicOverlay);
        binding.graphicOverlay.clear();
        binding.graphicOverlay.add(qrGraphic);

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.cameraWrapper.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.cameraWrapper.stopCamera();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_CODE && resultCode == DIALOG_RESULT_CODE) {
            String code = data.getExtras().getString(DIALOG_RESULT_EXTRA);
            mCallback.onCodeResult(code);
            getParentFragmentManager().popBackStack();
        }
    }

    ///////////////////////////////////////
    ///////////// Camera work /////////////
    ///////////////////////////////////////

    @Override
    public void onPictureTaken(Bitmap bitmap) {

    }

    @Override
    public void onFrameReceived(byte[] frameData, Size imageSize, int rotation) {
        final Bitmap frameImage = CameraWrapper.getImageFromFrame(frameData, imageSize, rotation);
        if (frameImage != null && qrDetector != null) {
            Frame frame = new Frame.Builder().setBitmap(frameImage).build();
            SparseArray<Barcode> barcodes = qrDetector.detect(frame);
            if (barcodes.size() > 0) {
                binding.cameraWrapper.post(() -> binding.cameraWrapper.stopCamera());
                final Barcode qrCode = barcodes.valueAt(0);
                processQR(qrCode);
            }
        }
    }

    @Override
    public void onCameraError(Exception e) {

    }

    ///////////////////////////////////////
    ///////////// Private methods /////////
    ///////////////////////////////////////

    private void setupClickListeners() {
        binding.cameraWrapper.setListener(this);
        binding.btnEnterKey.setOnClickListener(v -> openManualCodeDialog());
        binding.toolbar.toolbarCancel.setOnClickListener(v -> getParentFragmentManager().popBackStack());
    }

    private void processQR(final Barcode qrCode) {
        BackgroundThreadHandler.postOnMainThread(() -> {
            if (mCallback != null) {
                mCallback.onUrlResult(qrCode.rawValue);
            }
            getActivity().getSupportFragmentManager().popBackStack();
        });
    }

    private BarcodeDetector getBarcodeDetector() {
        final BarcodeDetector.Builder barcodeDetectorBuilder = new BarcodeDetector.Builder(getActivity());
        barcodeDetectorBuilder.setBarcodeFormats(Barcode.QR_CODE);
        final BarcodeDetector barcodeDetector = barcodeDetectorBuilder.build();
        if (!barcodeDetector.isOperational()) {
            IntentFilter lowStorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = getActivity().registerReceiver(null, lowStorageFilter) != null;
            if (hasLowStorage) {
                Toast.makeText(getActivity(), R.string.low_storage_error, Toast.LENGTH_LONG).show();
            }
            return null;
        }
        return barcodeDetector;
    }

    private void openManualCodeDialog() {
        ManualCodeDialog dialog = new ManualCodeDialog();
        dialog.setTargetFragment(this, RESULT_CODE);
        dialog.show(getFragmentManager(), DIALOG_TAG);
    }

    private void showAlert(String title, String message, final Runnable answer) {
        new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.home_continue, (dialog, which) -> {
                    answer.run();
                    dialog.dismiss();
                })
                .show();
    }

}