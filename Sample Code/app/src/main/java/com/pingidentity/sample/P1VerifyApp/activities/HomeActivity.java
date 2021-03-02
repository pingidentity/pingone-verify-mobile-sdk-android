package com.pingidentity.sample.P1VerifyApp.activities;

import android.os.Bundle;

import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;
import com.pingidentity.sample.P1VerifyApp.R;
import com.pingidentity.sample.P1VerifyApp.databinding.ActivityHomeBinding;
import com.pingidentity.sample.P1VerifyApp.fragments.home.HomeFragment;
import com.pingidentity.sample.P1VerifyApp.utils.IdvHelper;

import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    ActivityHomeBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        AppCenter.start(getApplication(), "c1ca8e1a-cf98-4caa-9836-6256c5fb2b3b",
                Analytics.class, Crashes.class);

        IdvHelper.getInstance().updateLifeCycleOwner(this);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();
    }

}