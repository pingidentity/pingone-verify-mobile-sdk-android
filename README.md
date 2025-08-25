# IDV_SDK_android
Prerequisites
- Android SDK 26 and up



## Initializing PingOneVerifyClient
Implement the `DocumentSubmissionListener` interface and its functions
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

Instantiate a PingOneVerifyClient.Builder and set its listener and Activity
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
| `setBodyTextColor(String bodyTextColor) -> UIAppearanceSetting`                        | Set body text color                                              |
| `setHeadingTextColor(String headingTextColor) -> UIAppearanceSetting`                  | Set heading text color                                           |
| `setNavigationBarColor(String navigationBarColor) -> UIAppearanceSetting`              | Set navigation bar background color                              |
| `setNavigationBarTextColor(String navigationBarTextColor) -> UIAppearanceSetting`      | Set navigation bar text color                                    |
| `setIconTintColor(String iconTintColor) -> UIAppearanceSetting`                        | Set icon tint color                                              |
| `setSolidButtonAppearance(ButtonAppearance solidButton) -> UIAppearanceSettings`       | Set solid button appearance                                      |
| `setBorderedButtonAppearance(ButtonAppearance borderedButton) -> UIAppearanceSettings` | Set bordered button appearance                                   |

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
