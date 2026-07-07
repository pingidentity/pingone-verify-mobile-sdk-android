package com.pingidentity.sdk.pingoneverify.sample.qr_scanner;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.util.Size;

import androidx.camera.camera2.interop.Camera2CameraInfo;
import androidx.camera.core.Camera;

public class CameraUtil {

    private CameraUtil() {
        throw new IllegalStateException("Utility class");
    }

    @SuppressLint("UnsafeOptInUsageError")
    public static String getBackCameraResolution(Camera camera, Context context) throws CameraAccessException {
        String cameraId = Camera2CameraInfo.from(camera.getCameraInfo()).getCameraId();
        CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);

        StreamConfigurationMap configs = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        Size[] previewSizes = configs.getOutputSizes(SurfaceTexture.class);

        String maxResolution = null;
        long pixelCount = -1;

        for (Size previewSize : previewSizes) {
            long pixelCountTemp = (long) previewSize.getWidth() * previewSize.getHeight();
            if (pixelCountTemp > pixelCount) {
                pixelCount = pixelCountTemp;
                maxResolution = previewSize.getWidth() + " x " + previewSize.getHeight();
            }
        }

        return maxResolution;
    }

}
