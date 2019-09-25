package dev.johndoe.sms_consent

import android.app.Activity
import android.content.*
import android.text.TextUtils
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.common.PluginRegistry.Registrar
import io.flutter.view.FlutterNativeView

class SmsConsentPlugin(private val activity: Activity) : MethodCallHandler, PluginRegistry.ActivityResultListener, PluginRegistry.ViewDestroyListener {

  companion object {
    @JvmStatic
    fun registerWith(registrar: Registrar) {
      val channel = MethodChannel(registrar.messenger(), "sms_consent")
      val plugin = SmsConsentPlugin(registrar.activity())
      channel.setMethodCallHandler(plugin)
      registrar.addActivityResultListener(plugin)
      registrar.addViewDestroyListener(plugin)
    }
  }

  private val SMS_CONSENT_REQUEST = 2332
  private val smsVerificationReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      // Check if the receiver is already unregistered, because if it is, we can't ask the user to consent
      if (!isSMSConsentRegistered) return
      // Normal flow since the receiver is still registered
      if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
        val extras = intent.extras
        val smsRetrieverStatus = extras?.get(SmsRetriever.EXTRA_STATUS) as Status
        when (smsRetrieverStatus.statusCode) {
          CommonStatusCodes.SUCCESS -> {
            // Get consent intent
            val consentIntent = extras.getParcelable<Intent>(SmsRetriever.EXTRA_CONSENT_INTENT)
            try {
              // Start activity to show consent dialog to user, activity must be started in
              // 5 minutes, otherwise you'll receive another TIMEOUT intent
              activity.startActivityForResult(consentIntent, SMS_CONSENT_REQUEST)
            } catch (e: ActivityNotFoundException) {
              unregisterReceiver()
              // Time out occurred, handle the error.
              awaitingResult?.error("activity_not_found", null, null)
            }
          }
          CommonStatusCodes.TIMEOUT -> {
            unregisterReceiver()
            // Time out occurred, handle the error.
            awaitingResult?.error("timeout", null, null)
          }
        }
      }
    }
  }
  private var awaitingResult: Result? = null
  private var isSMSConsentRegistered = false

  override fun onMethodCall(call: MethodCall, result: Result) {
    when {
        call.method == "startSMSConsent" -> {
          // Check for sender number
          val senderPhoneNumber = if (call.hasArgument("senderPhoneNumber")) {
            call.argument<String>("senderPhoneNumber")
          } else {
            null
          }
          // Start SMS Retriever
          val task = SmsRetriever.getClient(activity).startSmsUserConsent(senderPhoneNumber)
          task.addOnSuccessListener {
            // Start listening to the broadcast that will warn us that a code has arrived
            val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
            activity.registerReceiver(smsVerificationReceiver, intentFilter)
            isSMSConsentRegistered = true
            // Store the result since we need it to delivery the result later
            this.awaitingResult = result
          }
          task.addOnFailureListener {
            result.error("generic_error", null, null)
          }
        }
        call.method == "stopSMSConsent" -> {
          // Just stop listening the SMS Receiver
          unregisterReceiver()
          // Result as success
          result.success(null)
        }
        else -> result.notImplemented()
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
    when (requestCode) {
      SMS_CONSENT_REQUEST ->
        if (resultCode == Activity.RESULT_OK && data != null) {
          // Get SMS message content
          val message = data.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
          // The message contains the entire message and we need to extract the number from the text
          val formattedMessage = message.replace("[^\\p{ASCII}]".toRegex(), "")
          val oneTimeCode = onlyDigitFromString(formattedMessage)
          // Return to the flutter
          awaitingResult?.success(oneTimeCode)
          awaitingResult = null
          // Remove register since we won't use it anymore
          unregisterReceiver()
        } else {
          unregisterReceiver()
          // Consent denied. User can type OTC manually.
          // Fallback as canceled to flutter
          awaitingResult?.error("canceled", null, null)
        }
    }
    return true
  }

  override fun onViewDestroy(view: FlutterNativeView?): Boolean {
    unregisterReceiver()
    return true
  }

  private fun unregisterReceiver() {
    if (isSMSConsentRegistered) {
      isSMSConsentRegistered = false
      activity.unregisterReceiver(smsVerificationReceiver)
    }
  }

  private fun onlyDigitFromString(text: String): String {
    return if (TextUtils.isEmpty(text)) "" else text.replace("\\D+".toRegex(), "")
  }

}
