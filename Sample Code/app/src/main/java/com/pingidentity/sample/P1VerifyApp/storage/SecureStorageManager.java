package com.pingidentity.sample.P1VerifyApp.storage;

import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;

import com.pingidentity.did.sdk.idvalidation.utils.BackgroundThreadHandler;
import com.pingidentity.did.sdk.idvalidation.utils.EncryptedStorage;

import com.pingidentity.did.sdk.idvalidation.utils.P1VerifyContextProvider;
import com.pingidentity.did.sdk.idvalidation.utils.json.JsonUtils;

import com.pingidentity.p1verifyidschema.DriverLicense;
import com.pingidentity.p1verifyidschema.IdCard;
import com.pingidentity.p1verifyidschema.Passport;
import com.pingidentity.p1verifyidschema.Selfie;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


public class SecureStorageManager implements SecureStorage {

    private static final String PREF_FILE_NAME = "p1verify_test";
    public static final String TAG = SecureStorageManager.class.getCanonicalName();

    static final String CARD_IDS_PREFERENCES_KEY = "card_ids_preferences_key";
    static final String VALIDATION_STATUS_KEY = "validation_status_key";
    static final String AUTHORIZATION_STATUS_KEY = "authorization_status_key";
    static final String CARD_TYPE_PREFERENCES_KEY_SUFFIX = "_card_type";

    public static SecureStorageManager sharedInstance;
    private final SharedPreferences encryptedPreferences;
    private final BackgroundThreadHandler handler = BackgroundThreadHandler.singleBackgroundThreadHandler();

    private final Set<String> cardIds;
    private final Map<String, IdCard> cards = new HashMap<>();

    private static Runnable mResultHandler;

    private SecureStorageManager(@NonNull final SharedPreferences encryptedPreferences) {
        Objects.requireNonNull(encryptedPreferences);
        this.encryptedPreferences = encryptedPreferences;
        this.cardIds = encryptedPreferences.getStringSet(CARD_IDS_PREFERENCES_KEY, new HashSet<>());
        handler.post(this::loadCards);
    }

    public static void initialize(@NonNull final Runnable resultHandler, @NonNull final Consumer<Throwable> errorHandler) {
        Objects.requireNonNull(resultHandler);
        Objects.requireNonNull(errorHandler);
        mResultHandler = resultHandler;
        EncryptedStorage.getPreferences(PREF_FILE_NAME, (sharedPreferences -> sharedInstance = new SecureStorageManager(sharedPreferences)), errorHandler);
    }

    ///////////////////////////////////////
    ///////////// Card work ///////////////
    ///////////////////////////////////////

    private void loadCards() {
        this.cardIds.forEach(cardId -> EncryptedStorage.readFromFile(cardId, fileBytes -> {
            final String cardType = encryptedPreferences.getString(cardId + CARD_TYPE_PREFERENCES_KEY_SUFFIX, null);
            if (cardType == null) {
                return;
            }
            final String cardJson = new String(fileBytes);
            final IdCard card = getCardFromJson(cardJson, cardType);
            if (card != null) {
                cards.put(cardId, card);
            }
        }, error -> Log.d(TAG, "Error loading card with id: " + cardId, error)));
        mResultHandler.run();
    }

    @Override
    public <T extends IdCard> T getCardByType(String name, Class<T> type) {
        for (String cardId : cardIds) {
            if (name.equals(encryptedPreferences.getString(cardId + CARD_TYPE_PREFERENCES_KEY_SUFFIX, null))) {
                return type.cast(cards.get(cardId));
            }
        }
        return null;
    }

    @Override
    public void deleteCard(@NonNull final IdCard idCard) {
        File file = new File(P1VerifyContextProvider.getApplicationContext().getFilesDir(), idCard.getCardId());
        if (file.exists()) {
            if (file.delete()) {
                cards.remove(idCard.getCardId());
                removeCardId(idCard.getCardId());
                Log.d(TAG, "file Deleted :" + idCard.getCardId());
            } else {
                Log.d(TAG, "file not Deleted :" + idCard.getCardId());
            }
        }
    }

    @Override
    public void saveCard(@NonNull final IdCard idCard, @NonNull final Type cardType) {
        final String cardJson = JsonUtils.instance.adapter(cardType).toJson(idCard);
        saveCardIds(idCard.getCardId(), idCard.getCardType());
        cards.put(idCard.getCardId(), idCard);
        handler.post(() -> EncryptedStorage.writeToFile(idCard.getCardId(), cardJson.getBytes(),
                () -> Log.d(TAG, "Card " + idCard.getCardId() + "saved"),
                (error) -> Log.d(TAG, "Error saving card with id: " + idCard.getCardId() + ", cardType: " + idCard.getCardType(), error)));
    }

    @Nullable
    private IdCard getCardFromJson(@NonNull final String cardJson, @NonNull final String cardType) {
        try {
            switch (cardType) {
                case DriverLicense.CARD_TYPE_DL:
                    return JsonUtils.instance.adapter(DriverLicense.class).fromJson(cardJson);
                case Selfie.CARD_TYPE_SELFIE:
                    return JsonUtils.instance.adapter(Selfie.class).fromJson(cardJson);
                case Passport.CARD_TYPE_PASSPORT:
                    return JsonUtils.instance.adapter(Passport.class).fromJson(cardJson);
                default:
                    return null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to parse card of type " + cardType + " from Json.", e);
            return null;
        }
    }

    ///////////////////////////////////////
    ///////////// Validation work /////////
    ///////////////////////////////////////

    @Override
    public void setValidationStatus(boolean isSuccess) {
        final SharedPreferences.Editor editor = encryptedPreferences.edit();
        editor.putBoolean(VALIDATION_STATUS_KEY, isSuccess).apply();
    }

    @Override
    public boolean getValidationStatus() {
        return encryptedPreferences.getBoolean(VALIDATION_STATUS_KEY, false);
    }

    ///////////////////////////////////////
    ///////////// Authorization work //////
    ///////////////////////////////////////

    @Override
    public void setAuthorizationStatus(boolean isSuccess) {
        final SharedPreferences.Editor editor = encryptedPreferences.edit();
        editor.putBoolean(AUTHORIZATION_STATUS_KEY, isSuccess).apply();
    }

    @Override
    public boolean getAuthorizationStatus() {
        return encryptedPreferences.getBoolean(AUTHORIZATION_STATUS_KEY, false);
    }

    ///////////////////////////////////////
    ///////////// Card ID's work //////////
    ///////////////////////////////////////

    private void saveCardIds(@NonNull final String cardId, @NonNull final String cardType) {
        cardIds.add(cardId);
        final SharedPreferences.Editor editor = encryptedPreferences.edit();
        editor.putStringSet(CARD_IDS_PREFERENCES_KEY, cardIds).apply();
        editor.putString(cardId + CARD_TYPE_PREFERENCES_KEY_SUFFIX, cardType).apply();
    }

    private void removeCardId(@NonNull final String cardId) {
        cardIds.remove(cardId);
        final SharedPreferences.Editor editor = encryptedPreferences.edit();
        editor.putStringSet(CARD_IDS_PREFERENCES_KEY, cardIds).apply();
        editor.remove(cardId + CARD_TYPE_PREFERENCES_KEY_SUFFIX).apply();
    }

}


