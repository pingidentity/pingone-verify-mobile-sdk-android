package com.pingidentity.sample.P1VerifyApp.fragments.qrscanner;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.fragment.app.DialogFragment;

import com.pingidentity.sample.P1VerifyApp.databinding.ManualCodeDialogBinding;

public class ManualCodeDialog extends DialogFragment {

    private static final int MAX_CODE_LENGTH = 12;

    public static final String DIALOG_RESULT_EXTRA = "result";
    public static final int DIALOG_RESULT_CODE = 1;

    private ManualCodeDialogBinding binding;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = ManualCodeDialogBinding.inflate(inflater, container, false);
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        binding.btnVerify.setOnClickListener(v -> {
            getTargetFragment().onActivityResult(getTargetRequestCode(), DIALOG_RESULT_CODE,
                    new Intent().putExtra(DIALOG_RESULT_EXTRA, binding.edtCode.getText().toString()));
            dismiss();
        });

        binding.edtCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.btnVerify.setEnabled(s.length() == MAX_CODE_LENGTH);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.btnCancel.setOnClickListener(v -> dismiss());

        return binding.getRoot();
    }

    public void onResume() {
        super.onResume();
        Window window = getDialog().getWindow();
        Point size = new Point();
        Display display = window.getWindowManager().getDefaultDisplay();
        display.getSize(size);
        int width = size.x;
        window.setLayout((int) (width * 0.9), WindowManager.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);
    }

}