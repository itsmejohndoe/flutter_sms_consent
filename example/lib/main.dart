import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:sms_consent/sms_consent.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {

  @override
  _MyAppState createState() => _MyAppState();

}

class _MyAppState extends State<MyApp> {

  String _receivedCode = '-';

  @override
  void initState() {
    super.initState();
    initSMSState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initSMSState() async {
    String receivedCode;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      receivedCode = await SmsConsent.startSMSConsent();
    } on PlatformException {
      receivedCode = 'Failed to get the code.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() => _receivedCode = receivedCode);
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('SMS User Consent API Sample'),
        ),
        body: Center(
          child: Text('The received code is: $_receivedCode\n'),
        ),
      ),
    );
  }

  @override
  void dispose() {
    SmsConsent.stopSMSConsent();
    // Dispose call
    super.dispose();
  }

}
