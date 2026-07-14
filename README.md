# PingOne Verify Android SDK

PingOne Verify is an identity verification service that supports the collection and authentication of identity evidence, including government-issued IDs, selfies, email/phone OTPs, and geolocation. This SDK functions as the collection client for the verification flow, capturing user evidence and performing quality checks to ensure documents and images meet the required standards before they are submitted for verification.

---

## Contents

1. [Components](#components)
2. [Prerequisites](#prerequisites)
3. [Installation](#installation)
4. [Class Reference](#class-reference)

---

## Components

![Architecture](docs/images/Architecture.svg)

| Component | Description |
|-----------|-------------|
| **Native Mobile Application** | The consuming application that integrates the SDK. It initiates verification flows by invoking public client methods and is responsible for integrating and customizing the open-source UI module. |
| **verify-ui** | Open-source UI module that provides the instructional and interactive screens necessary for the various verification capture flows. The source lives in `:app` and is compiled directly into the app target. |
| **verify-core** | The central coordination layer. It orchestrates all necessary flows between the UI and capture components, manages secure data transmission to the backend service, and ensures successful execution of the overall transaction logic. |
| **ID Capture Module** | Integrates with BlinkID to capture images and extract data from government identity documents. |
| **Selfie Capture Module** | Manages the facial image capture process, performs real-time quality checks on live frames, and executes liveness detection to mitigate injection attacks. |
| **verify-transaction-coordinator** | Interface implemented within the core module; utilized by the UI to execute specific actions (e.g., submit data). |
| **verify-transaction-coordinator-delegate** | Interface implemented by the UI; utilized by the core module to notify the UI to perform actions based on instructions received from the backend service. |
| **Backend Service** | PingOne Verify services hosted in the PingOne multi-tenant SaaS platform. Provides data collection and transaction state management, enabling a "backend-driven UI" flow alongside verify-core. |

---

## Integration Flow Diagrams

The diagrams below show the message flow between your UI layer (`verify-ui`), the delegate you implement (`verify-coordinator-delegate`), the coordinator API (`verify-coordinator`), and the SDK core (`verify-core`) for each capture type.

### Government ID Capture

Scans a government-issued document (passport, driver licence, or national ID) and submits it for server-side verification. On quality failures, the SDK requests a retry up to the number of attempts configured in your verify policy.

![Government ID Capture](docs/images/GovernmentId.png)

---

### Selfie Capture

Captures a live selfie for liveness detection and face-match verification. If the selfie step is marked optional in your verify policy, the UI can skip it entirely. On quality failures, the SDK requests a retry.

![Selfie Capture](docs/images/Selfie.png)

---

### Email OTP Verification

Collects the user's email address, sends a one-time passcode to that address, and validates it. The user can request a resend at any time; the SDK refreshes the OTP session and restarts the countdown timers automatically.

![Email OTP Verification](docs/images/Email.png)

---

### Phone OTP Verification

Same flow as email OTP but the one-time passcode is delivered via SMS. The user enters their phone number, receives a code, and submits it.

![Phone OTP Verification](docs/images/Phone.png)

---

### Geolocation Capture

Requests the device's current location. If the user grants permission the coordinates are captured and submitted immediately. If permission is denied the SDK surfaces an error; the UI should display a rationale and a button so the user can grant permission from Settings and retry.

![Geolocation Capture](docs/images/Geolocation.png)

---

## Prerequisites

- Android Studio with Gradle
- Android SDK 26 or later
- Physical device recommended (camera is required; emulator is not supported for capture steps)

---

## Installation

### AAR-based Integration

Use this approach when integrating the SDK as prebuilt AARs (e.g. from the `SDK/` folder shipped alongside this repository).

1. Add the local maven repository to your **root** `build.gradle`:

```groovy
allprojects {
    repositories {
        maven { url 'path/to/SDK/maven' }  // required for IdCaptureProvider POM resolution
        google()
        mavenCentral()
    }
}
```

2. Add to your **app** `build.gradle`:

```groovy
dependencies {
    // Core
    implementation files('path/to/SDK/PingOneVerify-4.0.1.aar')
    implementation files('path/to/SDK/NeoInterfaces-4.0.1.aar')

    // Geolocation capture
    implementation files('path/to/SDK/GeoLocationProvider-4.0.1.aar')

    // Selfie / liveness capture — declared via maven coordinates so transitive deps resolve automatically
    implementation 'com.pingidentity.sdk.pingoneverify:SelfieCaptureProvider:4.0.1'
    implementation files('path/to/SDK/iad-2.4.0.aar')

    // Government ID capture — declared via maven coordinates so transitive deps resolve automatically
    implementation 'com.pingidentity.sdk.pingoneverify:IdCaptureProvider:4.0.1'

    // Camera — required for QR scanning
    implementation 'androidx.camera:camera-camera2:1.4.2'
    implementation 'androidx.camera:camera-core:1.4.2'
    implementation 'androidx.camera:camera-lifecycle:1.4.2'
    implementation 'androidx.camera:camera-view:1.4.2'
}
```

> **Why `IdCaptureProvider` and `SelfieCaptureProvider` use maven coordinates instead of `files(...)`:** Both modules have transitive runtime dependencies that cannot be bundled inside the AAR. Declaring them via maven coordinates allows Gradle to read the accompanying `.pom` files and resolve those transitive dependencies automatically from `google()` / `mavenCentral()`. Declaring them via `files(...)` will bypass POM resolution and result in a runtime crash.

---

### Using the open-source UI (SampleCode)

The `SampleCode/PingOneVerify/` directory ships the full open-source UI layer (`PingOneVerifyHelper`, all capture fragments, dialogs, and utilities) alongside the sample app entry point. You can copy this UI source directly into your own project and modify it freely.

1. Copy the UI source from `SampleCode/PingOneVerify/app/src/main/java/.../ui/` into your project.

2. Use the same AAR-based dependencies listed in the [AAR-based Integration](#aar-based-integration) section above.

> **Language pack:** Override string values in your app's `res/values/strings.xml` using the same keys defined in the UI source.

---

## Quick Start — Built-in UI

`PingOneVerifyHelper` owns the full lifecycle — async init, language pack, app theme, and navigation. Construct it and the verification flow starts automatically when ready.

```java
public class MyActivity extends AppCompatActivity {

    void start(String url) {
        new PingOneVerifyHelper(this, url);
    }

}
```

`PingOneVerifyHelper` handles navigation, fragment routing, app theme, language pack, and all delegate callbacks internally. No additional wiring is needed.

---

### Localization

Override any string by replacing its value in your app's `res/values/strings.xml`.

---

## Migration Guide

You can refer to the [migration guide](MIGRATION.md).

---

## Class Reference

You can refer to the [class reference](Class Reference.md).

---
