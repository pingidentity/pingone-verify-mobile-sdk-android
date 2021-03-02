package com.pingidentity.sample.P1VerifyApp.storage;


import com.pingidentity.p1verifyidschema.DriverLicense;
import com.pingidentity.p1verifyidschema.IdCard;
import com.pingidentity.p1verifyidschema.Passport;
import com.pingidentity.p1verifyidschema.Selfie;

public class CardRepository {

    private final SecureStorageManager mSharedPreferences;

    private DriverLicense mDriverLicense;
    private Selfie mIdCard;
    private Passport mPassport;

    public CardRepository(SecureStorageManager sharedPreferences) {
        this.mSharedPreferences = sharedPreferences;
    }

    public void saveDriverLicense(DriverLicense card){
        deleteCard(card);
        mDriverLicense = (DriverLicense) card;
        mSharedPreferences.saveCard(card, DriverLicense.class);
    }

    public void saveIDCard(Selfie card){
        deleteCard(card);
        mIdCard = (Selfie) card;
        mSharedPreferences.saveCard(card, Selfie.class);
    }

    public void savePassport(Passport passport){
        deleteCard(passport);
        mPassport = (Passport) passport;
        mSharedPreferences.saveCard(passport, Passport.class);
    }

    public DriverLicense getDriverLicense() {
        if (mDriverLicense == null) {
            mDriverLicense = mSharedPreferences.getCardByType(DriverLicense.CARD_TYPE_DL, DriverLicense.class);
        }
        return mDriverLicense;
    }

    public Passport getPassport() {
        if (mPassport == null) {
            mPassport = mSharedPreferences.getCardByType(Passport.CARD_TYPE_PASSPORT, Passport.class);
        }
        return mPassport;
    }

    public Selfie getIdCard() {
        if (mIdCard == null) {
            mIdCard = mSharedPreferences.getCardByType(Selfie.CARD_TYPE_SELFIE, Selfie.class);
        }
        return mIdCard;
    }

    public void deleteCard(IdCard card) {
        if (card instanceof DriverLicense) {
            mDriverLicense = null;
        }
        if (card instanceof Selfie) {
            mIdCard = null;
        }
        if (card instanceof Passport) {
            mPassport = null;
        }
        mSharedPreferences.deleteCard(card);
    }

    public void setValidationStatus(boolean isSuccess) {
        mSharedPreferences.setValidationStatus(isSuccess);
    }

    public boolean getValidationStatus() {
        return mSharedPreferences.getValidationStatus();
    }

    public void setUserAuthorized() {
        mSharedPreferences.setAuthorizationStatus(true);
    }

    public boolean getUserAuthorized() {
        return mSharedPreferences.getAuthorizationStatus();
    }
}