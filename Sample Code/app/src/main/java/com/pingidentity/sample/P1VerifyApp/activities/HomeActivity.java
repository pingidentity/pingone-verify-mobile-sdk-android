package com.pingidentity.sample.P1VerifyApp.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.pingidentity.sample.P1VerifyApp.R;
import com.pingidentity.sample.P1VerifyApp.databinding.ActivityHomeBinding;
import com.pingidentity.sample.P1VerifyApp.fragments.home.HomeFragment;
import com.pingidentity.sample.P1VerifyApp.utils.IdvHelper;

public class HomeActivity extends AppCompatActivity {

    ActivityHomeBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        IdvHelper.getInstance().updateLifeCycleOwner(this);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();
    }

}