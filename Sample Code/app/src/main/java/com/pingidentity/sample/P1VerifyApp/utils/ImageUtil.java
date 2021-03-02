package com.pingidentity.sample.P1VerifyApp.utils;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.media.ThumbnailUtils;

import androidx.annotation.NonNull;


public class ImageUtil {

    public static Bitmap resize(@NonNull final Bitmap bitmap, final int maxSide) {
        final int biggestSide = Math.max(bitmap.getHeight(), bitmap.getWidth());
        final float scaleFactor = (float) biggestSide / (float) maxSide;
        return resize(bitmap, scaleFactor);
    }

    public static Bitmap resize(@NonNull final Bitmap bitmap, @NonNull final float scaleFactor) {
        final int newWidth = (int) (bitmap.getWidth() / scaleFactor);
        final int newHeight = (int) (bitmap.getHeight() / scaleFactor);
        return ThumbnailUtils.extractThumbnail(bitmap, newWidth, newHeight);
    }

    public static Rect translateRect(@NonNull final Rect originalRect, final float scaleFactor, final int padding) {
        final int newLeft = (int) ((float) originalRect.left / scaleFactor) - padding;
        final int newTop = (int) ((float) originalRect.top / scaleFactor) - padding;
        final int newRight = (int) ((float) originalRect.right / scaleFactor) + padding;
        final int newBottom = (int) ((float) originalRect.bottom / scaleFactor) + padding;

        return new Rect(newLeft, newTop, newRight, newBottom);
    }

}
