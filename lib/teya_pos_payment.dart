import 'package:flutter/services.dart';

class TeyaPosPayment {
  static const MethodChannel _channel =
      MethodChannel('teya_pos_payment/payment');

  /// Elindítja a fizetést a Teya eszközön.
  ///
  /// [amount] - összeg HUF-ban (pl. 1000.0 = 1000 Ft)
  /// [uuid] - egyedi tranzakció-azonosító
  /// [invoiceRefs] - számla azonosító vagy hivatkozás
  /// [cardPaymentOff] - ha true, csak készpénz
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

    if (result is Map) {
      return Map<String, dynamic>.from(result);
    } else if (result is String) {
      return {'status': 'started', 'requestId': result};
    } else {
      throw Exception('Ismeretlen válasz: $result');
    }
  }

  /// Visszaadja a legutóbbi fizetés állapotát.
  ///
  /// Lehetséges státuszok: `approved`, `failed`, `pending`
  static Future<Map<String, dynamic>> getStatus() async {
    final result = await _channel.invokeMethod('getStatus');

    if (result is Map) {
      return Map<String, dynamic>.from(result);
    } else if (result is String) {
      return {'status': 'raw', 'message': result};
    } else {
      throw Exception('Ismeretlen státusz válasz: $result');
    }
  }
}
