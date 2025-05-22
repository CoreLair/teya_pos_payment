import 'package:flutter/services.dart';

class TeyaPosPayment {
  static const MethodChannel _channel =
      MethodChannel('teya_pos_payment/payment');

  static Future<Map<String, dynamic>> startPayment({
    required double amount,
    required String uuid,
    required String invoiceRefs,
    bool cardPaymentOff = false,
  }) async {
    final result = await _channel.invokeMethod('startPaymentFromJavaCode', {
      'amount': amount,
      'uuid': uuid,
      'invoice_refs': invoiceRefs,
      'card_payment_off': cardPaymentOff,
    });

    return Map<String, dynamic>.from(
        result is String ? {'requestId': result} : result);
  }

  static Future<Map<String, dynamic>> getStatus() async {
    final result = await _channel.invokeMethod('getStatus');

    if (result is String) {
      return {'status': 'raw', 'message': result};
    }

    return Map<String, dynamic>.from(result);
  }
}
