# Migration Guide — PingOne Verify Android SDK v4

This guide covers migrating from the previous SDK (the `PingOneVerifyClient.Builder()` API with `DocumentSubmissionListener`) to the current SDK.

If you're using the built-in UI, your code change is small — most of the rewrite happens inside the SDK. If you're using a headless / BYOUI integration, expect more delegate method changes.

---

## TL;DR — Code diff for a typical built-in-UI integration

**Before:**

```java
new PingOneVerifyClient.Builder()
        .setRootActivity(activity)
        .setQrString(qrString)
        .build()
        .startVerification(callback);
```

**After:**

```java
new PingOneVerifyHelper(activity, verificationUrl);
```

That's it for the built-in UI. The helper:

- Owns the underlying `PingOneVerifyClient` (no more `setListener`).
- Handles navigation, fragment routing, app theme, and language pack automatically.
- Handles back / cancel internally (no more `BackActionListener`).
- On completion, dismisses the SDK navigation and surfaces the result — no code on your side.

---

## Removed/Updated APIs

### `PingOneVerifyClient.Builder`

| Removed | Replacement |
|---|---|
| `Builder()` (no args) | `Builder(verificationUrl)` — the URL is now part of the init signature. |
| `.setRootActivity(_)` | For built-in UI, pass the activity to `new PingOneVerifyHelper(activity, url)`. For custom UI, call `.setContext(activity)` on the Builder. |
| `.setListener(_)` (`DocumentSubmissionListener`) | Pass a `VerifyTransactionCoordinatorDelegate` to `.setCoordinatorDelegate(_)` instead. |
| `.setQrString(qrString)` | Pass the URL to `Builder(verificationUrl)` directly. |
| `.setBackActionHandler(_)` (`BackActionListener`) | Removed. Built-in UI handles back / cancel internally. For custom UI, manage the back action in your own UI and call `coordinator.endVerification()` to tear down state. |
| `.setUIAppearance(_)` + `UIAppearanceSettings` | Removed. Built-in UI applies the theme configured in the PingOne Admin Console automatically. |
| `.setLanguagePackProvider(_)` | Removed as a Builder setter. Language pack fetching is built into core and runs automatically via `:language-provider`. |
| `.startVerification(callback)` | `Builder.build(BuilderCallback)` produces the client asynchronously; `onSuccess` fires after URL fetch, transaction init, language pack, and app theme have all completed. |

### Removed listener interfaces

| Removed | Replacement |
|---|---|
| `DocumentSubmissionListener` (`onDocumentSubmitted`, `onSubmissionComplete`, `onSubmissionError`) | `VerifyTransactionCoordinatorDelegate` (`didSubmitDocument`, `didCompleteSubmission`, `didFailWith`). |
| `DocumentCaptureListener` | Removed entirely. All capture flow goes through `VerifyTransactionCoordinatorDelegate`. |
| `BackActionListener` (`onBackAction`) | Removed. Built-in UI handles back internally. |

### Renamed delegate methods

| Old (`DocumentSubmissionListener`) | New (`VerifyTransactionCoordinatorDelegate`) |
|---|---|
| `onDocumentSubmitted(response)` | `didSubmitDocument(coordinator, response)` — same `DocumentSubmissionResponse` payload. |
| `onSubmissionComplete(status)` | `didCompleteSubmission(coordinator)` — the `status` argument is gone. |
| `onSubmissionError(error)` | `didFailWith(coordinator, error)` — same `DocumentSubmissionError` payload. |

---

## New delegate methods (must be implemented when building custom UI)

`VerifyTransactionCoordinatorDelegate` has default no-op implementations — implement the methods your flow requires. All methods must be handled to execute a complete verification flow.

- `didReceiveAppTheme(coordinator, appTheme, error)` — server theme delivered. Apply, or fall back to defaults on error.
- `didReceiveLanguagePack(coordinator, languageProvider, error)` — remote language pack delivered.
- `shouldCaptureDocument(coordinator, settings)` — fires for every capture step. Drive your UI from `settings.getDocumentType()`.
- `didCaptureGovernmentId(coordinator, result)` — show a preview screen, then call `coordinator.submitGovernmentId(result)`.
- `didCaptureSelfie(coordinator, result)` — show a preview screen, then call `coordinator.submitSelfie(result)`.
- `shouldRetryCapture(coordinator, feedback, settings)` — server requested a retry. `feedback` is a `RetryFeedback` value.
- `didCaptureGeolocation(coordinator, latitude, longitude)` — location coordinates available. Call `coordinator.submitGeolocation(latitude, longitude)` from here.
- `didSubmitDocument(coordinator, response)` — document was submitted; update progress.
- `didSubmitOtp(coordinator, success)` — OTP result. `true` on success; on `false`, show the user the error and let them re-enter.
- `didUpdateOtpSession(coordinator, settings)` — OTP session updated (e.g. after a resend). Refresh the OTP entry screen with the new state.
- `didCompleteSubmission(coordinator)` — flow finished successfully.
- `didFailWith(coordinator, error)` — flow failed.

## Coordinator methods

`VerifyTransactionCoordinator` provides two methods specifically for routing capture through the default provider modules:

- `captureSelfie(activity)` — launches the `:selfie-capture` UI.
- `submitSelfie(result)` — submits a captured `SelfieCaptureResult`.
- `captureGovernmentId(activity)` — launches the `:id-capture` UI.
- `submitGovernmentId(result)` — submits a captured `IdCaptureResult`.

---

## Module / AAR changes

| Module | AAR | Status |
|---|---|---|
| `:verify-core` | `PingOneVerify-4.0.1.aar` | Required (no change) |
| `:neo-interfaces` | `NeoInterfaces-4.0.1.aar` | Required (no change) |
| `:language-provider` | — | Removed. Language pack fetching is now built into `PingOneVerify.aar` — no separate AAR or manual wiring needed. |
| `:id-capture` | `IdCaptureProvider-4.0.1` (maven coordinates) | Required for government-ID capture. Must be declared via maven coordinates — see [AAR-based integration — dependency declaration change](#aar-based-integration--dependency-declaration-change). |
| `:selfie-capture` | `SelfieCaptureProvider-4.0.1` (maven coordinates) | Required for selfie steps. Must be declared via maven coordinates — see [AAR-based integration — dependency declaration change](#aar-based-integration--dependency-declaration-change). |
| `:location-provider` | `GeoLocationProvider-4.0.1.aar` | Optional. Required only for geolocation steps. |
| `:geo-location` | — | **Removed.** Replaced by `GeoLocationProvider.aar` — remove any reference to the old AAR. |

---

## AAR-based integration — dependency declaration changes

If you integrate via prebuilt AARs, the `IdCaptureProvider` and `SelfieCaptureProvider` dependency declarations have changed.

**Before:**
```groovy
implementation files('path/to/SDK/IdCaptureProvider-4.0.0.aar')
implementation files('path/to/SDK/SelfieCaptureProvider-4.0.0.aar')
implementation files('path/to/SDK/blinkid-core-7.6.1.aar')
```

**After:**
```groovy
// In root build.gradle — add the local maven repository (required for both providers)
allprojects {
    repositories {
        maven { url 'path/to/SDK/maven' }
        google()
        mavenCentral()
    }
}

// In app build.gradle — use maven coordinates instead of files(...)
implementation 'com.pingidentity.sdk.pingoneverify:IdCaptureProvider:4.0.1'
implementation 'com.pingidentity.sdk.pingoneverify:SelfieCaptureProvider:4.0.1'
implementation files('path/to/SDK/iad-2.4.0.aar')  // still required as a flat AAR
```

**Why this change is required:** Both `IdCaptureProvider` and `SelfieCaptureProvider` have transitive runtime dependencies that cannot be bundled inside the AAR itself. The `.pom` files shipped alongside the AARs in `SDK/maven/` declare these dependencies so Gradle can resolve them automatically. Using `files(...)` bypasses POM resolution entirely and will cause a runtime crash when capture is initiated.

Note: `blinkid-core` is no longer shipped as a flat AAR — it is resolved transitively via `IdCaptureProvider.pom` from `mavenCentral()`. Remove any explicit `blinkid-core` reference from your `build.gradle`.

---

## Behaviour changes

### No auto-start; QR scanner is no longer auto-presented

- `PingOneVerifyHelper` starts the flow automatically once `build()` completes — no separate `start()` call needed.
- The Builder no longer presents its own QR scanner. Developers must supply the verification URL to `Builder(verificationUrl)`. A `QrScannerDialog` is still shipped in the app source (see `:app/src/main/java/.../sample/qr_scanner/`).

### verify-ui is now open source

The default UI layer (previously shipped as a prebuilt AAR) is now compiled directly in `:app`. All UI fragments, dialogs, `PingOneVerifyHelper`, and `DocumentCapturePresenter` are modifiable source:

```
:app/src/main/java/com/pingidentity/sdk/pingoneverify/
  ├── sample/               ← sample app, QR scanner
  └── ui/                   ← open-source verify-ui (modify freely)
      ├── fragments/        ← all capture screens, dialogs
      ├── providers/        ← PingOneVerifyHelper, DocumentCapturePresenter
      ├── utils/
      └── views/
```

---

## Step-by-step migration checklist

1. Replace `PingOneVerifyClient.Builder().setRootActivity(...).setQrString(...).build().startVerification(...)` with `new PingOneVerifyHelper(activity, verificationUrl)`. The helper starts the flow automatically.
2. Implementations for `DocumentSubmissionListener`, `BackActionListener`, `DocumentCaptureListener` can be removed. For built-in UI, no new interface implementations are required — it is handled in `PingOneVerifyHelper`.
3. Remove `.setUIAppearance(_)` if it was used. Configure the theme in the PingOne Admin Console. The SDK applies the server theme automatically.
4. Replace any reference to the old geolocation AAR with `GeoLocationProvider-4.0.1.aar`.
5. Remove any `language-provider` AAR or dependency — language pack fetching is now built into `PingOneVerify.aar`.
6. Update `IdCaptureProvider` and `SelfieCaptureProvider` from `files(...)` references to maven coordinates, and remove any explicit `blinkid-core` AAR reference — see [AAR-based integration — dependency declaration changes](#aar-based-integration--dependency-declaration-changes).
