import 'dart:async';

import 'package:flutter/services.dart';

class SmsConsent {

  static const MethodChannel _channel = const MethodChannel('sms_consent');

  static Future<String> startSMSConsent({String senderPhoneNumber}) async {
    final String oneTimeCode = await _channel.invokeMethod('startSMSConsent', senderPhoneNumber);
    return oneTimeCode;
  }

  static Future<void> stopSMSConsent() async {
    return await _channel.invokeMethod('stopSMSConsent');
  }

}
