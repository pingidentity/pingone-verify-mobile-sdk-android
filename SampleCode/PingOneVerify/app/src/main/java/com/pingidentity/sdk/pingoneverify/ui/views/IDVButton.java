package com.pingidentity.sdk.pingoneverify.ui.views;

import android.content.Context;
import androidx.appcompat.widget.AppCompatButton;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.databinding.BindingAdapter;

import com.pingidentity.sdk.pingoneverify.R;

public class IDVButton extends AppCompatButton {
    public IDVButton(Context context) {
        super(context);
    }

    public IDVButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public IDVButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(@Nullable AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.IDVButton);
            int back = a.getColor(R.styleable.IDVButton_background_appearance, 0);
            int border = a.getColor(R.styleable.IDVButton_border_appearance, 0);
            int color = a.getColor(R.styleable.IDVButton_text_appearance, 0);
            float cornerRadius = a.getDimension(R.styleable.IDVButton_corner_radius, 100f);
            super.setBackground(getDrawable(back, border, cornerRadius));
            super.setTextColor(color);
            super.setStateListAnimator(null);
            //^This removes the shadow from buttons
            a.recycle();
        }
    }

    private Drawable getDrawable(int back, int border, float radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(radius);
        drawable.setStroke(2, border);
        drawable.setColor(back);
        return drawable;
    }

    @BindingAdapter("app:border_appearance")
    public static void setBorderAppearance(IDVButton idvButton, int color) {
        GradientDrawable drawable = (GradientDrawable) idvButton.getBackground().mutate();
        drawable.setStroke(2, color);
    }

    @BindingAdapter("app:background_appearance")
    public static void setBackgroundAppearance(IDVButton idvButton, int color) {
        GradientDrawable drawable = (GradientDrawable) idvButton.getBackground().mutate();
        drawable.setColor(color);
    }

    @BindingAdapter("app:text_appearance")
    public static void setTextAppearance(IDVButton idvButton, int color) {
        idvButton.setTextColor(color);
    }

    @BindingAdapter("app:corner_radius")
    public static void setCornerRadius(IDVButton idvButton, float cornerRadius) {
        GradientDrawable drawable = (GradientDrawable) idvButton.getBackground().mutate();
        drawable.setCornerRadius(cornerRadius);
    }

    public void setButtonEnabled(boolean isEnabled) {
        setAlpha(isEnabled ? 1f : .5f);
        setEnabled(isEnabled);
    }

}
