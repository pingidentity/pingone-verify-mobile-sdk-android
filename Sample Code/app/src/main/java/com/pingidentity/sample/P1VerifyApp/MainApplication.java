package com.pingidentity.sample.P1VerifyApp;

import android.app.Application;

import com.pingidentity.sample.P1VerifyApp.di.Injector;

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public void initDagger() {
        Injector.initializeAppComponent(this);
        Injector.getAppComponent().inject(this);
    }

}
