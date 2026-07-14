package com.pingidentity.sdk.pingoneverify.ui.providers;

import com.pingidentity.sdk.pingoneverify.neo.errors.DocumentSubmissionError;

/**
 * Callback for verification flow completion events.
 * Implement on your Activity and pass to {@link PingOneVerifyHelper}.
 */
public interface VerifyHelperCallback {

    void onVerificationCompleted();

    void onVerificationFailed(DocumentSubmissionError error);
}
