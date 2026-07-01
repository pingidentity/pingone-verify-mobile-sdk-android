package com.pingidentity.sdk.pingoneverify.sample;

import android.app.AlertDialog;
import com.pingidentity.sdk.pingoneverify.neo.errors.DocumentSubmissionError;
import com.pingidentity.sdk.pingoneverify.ui.providers.VerifyHelperCallback;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.pingidentity.sdk.pingoneverify.sample.fragments.CompletedFragment;
import com.pingidentity.sdk.pingoneverify.sample.fragments.MainFragment;

import java.util.List;
import java.util.stream.Collectors;

public class MainActivity extends FragmentActivity implements VerifyHelperCallback {

    public static final String TAG = MainActivity.class.getName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        moveToMainFragment();
    }

    @Override
    public void onBackPressed() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments().stream()
                .filter(fragment -> fragment != null && fragment.isVisible())
                .collect(Collectors.toList());
        if (fragments.size() == 1) {
            if (fragments.get(0) instanceof CompletedFragment) {
                moveToMainFragment();
            } else {
                finishAffinity();
            }
        } else {
            super.onBackPressed();
        }
    }

    public void moveToMainFragment() {
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new MainFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onVerificationCompleted() {
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new CompletedFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onVerificationFailed(DocumentSubmissionError error) {
        moveToMainFragment();
        if (error instanceof DocumentSubmissionError.UserCanceledError) {
            // Skip showing error
        } else {
            showError((error != null) ? error.getLocalizedMessage() : null);
        }
    }

    public void showError(@Nullable String message) {
        String errorTitle = "Verification Failed";
        runOnUiThread(() -> new AlertDialog.Builder(this)
                .setTitle(errorTitle)
                .setMessage((message != null) ? errorTitle : errorTitle)
                .setPositiveButton(android.R.string.ok, (d, w) -> moveToMainFragment())
                .show());
    }

}