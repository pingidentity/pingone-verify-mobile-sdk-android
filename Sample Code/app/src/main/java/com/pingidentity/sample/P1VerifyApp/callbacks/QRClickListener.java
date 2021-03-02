package com.pingidentity.sample.P1VerifyApp.callbacks;

public interface QRClickListener {

    void onUrlResult(String url);

    void onCodeResult(String code);

}
