package com.pingidentity.sample.P1VerifyApp.di;


import com.pingidentity.sample.P1VerifyApp.MainApplication;
import com.pingidentity.sample.P1VerifyApp.di.modules.AppModule;
import com.pingidentity.sample.P1VerifyApp.di.modules.RepositoryModule;

public class Injector {

    private static AppComponent mAppComponent;

    private Injector() {

    }

    public static void initializeAppComponent(MainApplication application) {
        mAppComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(application))
                .repositoryModule(new RepositoryModule())
                .build();
    }

    public static AppComponent getAppComponent(){
        return mAppComponent;
    }

}
