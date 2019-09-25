#import "SmsConsentPlugin.h"
#import <sms_consent/sms_consent-Swift.h>

@implementation SmsConsentPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftSmsConsentPlugin registerWithRegistrar:registrar];
}
@end
