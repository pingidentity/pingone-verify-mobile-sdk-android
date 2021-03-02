package com.pingidentity.sample.P1VerifyApp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.pingidentity.sample.P1VerifyApp.MainApplication;
import com.pingidentity.sample.P1VerifyApp.R;
import com.pingidentity.sample.P1VerifyApp.di.Injector;

import com.pingidentity.sample.P1VerifyApp.storage.CardRepository;
import com.pingidentity.sample.P1VerifyApp.storage.SecureStorageManager;

import javax.inject.Inject;

public class SplashActivity extends AppCompatActivity {

    @Inject
    CardRepository mRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        SecureStorageManager.initialize(() -> {
                    ((MainApplication) getApplication()).initDagger();
                    Injector.getAppComponent().inject(this);
                    init();
                },
                error -> {
                    Toast.makeText(this, "Failed to initialize Storage", Toast.LENGTH_SHORT).show();
                    Log.e("StorageManagerInit", "Error initializing StorageManager.", error);
                });
    }

    private void init() {
        if (mRepository.getUserAuthorized() && (mRepository.getIdCard() != null || mRepository.getDriverLicense() != null || mRepository.getPassport() != null)) {
            startActivity(HomeActivity.class);
        } else {
            startActivity(WizardActivity.class);
        }
    }

    private void startActivity(Class<?> activity) {
        Intent myIntent = new Intent(this, activity);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(myIntent);
    }
}