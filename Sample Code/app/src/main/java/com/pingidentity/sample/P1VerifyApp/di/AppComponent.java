package com.pingidentity.sample.P1VerifyApp.di;

import com.pingidentity.sample.P1VerifyApp.MainApplication;
import com.pingidentity.sample.P1VerifyApp.activities.SplashActivity;
import com.pingidentity.sample.P1VerifyApp.di.modules.AppModule;
import com.pingidentity.sample.P1VerifyApp.di.modules.RepositoryModule;
import com.pingidentity.sample.P1VerifyApp.fragments.details.DetailsPresenter;
import com.pingidentity.sample.P1VerifyApp.fragments.home.HomePresenter;
import com.pingidentity.sample.P1VerifyApp.fragments.wizard.WizardPresenter;
import com.pingidentity.sample.P1VerifyApp.storage.CardRepository;
import com.pingidentity.sample.P1VerifyApp.utils.CaptureUtil;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {AppModule.class, RepositoryModule.class})
public interface AppComponent {

    void inject(MainApplication application);

    void inject(SplashActivity splashActivity);

    void inject(CardRepository cardRepository);

    void inject(WizardPresenter wizardPresenter);

    void inject(HomePresenter homePresenter);

    void inject(CaptureUtil captureUtil);

    void inject(DetailsPresenter detailsPresenter);

}
