# sms_consent

A Flutter plugin that enable the use of SMS User Consent API.

## Only Android Support

Since iOS have his own way to make easier to users to input the incoming OTP, this package only target the ANDROID OS.

[To understand the flow and the API check this link](https://developers.google.com/identity/sms-retriever/user-consent/request)

## Installation

Just follow the basic installation of any flutter plugin by adding the package to the project's pubspec.yaml.

On your Flutter project #
See the installation instructions on pub.

## Using this amazing plugin

It's a very easy thing, trust me. With just 1 line you make the SMS User Consent API work on your app.

```dart
await SmsConsent.startSMSConsent(senderPhoneNumber: '321321312');
```
OR
```dart
await SmsConsent.startSMSConsent();
```

## Part of the journey is the end

When you ended with the plugin, is important to stop the Consent Service. Not calling the method below may result in unexpected behaviour in your app!

```dart
await SmsConsent.stopSMSConsent();
```

## Considerations

You need to call the plugin before requesting the OTP, since the SMS User Consent API will ONLY show SMS that are received after the initialization of it. In other words: the API doesn't show old messages.



