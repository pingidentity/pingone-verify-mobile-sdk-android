package com.pingidentity.sample.P1VerifyApp.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.pingidentity.sample.P1VerifyApp.databinding.ViewDetailBinding;


public class DetailView extends LinearLayout {

    public DetailView(Context context, String param, String description, boolean underline) {
        super(context);
        init(context, param, description, underline);
    }

    public DetailView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DetailView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void init(Context context, String param, String description, boolean underline) {
        ViewDetailBinding mBinding = ViewDetailBinding.inflate(LayoutInflater.from(context), this, true);

        mBinding.txtParamName.setText(param);
        mBinding.txtInfo.setText(description);

        if (!underline) {
            mBinding.layoutContent.setPadding(0, 8, 0, 8);
            mBinding.viewDivider.setVisibility(GONE);
        }

    }

}