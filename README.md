# PingOne Verify SDK for Android


PingOneVerify Android SDK provides a secure interface for an Android app to use the PingOne Verify service for validating a user's identity. The SDK also parses the responses received from the service for different operations and forwards the same to the app via callbacks.

### Running the Sample App


[Download the Sample App](https://github.com/pingidentity/pingone-verify-mobile-sdk-android).


### Prerequisites
* Android Studio with Gradle
* Android SDK 26 and up

### Set Up and Clone or Download

The sample app can be run on a simulator but requires special set up and works much better on a device, because the app requires the camera to capture a selfie and and the related user ID documents. If you want to run the app on emulator you will need to use your PC's camera.

1. Clone or download the [PingOne Verify SDK for Android](https://github.com/pingidentity/pingone-verify-mobile-sdk-android) sample code to your computer. The Sample Code directory contains the Android project that is ready to be built when the prerequisites are satisfied.


2. To open the sample app as a project in Android Studio, go to File --> New --> Import Project. Choose the SampleCode/PingOneVerify folder as the project's root folder.

### Integrating PingOne Verify SDK with Your App

PingOneVerify Android SDK provides a secure interface for an Android app to use the PingOne Verify service for validating a user's identity. The SDK also parses the responses received from the service for different operations and forwards the same to the app via callbacks.

### Getting started
Add the dependencies needed for your application.

If you haven't done so already, clone or download the [PingOne Verify SDK for Android](https://github.com/pingidentity/pingone-verify-mobile-sdk-android) sample app. You'll find the `.aar` dependencies required for the PingOne Verify Android SDK in the SDK directory.

1. Create a `libs` folder, if it doesn't exist under your module, and copy the downloaded `.aar` dependencies:

    * PingOneVerify-2.3.0.aar

    * id_scanner_sdk.aar

    * voice_sdk.aar

    * iad.aar

2. Add the following to your module level `build.gradle` file to include the dependencies in your module:

```
dependencies {
    implementation fileTree(include: ["*.aar"], dir: "libs")
    ...
}
```
3. Because these components are loaded locally, you must also include the SDK's dependencies in the configuration to compile and run it.
```
dependencies {

    implementation 'androidx.activity:activity-ktx:1.7.0'
    implementation 'androidx.fragment:fragment-ktx:1.5.6'
    implementation 'androidx.annotation:annotation:1.6.0'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'androidx.camera:camera-camera2:1.2.3'
    implementation 'androidx.camera:camera-core:1.2.3'
    implementation 'androidx.camera:camera-extensions:1.2.3'
    implementation 'androidx.camera:camera-lifecycle:1.2.3'
    implementation 'androidx.camera:camera-view:1.2.3'
    implementation 'androidx.core:core-ktx:1.10.0'
    implementation 'androidx.fragment:fragment-ktx:1.5.7'

    implementation 'com.google.android.material:material:1.9.0'
    implementation 'com.google.code.gson:gson:2.9.0'
    implementation 'com.googlecode.libphonenumber:libphonenumber:8.13.7'

    implementation 'com.google.android.gms:play-services-location:21.3.0'
    implementation 'com.google.android.gms:play-services-mlkit-barcode-scanning:18.2.0'
    implementation 'com.google.android.gms:play-services-mlkit-face-detection:17.1.0'

    implementation 'com.squareup.moshi:moshi:1.14.0'
    implementation 'com.squareup.moshi:moshi-kotlin:1.14.0'
    implementation 'com.squareup.okhttp3:okhttp:4.9.3'
    implementation 'com.squareup.okhttp3:okhttp-urlconnection:4.9.3'
    implementation 'com.squareup.retrofit2:converter-moshi:2.9.0'
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.github.bumptech.glide:glide:4.16.0'
    kapt 'com.github.bumptech.glide:compiler:4.16.0'
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4'
    implementation 'org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.10'

    [//]: <> (IMPORTANT: Below dependencies must be defined for Selfie/Government ID document upload on S3)
    implementation 'org.bitbucket.b_c:jose4j:0.9.5'
    [//]: <> (IMPORTANT: Below dependencies should be defined for Selfie Liveness feature)
    implementation 'com.google.protobuf:protobuf-javalite:3.20.1'
    implementation 'com.google.protobuf:protobuf-kotlin-lite:3.20.1'

    }
```

### Initializing PingOneVerifyClient

1. Import `com.pingidentity.sdk.pingoneverify.PingOneVerifyClient` in your desired Activity or Fragment
```
import com.pingidentity.sdk.pingoneverify.PingOneVerifyClient;
```
2. Implement the [DocumentSubmissionListener](#documentsubmissionlistener_callbacks) interface and its functions
````java
    public class CallingFragment extends Fragment implements DocumentSubmissionListener {
        // Callback when document is successfully submitted
        @Override public void onDocumentSubmitted(DocumentSubmissionResponse response) {}
        // Callback when verification transaction is completed
        @Override public void onSubmissionComplete(DocumentSubmissionStatus status) {}
        // Callback when there is an error during submission
        @Override public void onSubmissionError(DocumentSubmissionError error) {}
    }
````

3. Instantiate a PingOneVerifyClient.Builder and set its `listener` and `rootActivity`
   Start the Verification Process
````java
    PingOneVerifyClient.Builder builder = new PingOneVerifyClient.Builder(false)
        .setRootActivity(getActivity())
        .setListener(this)
        .startVerification(new PingOneVerifyClient.Builder.BuilderCallback() {
            @Override public void onSuccess(PingOneVerifyClient client) {}
            @Override public void onError(String errorMessage) {}
        });
````

4. Optionally, you can set an explicit **qrString** using the `PingOneVerifyClient.Builder`
````java
    new PingOneVerifyClient.Builder(false)
        .setListener(this)
        .setRootActivity(getActivity())
        .setQrString(qrString: "https://api.pingone.com...")
````

5. Optionally, you can set a [Selfie Capture Settings](#selfiecapturesettings) with your preference using the `PingOneVerifyClient.Builder.`
````java
// Default is 45 seconds for captureTime and true for shouldCaptureAfterTimeout
SelfieCaptureSettings selfieCaptureSettings = new SelfieCaptureSettings(15, false)
List<CaptureSettings> settings = new ArrayList<>();
settings.add(selfieCaptureSettings);

new PingOneVerifyClient.Builder(false)
    .setRootActivity(getActivity())
    .setListener(this)
    .setDocumentCaptureSettings(settings);
````

### DocumentSubmissionListener Callbacks

1. `onDocumentSubmitted(DocumentSubmissionResponse response)`


    * Called whenever a document is successfully submitted.
    * Appropriate [DocumentSubmissionResponse](#documentsubmissionresponse) is returned
````java
public void onDocumentSubmitted(DocumentSubmissionResponse response)  {
    Log.d("The  document  status  is :", response.getDocumentStatus())
    Log.d("The  document  submission  status  is",          response.getDocumentSubmissionStatus())
    Map<String,String> documents = response.getDocument();  
    documents.forEach((k,v)->Log.d("(Key, Value):", "("+ k + ","+ v +")"));
}

````

2. `onSubmissionComplete(DocumentSubmissionStatus status)`


    * Called when all required documents have been submitted for a transaction.
    * [DocumentSubmissionStatus](#documentsubmissionstatus) will always be `.completed`
````java
public void onSubmissionComplete(DocumentSubmissionStatus status) {
    //  present  a  basic  alert  to  indicate  completion
    new AlertDialog.Builder(getActivity())  
        .setTitle("Document Submission Complete")  
        .setMessage("All  documents  have  been  successfully  submitted")  
        .create()  
        .show();
}
````

3. `onSubmissionError(DocumentSubmissionError error)`


    * Called when error occurs during SDK flow.
    * Appropriate [DocumentSubmissionError](#documentsubmissionerror) is returned
````java
public void onSubmissionError(DocumentSubmissionError error) {  
    Log.e("onSubmissionError", error.getMessage());
}

````

### Class Reference

### DocumentSubmissionResponse
`DocumentSubmissionResponse` object holds information pertaining to the document that was successfully submitted to the ID Verification service.


````java
public class DocumentSubmissionResponse implements Serializable{
    public func getDocumentStatus() -> [String: DocumentStatus]
    public func getDocumentSubmissionStatus() -> DocumentSubmissionStatus
    public Map<String, String> getDocument()
    public Date getCreatedAt()
    public Date getUpdatedAt()
    public Date getExpiresAt()
}
````
`documentStatus` dictionary shows the status of each document requested. For example, if the requested documents for a certain transaction are the following:

* Email

* Phone

* Selfie

and the callback `oonDocumentSubmitted(DocumentSubmissionResponse response)` was fired after submitting `Email`, the `documentStatus` property will look like this:

```
["Email": .PROCESSED, "Phone": .REQUIRED, "Selfie", .REQUIRED]

```

The list of `DocumentStatus` states can be found in [DocumentStatus](#documentstatus)


`documentSubmissionStatus` property shows the status of the entire verification transaction. Detailed information regarding `DocumentSubmissionStatus` can be found in [DocumentSubmissionStatus](#documentsubmissionstatus)


`document` dictionary holds information about the document that was just submitted to the ID Verification service. **Only applicable keys** are populated in the `document` dictionary


Data model of `document`


| Key | Description |
| --- | --- |
| `frontImage` | Base64 encoded string of the front image of a document |
| `backImage` | Base64 encoded string of the back image of a document |
| `barcode` | Barcode Data found on the document |
| `mrzData` | MRZData found on passport and other relevant document |
| `firstName` | User's first name as shown on the document |
| `middleName` | User's middle name as shown on the document |
| `lastName` | User's last name as shown on the document |
| `fullName` | User's full name |
| `additionalNameInfo` | Additional name info about the user |
| `addressStreet` | User's street address as shown on the document |
| `addressCity` | User's residence city as shown on the document |
| `addressState` | User's residence state as shown on the document |
| `addressZip` | User's ZIP Code as shown on the document |
| `country` | User's residence country as shown on the document |
| `sex` | User's sex as shown on the document |
| `dateOfBirth` | User's date of birth as shown on the document |
| `placeOfBirth` | User's place of birth as shown on the document |
| `nationality` | User's nationality as shown on the document |
| `maritalStatus` | User's marital status as shown on the document |
| `race` | User's race as shown on the document |
| `religion` | User's religion as shown on the document |
| `residentialStatus` | User's residential status as shown on the document |
| `documentNumber` | Document number as shown on the document |
| `documentAdditionalNumber` | Additional document number as shown on the document |
| `personalIdNumber` | Personal Id number as shown on the document |
| `dateOfIssue` | Date of issuance of the document |
| `dateOfExpiry` | Date of expiration of the document |
| `issuingAuthority` | Issuing authority of the document |
| `employer` | Employer as shown on the document |
| `profession` | Profession as shown on the document |


### DocumentStatus

An enum describing the status of a particular document


````java
public enum DocumentStatus {
    case REQUIRED, // document is required
    case OPTIONAL, // document is optional; user may choose to skip
    case COLLECTED, // document has been collected
    case PROCESSED, // document has been processed
    case SKIPPED; // document is skipped by user's choice
}
````

### DocumentSubmissionStatus

An enum describing the status of the verification transaction

````java
public enum DocumentSubmissionStatus implements Serializable {
    @Json(name = "NOT_STARTED") NOT_STARTED, // transaction has not been initiated
    @Json(name = "STARTED") STARTED,// transaction has been initiated, but not completed
    @Json(name = "COMPLETED") COMPLETED, // transcation is being processed
    @Json(name = "PROCESS") PROCESS // transaction has been processed and is completed
}
````

### SelfieCaptureSettings

A configurable object to customize selfie capture experience

````java
public class SelfieCaptureSettings {
    public int captureTime; // selfie capture time (default is 45 seconds)
    public boolean shouldCaptureAfterTimeout; // whether user should be able to capture selfie after timeout (default is true)
}

````

### PingOne Verify SDK Errors
### ClientBuilderError
`ClientBuilderError` is returned when *PingOneVerify SDK* is initialized **incorrectly**. It subclasses `BuilderError` and `QRError` and is returned during `Builder.startVerification()`


### BuilderError

| Error | Description |
| --- | --- |
| `"Must Set RootActivity"` | RootActivity was not set using `.setRootActivity(getActivity()))` in the builder |
| `"Must Set DocumentSubmissionListener"` | `DocumentSubmissionListener` was not set using `.setDocumentSubmissionListener(listener)` in the builder |


### QRError

| Error | Description |
| --- | --- |
| `"Unable to parse queryItems from QR String: "` | Unable to parse QR Code |
| `"Missing verification code"` | QR Code is missing verification code |
| `"Missing TransactionId"` | QR Code is missing transactionId |


### DocumentSubmissionError


| Error | Description |
| --- | --- |
| `"Unable to initiate document transaction for transactionId:"` | Error when initiating a transaction |
| `"initiateDocumentCollection error"` | Error when submitting a document |
| `"There are no documents to submit for this transaction"` | No more documents to submit |
| `"Missing Document Type"` | Document type is missing |
| `"Invalid key map for documentType:"` | A valid key map for the document does not exist |
| `"Document Capture Error"` | Error when capturing the data |
| `"Missing OTP Destination for:"` | OTP Destination is missing |
| `Verification Code failed for %s` | OTP failed with invalid code |


### UI Customization
UIAppearanceSettings
`UIAppearanceSettings` instance allows you to customize the SDK's user interface during run time.

````java
    public class UIAppearanceSettings {
        private ImageLink logoImage;
        private String backgroundColor;
        private String bodyTextColor;
        private String headingTextColor;
        private String navigationBarColor;
        private String navigationBarTextColor;
        private String iconTintColor;
        private ButtonAppearance solidButtonAppearance;
        private ButtonAppearance borderedButtonAppearance;
    }
````

| Method                                                                                 | Description                                                      |
|----------------------------------------------------------------------------------------|------------------------------------------------------------------|
| `setLogoImage(Bitmap logoImage) -> UIAppearanceSetting`                                | Set logo image that is shown at the center of the navigation bar |
| `setBackgroundColor(String backgroundColor) -> UIAppearanceSetting`                    | Set application background color                                 |
| `setAppearanceProvider(AppearanceProvider appearanceProvider) -> UIAppearanceSetting`  | Customise appearance settings(See [AppearanceProvider](#appearanceprovider))|
| `setBodyTextColor(String bodyTextColor) -> UIAppearanceSetting`                        | Set body text color                                              |
| `setHeadingTextColor(String headingTextColor) -> UIAppearanceSetting`                  | Set heading text color                                           |
| `setNavigationBarColor(String navigationBarColor) -> UIAppearanceSetting`              | Set navigation bar background color                              |
| `setNavigationBarTextColor(String navigationBarTextColor) -> UIAppearanceSetting`      | Set navigation bar text color                                    |
| `setIconTintColor(String iconTintColor) -> UIAppearanceSetting`                        | Set icon tint color                                              |
| `setSolidButtonAppearance(ButtonAppearance solidButton) -> UIAppearanceSettings`       | Set solid button appearance                                      |
| `setBorderedButtonAppearance(ButtonAppearance borderedButton) -> UIAppearanceSettings` | Set bordered button appearance                                   |

### AppearanceProvider
The `AppearanceProvider` can be customized with the following methods to make additional changes to the User Interface.

| Method                                                                                 | Description                                                      |
|----------------------------------------------------------------------------------------|------------------------------------------------------------------|
| `setShowSessionExpirationTimer(Boolean showTimer)`                                     | Whether to show session timer, defaults to `true`                |
| `setNavigationTitle(SpannableString navigationTitle)`                                  | Set custom title-text, works with `setLogoImage(null)`           |
| `setAttributedTexts(Map<String, SpannableString> values)`                              | Use stylable texts for title & description texts for screens     |

### Customizing icon tint color
If using custom image resources, set `PingOneVerifyClient.Builder(boolean isOverridingAssets)` to `true`.
When the `isOverridingAssets` flag is `true`, the icon tint color is not applied to the custom images.
If `isOverridingAssets` flag is set to `false`, the icon tint color is automatically applied to all the icon images.

### Customizing image resource
You can customize these images:

| Asset Name       | Description                                                            | 
|------------------|------------------------------------------------------------------------|
| idv_logo         | Logo image that appears at the center of the navigation bar            |
| idv_gov_id       | Image that is shown when government id is requested                    |                      
| idv_selfie       | Image that is shown when selfie is requested                           |
| idv_phone        | Image that is shown when phone number verification is requested        |
| idv_email        | Image that is shown when email verification is requested               |
| idv_back         | Image that is used as back button in the navigation bar                |
| idv_cancel       | Image that is used for cancel button at top left of the navigation bar |
| idv_upload_retry | Icon shown when document upload fails with retry attempts remaining    |
| idv_upload_error | Icon shown when verification fails for the uploaded document           |

### Customizing color resource
You can customize the following color resources:

| Color Name                   | Description                                                       | 
|------------------------------|-------------------------------------------------------------------|
| idv_progress_color           | Tint color for the progress UI shown during the document upload   |
| idv_document_upload_retry    | Icon tint for retry when document upload can be retried           |                      
| idv_document_upload_error    | Icon tint for error when uploaded document verification fails     |
| idv_selfie_guideline_default | Default border for oval face detector for selfie verification     |
| idv_selfie_guideline_success | Success border for oval face detector for selfie verification     |
| idv_selfie_guideline_error   | Error border for oval face detector for selfie verification       |
| idv_selfie_background        | Add color in ARGB to customize foreground for selfie verification |
| idv_selfie_close             | Icon tint for close/cancel button for selfie verification         |
| idv_selfie_error             | Text color for face recognition response for selfie verification  |

### Customizing localization (text)
For localization and messages, you can replace the values found in [localizable_strings.xml](https://github.com/pingidentity/pingone-wallet-mobile-sdk-android/blob/master/SampleCode/app/src/main/res/values/strings.xml).

To customize resources such as icons/images, colors, localization strings, etc.:
Include your custom resources such as drawables, colors, & strings in the respective `res` folder of your app


### Verify Policy



PingOne Verify Native SDK utilizes Verify Policies. You can apply policies two ways:


* Use [Verify Policies](https://apidocs.pingidentity.com/pingone/platform/v1/api/#verify-policies) from the PingOne platform API.


* Use **Policies** tab found in *PingOne Admin Console -> PingOne Verify -> Policies* to customize verify policy for a particular environment.
