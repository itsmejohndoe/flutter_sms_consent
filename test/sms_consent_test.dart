import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:sms_consent/sms_consent.dart';

void main() {

  const MethodChannel channel = MethodChannel('sms_consent');

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '233434';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('startSMSConsent', () async {
    expect(await SmsConsent.startSMSConsent(), '233434');
  });

}
