package com.pingidentity.sdk.pingoneverify.ui

object UiConstants {

    const val ID_CAPTURE_TAG = "IdCaptureFragment"
    const val GEOLOCATION_TAG = "LocationFragment"

    object Tags {
        const val PROCESSING_DIALOG = "ProcessingDialog"
        const val UPLOAD_FAILURE_DIALOG = "UploadFailureDialog"
        const val DOCUMENT_CAPTURE_DIALOG = "com.pingidentity.sdk.pingoneverify.ui.fragments.DocumentCaptureDialog"
        const val EMAIL_PHONE_VERIFICATION_DIALOG = "com.pingidentity.sdk.pingoneverify.ui.fragments.EmailPhoneVerificationDialog"
        const val OTP_DIALOG = "OtpDialog"

        // SelfieViewActivity intent extras
        const val SELFIE_EXTRA_LICENSE_KEY = "extra_license_key"
        const val SELFIE_EXTRA_CAPTURE_TIME = "extra_capture_time"
        const val SELFIE_EXTRA_PAYLOAD_SIZE = "extra_payload_size"
        const val SELFIE_EXTRA_SHOULD_CAPTURE_AFTER_TIMEOUT = "extra_should_capture_after_timeout"

        // SelfieViewActivity result keys
        const val SELFIE_RESULT_PHOTO_PATH = "result_photo_path"
        const val SELFIE_RESULT_PAYLOAD_PATH = "result_payload_path"
    }
}
