package com.pingidentity.sample.P1VerifyApp.storage;

import androidx.annotation.NonNull;

import com.pingidentity.p1verifyidschema.IdCard;

import java.lang.reflect.Type;

public interface SecureStorage{

    <T extends IdCard> T getCardByType(String name, Class<T> type);

    void deleteCard(@NonNull final IdCard idCard);

    void saveCard(@NonNull final IdCard idCard, @NonNull final Type cardType);

    void setValidationStatus(boolean isSuccess);

    boolean getValidationStatus();

    void setAuthorizationStatus(boolean isSuccess);

    boolean getAuthorizationStatus();
}
