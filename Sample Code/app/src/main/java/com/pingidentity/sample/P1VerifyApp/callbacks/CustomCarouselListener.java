package com.pingidentity.sample.P1VerifyApp.callbacks;

import android.view.View;

import androidx.annotation.NonNull;

import com.azoft.carousellayoutmanager.CarouselLayoutManager;
import com.azoft.carousellayoutmanager.ItemTransformation;

public class CustomCarouselListener implements CarouselLayoutManager.PostLayoutListener {

    public CustomCarouselListener() {}

    @Override
    public ItemTransformation transformChild(@NonNull View child, float itemPositionToCenterDiff, int orientation) {
        float absoluteCenterDiff = Math.abs(itemPositionToCenterDiff);
        double angleValue = StrictMath.atan(absoluteCenterDiff);

        final float scale;
        final float translateX;
        final float translateY;
        if (CarouselLayoutManager.VERTICAL == orientation) {
            scale = (float) (-0.5 * angleValue / Math.PI + 1);
            translateX = 0;
            final float translateYGeneral = child.getMeasuredHeight() * (1 - scale) / 5f;
            translateY = Math.signum(itemPositionToCenterDiff) * translateYGeneral;
        } else {
            scale = (float) (-0.15 * angleValue / Math.PI + 1);
            translateY = 0;
            final float translateXGeneral = child.getMeasuredWidth() * (1 - scale) * 3.0f;
            translateX = Math.signum(itemPositionToCenterDiff) * translateXGeneral;
        }
        return new ItemTransformation(scale, scale, translateX, translateY);
    }

}