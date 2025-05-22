
import 'package:flutter/services.dart';

class TeyaPosPayment {
  static const MethodChannel _channel = MethodChannel('teya_pos_payment');

  static Future<Map<String, dynamic>> startPayment({
    required int amount,
    String currency = "HUF",
  }) async {
    final result = await _channel.invokeMethod('startPayment', {
      'amount': amount,
      'currency': currency,
    });
    return Map<String, dynamic>.from(result);
  }
}
