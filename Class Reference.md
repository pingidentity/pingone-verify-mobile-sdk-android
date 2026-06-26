# VerifyTransactionCoordinator / VerifyTransactionCoordinatorDelegate — Android Public API Reference

Two-way contract between the verification UI and the SDK core.

- **`VerifyTransactionCoordinator`** — UI calls these on core to submit data and drive the flow.
- **`VerifyTransactionCoordinatorDelegate`** — core calls these on the UI to instruct what to show next.

---

## `VerifyTransactionCoordinator`

### Document submission

| Method | Signature | Description |
|--------|-----------|-------------|
| Submit email | `submitEmail(String email)` | Submits an email address for OTP verification. |
| Submit phone | `submitPhone(String phone)` | Submits a phone number for OTP verification. |
| Submit selfie | `submitSelfie(SelfieCaptureResult result)` | Submits a captured selfie to PingOne Verify. |
| Submit government ID | `submitGovernmentId(IdCaptureResult result)` | Submits a captured government ID to PingOne Verify. |
| Submit geolocation | `submitGeolocation(float latitude, float longitude)` | Submits captured geolocation coordinates. |

### OTP

| Method | Signature | Description |
|--------|-----------|-------------|
| Submit OTP | `submitOtp(String passcode, DocumentClass otpType)` | Submits the one-time passcode entered by the user. `otpType` must be `EMAIL` or `PHONE`. |
| Resend OTP | `resendOtp(DocumentClass otpType)` | Requests a new OTP delivery for the given document type. |

### Capture launch

| Method | Signature | Description |
|--------|-----------|-------------|
| Launch selfie capture | `captureSelfie(Activity activity)` | Launches the selfie capture flow. Requires the `:selfie-capture` module. |
| Launch ID capture | `captureGovernmentId(Activity activity)` | Launches the ID capture flow. Requires the `:id-capture` module. |
| Capture geolocation | `captureGeolocation(Context context)` | Asks the location provider to request the device's current location. Call this after the user grants location permission. |

### Flow control

| Method | Signature | Description |
|--------|-----------|-------------|
| Skip step | `skipDocument(DocumentClass type)` | Skips the current optional step. The server must mark the step optional or the call fails. |
| End flow | `endVerification()` | Ends the verification flow and releases observers. |

---

## `VerifyTransactionCoordinatorDelegate`

### Theme and language pack (triggered before the first `shouldCaptureDocument`)

| Method | Signature | Description |
|--------|-----------|-------------|
| App theme ready | `didReceiveAppTheme(VerifyTransactionCoordinator coordinator, AppThemeResponse appTheme, IdvError error)` | Server theme fetched. `appTheme` is non-null on success; `error` is non-null on failure. |
| Language pack ready | `didReceiveLanguagePack(VerifyTransactionCoordinator coordinator, LanguagePackProviderContract languageProvider, IdvError error)` | Language pack fetched. Use `error` (non-null on failure) to fall back to app string resources. Fires at `build()` completion. |

### Capture lifecycle

| Method | Signature | Description |
|--------|-----------|-------------|
| Should capture | `shouldCaptureDocument(VerifyTransactionCoordinator coordinator, DocumentCaptureSettings settings)` | Core needs the user to capture a document or provide OTP/geolocation. Read `settings.getDocumentType()` to determine the screen to show. |
| Should retry | `shouldRetryCapture(VerifyTransactionCoordinator coordinator, RetryFeedback feedback, DocumentCaptureSettings settings)` | The previous submission failed due to quality issues and the user may retry. |
| Did submit document | `didSubmitDocument(VerifyTransactionCoordinator coordinator, DocumentSubmissionResponse response)` | Document submitted successfully. |
| Did capture selfie | `didCaptureSelfie(VerifyTransactionCoordinator coordinator, SelfieCaptureResult result)` | Selfie camera finished. Show preview if desired, then call `submitSelfie(result)` or `captureSelfie(activity)` to retake. |
| Did capture ID | `didCaptureGovernmentId(VerifyTransactionCoordinator coordinator, IdCaptureResult result)` | ID scan finished. Show preview if desired, then call `submitGovernmentId(result)` or `captureGovernmentId(activity)` to retake. |
| Did capture geolocation | `didCaptureGeolocation(VerifyTransactionCoordinator coordinator, float latitude, float longitude)` | Geolocation captured. Optionally show a confirmation, then call `submitGeolocation(latitude, longitude)`. |

### OTP

| Method | Signature | Description |
|--------|-----------|-------------|
| Did submit OTP | `didSubmitOtp(VerifyTransactionCoordinator coordinator, boolean success)` | OTP verification result. `true` if the passcode was accepted. |
| OTP session updated | `didUpdateOtpSession(VerifyTransactionCoordinator coordinator, DocumentCaptureSettings settings)` | OTP session state changed (e.g. after a resend acknowledgement). Refresh the OTP entry screen with the new state. |

### Completion / failure

| Method | Signature | Description |
|--------|-----------|-------------|
| Did complete | `didCompleteSubmission(VerifyTransactionCoordinator coordinator)` | All required documents submitted. Navigate to your completion screen and release your reference to the coordinator. |
| Did fail | `didFailWith(VerifyTransactionCoordinator coordinator, DocumentSubmissionError error)` | Unrecoverable error. Use `error.getErrorCode()` / `error.getMessage()` for details. |

---

## Data Model

### `DocumentStatus` — Server-reported per-document status

Used as values inside `DocumentSubmissionResponse.documentStatus`.

| Case | Raw value | Description |
|------|-----------|-------------|
| `REQUIRED` | `0` | Must be collected before the transaction can complete. |
| `OPTIONAL` | `1` | May be collected but can be skipped. |
| `COLLECTED` | `2` | Received by the server. |
| `PROCESSED` | `3` | Processed by the server. |
| `SKIPPED` | `4` | Skipped by the user or the SDK. |
| `RETRYABLE` | `5` | Submission failed; the user may retry. |

### `DocumentSubmissionStatus` — Overall collection status

Returned in `DocumentSubmissionResponse.documentSubmissionStatus`.

| Case | Description |
|------|-------------|
| `NOT_STARTED` | Document collection has not yet begun. |
| `STARTED` | Document collection is in progress. |
| `COMPLETED` | All required documents have been collected. |
| `PROCESS` | Documents are being processed by the server. |

### `OtpStatus` — OTP delivery / verification status (inside `OtpSession`)

| Case | Description |
|------|-------------|
| `REQUESTED` | OTP requested; delivery has not yet started. |
| `IN_PROGRESS` | OTP delivery in progress. |
| `OTP_SENT` | OTP delivered to the user. |
| `SUCCESS` | Delivery and verification successful. |
| `FAIL` | Delivery or verification failed; not retryable. |
| `OTP_RETRYABLE` | Delivery failed; the user may request a new code. |
| `OTP_VERIFIED` | User successfully verified the OTP. |

### `SelfieCaptureResult`

Returned by `didCaptureSelfie` and consumed by `submitSelfie`.

| Field | Type | Description |
|-------|------|-------------|
| `selfie` | `String` | Base64-encoded JPEG selfie image. |
| `iadPayload` | `String?` | Base64-encoded liveness payload from the IDRND SDK, or `null` if unavailable. |

### `IdCaptureResult`

Returned by `didCaptureGovernmentId` and consumed by `submitGovernmentId`.

| Field | Type | Description |
|-------|------|-------------|
| `documentData` | `Map<String, String>` | Key-value map of OCR fields and base64-encoded images extracted from the document. |
| `idType` | `String` | The scanned document type string (e.g. `"DRIVER_LICENSE"`, `"PASSPORT"`). |

### `DocumentSubmissionResponse`

Server response delivered to `didSubmitDocument`.

| Field | Type | Description |
|-------|------|-------------|
| `document` | `Map<String, String>?` | The submitted document data as a key-value map (e.g. `{"email": "user@example.com"}`). Used internally to derive the OTP destination for `EMAIL` / `PHONE` flows. |
| `documentStatus` | `Map<String, DocumentStatus>?` | Per-document verification status, keyed by document type string. Values are typed `DocumentStatus` enum constants. |
| `documentSubmissionStatus` | `DocumentSubmissionStatus?` | Overall status of the document collection session. |
| `createdAt` | `String?` | ISO 8601 timestamp when the session was created on the server. |
| `updatedAt` | `String?` | ISO 8601 timestamp of the most recent server-side update. |
| `expiresAt` | `String?` | ISO 8601 timestamp after which the session is no longer valid. |

### `RetryFeedback`

Server feedback for a failed capture, delivered to `shouldRetryCapture`.

| Field | Type | Description |
|-------|------|-------------|
| `code` | `String` | Server error code (e.g. `"QUALITY_CHECK_FAILED"`). |
| `message` | `String` | Human-readable fallback message from the server. |
| `languagePackKey` | `String` | Language-pack key for the localised error string. |

### `DocumentCaptureSettings`

Common base class for every capture step. Concrete subtypes — `IdCaptureSettings`, `SelfieCaptureSettings`, `EmailCaptureSettings`, `PhoneCaptureSettings`, `OtpCaptureSettings`, `LocationCaptureSettings` — carry additional fields specific to their step.

| Field | Accessor | Description |
|-------|----------|-------------|
| `documentType` | `getDocumentType()` | The data type this step collects. |
| `optional` | `isOptional()` | When `true`, the user may skip this step. |
| `isRetry` | `isRetry()` | `true` when this step is a retry of a previously failed attempt. |
| `remainingAttempts` | `getRemainingAttempts()` | Number of remaining capture attempts, or `null` if unlimited. |

### `OtpCaptureSettings` (extends `DocumentCaptureSettings`)

Subtype delivered to `shouldCaptureDocument` and `didUpdateOtpSession` for OTP steps.

| Field | Type | Description |
|-------|------|-------------|
| `otpDestination` | `String?` | Email address or phone number to which the OTP was sent. |
| `canResend` | `Boolean` | Whether the server permits another OTP delivery. |
| `expiresAt` | `String?` | ISO 8601 OTP expiry timestamp. |
| `resendCooldown` | `String?` | Seconds the caller must wait before another resend. |

### `OtpSession`

| Field | Accessor | Description |
|-------|----------|-------------|
| `otpStatus` | `getOtpStatus()` | Current delivery/verification status (see [OtpStatus](#otpstatus--otp-delivery--verification-status-inside-otpsession)). |
| `expiresAt` | `getExpiresAt()` | ISO 8601 OTP expiry timestamp. |
| `canResend` | `isCanResend()` | Whether the server permits another OTP delivery. |
| `resendCooldown` | `getResendCooldown()` | Seconds the caller must wait before another resend. |

### `AppThemeResponse`

Delivered to `didReceiveAppTheme`.

| Field | Description |
|-------|-------------|
| `companyName` | Configured company name, if any. |
| `template` | Template identifier (e.g. `"default"`). |
| `defaultTheme` | `true` when the server returned the default PingOne theme. |
| `configuration` | Full set of colour, button, and logo configuration values. |

### `DocumentSubmissionError`

Delivered to `didFailWith` on unrecoverable failures.

| Member | Description |
|--------|-------------|
| `getErrorCode()` | Machine-readable error code. |
| `getMessage()` | Human-readable description, suitable for logging. |

**Error subclasses**: `InitiateDocumentTransactionError`, `NoDocumentToSubmitError`, `UserCanceledError`, `MissingDocumentTypeError`, `InvalidKeyMapError`, `DocumentCaptureError`, `MissingOtpDestinationError`, `MissingOtpError`, `FailedOtpError`, `OtpSessionTimeoutError`, `TransactionFailedError`, `OtpTimedOut`, `SkipDocumentError`.

## Canonical Flow

```
shouldCaptureDocument    Core → UI    (UI shows the capture screen)
capture<X>               UI → Core    (UI launches the capture provider)
didCapture<X>            Core → UI    (UI shows preview / confirm)
submit<X>                UI → Core    (UI shows progress screen)
didSubmitDocument        Core → UI    (UI hides progress, advances)
```

Where `<X>` is one of `GovernmentId`, `Selfie`, `Geolocation`. For email/phone/OTP steps the UI shows a text-entry screen instead of a capture provider and skips the `capture<X>` / `didCapture<X>` legs.

**Progress screen lifecycle**:
- Show after `submit<X>`.
- Hide on `didSubmitDocument`, `didCompleteSubmission`, or the next `shouldCaptureDocument`.
