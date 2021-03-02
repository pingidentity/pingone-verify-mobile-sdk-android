package com.pingidentity.sample.P1VerifyApp.utils;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class P1FirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = P1FirebaseMessagingService.class.getCanonicalName();

    private static final MutableLiveData<String> pushToken =  new MutableLiveData<>();
    private static final MutableLiveData<Map<String, String>> notificationData =  new MutableLiveData<>();

    public static void updatePushToken(@NonNull final String pushToken) {
        P1FirebaseMessagingService.pushToken.postValue(pushToken);
    }

    public static LiveData<String> getPushToken() {
        return pushToken;
    }

    public static LiveData<Map<String, String>> getNotificationData() {
        Log.e(TAG, "notificationData.hasActiveObservers(): " + (notificationData.hasActiveObservers()));
        return notificationData;
    }

    public static void clearNotificationData() {
        notificationData.postValue(null);
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "New Token: " + token);
        P1FirebaseMessagingService.pushToken.postValue(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        notificationData.postValue(remoteMessage.getData());
    }
}