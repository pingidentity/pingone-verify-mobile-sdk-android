package com.pingidentity.sdk.pingoneverify.sample;

import android.content.pm.ActivityInfo;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import java.util.List;
import java.util.stream.Collectors;

public class MainActivity extends FragmentActivity {

    public static final String TAG = MainActivity.class.getName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        moveToMainFragment();
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
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
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                    setEnabled(true);
                }
            }
        });
    }

    private void moveToMainFragment() {
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new MainFragment())
                .addToBackStack(null)
                .commit();
    }
}
