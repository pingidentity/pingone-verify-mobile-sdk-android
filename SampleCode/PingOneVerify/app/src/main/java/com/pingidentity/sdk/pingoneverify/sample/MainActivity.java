package com.pingidentity.sdk.pingoneverify.sample;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.pingidentity.sdk.pingoneverify.sample.fragments.CompletedFragment;
import com.pingidentity.sdk.pingoneverify.sample.fragments.MainFragment;

import java.util.List;
import java.util.stream.Collectors;

public class MainActivity extends FragmentActivity {

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

    public void showCompletedScreen() {
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new CompletedFragment())
                .addToBackStack(null)
                .commit();
    }

    public void showError(String message) {
        runOnUiThread(() -> new AlertDialog.Builder(this)
                .setTitle("Verification Failed")
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, (d, w) -> moveToMainFragment())
                .show());
    }
}