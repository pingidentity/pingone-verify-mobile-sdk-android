package com.pingidentity.sample.P1VerifyApp.di.modules;

import com.pingidentity.sample.P1VerifyApp.storage.CardRepository;
import com.pingidentity.sample.P1VerifyApp.storage.SecureStorageManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class RepositoryModule {

    public RepositoryModule() {
    }

    @Provides
    @Singleton
    SecureStorageManager providesSharedPreferencesManager() {
        return SecureStorageManager.sharedInstance;
    }

    @Provides
    @Singleton
    CardRepository providesApplication(SecureStorageManager secureStorageManager) {
        return new CardRepository(secureStorageManager);
    }
}