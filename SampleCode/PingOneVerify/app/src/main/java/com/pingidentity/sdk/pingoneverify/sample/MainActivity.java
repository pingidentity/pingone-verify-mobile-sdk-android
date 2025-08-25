package com.pingidentity.sdk.pingoneverify.sample;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

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

    private void moveToMainFragment() {
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new MainFragment())
                .addToBackStack(null)
                .commit();
    }
}