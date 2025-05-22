import 'package:flutter/material.dart';
import 'package:teya_pos_payment/teya_pos_payment.dart';
import 'package:uuid/uuid.dart';

void main() => runApp(const MyApp());

class MyApp extends StatelessWidget {
  const MyApp({super.key});
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: PaymentScreen(),
    );
  }
}

class PaymentScreen extends StatefulWidget {
  const PaymentScreen({super.key});

  @override
  State<PaymentScreen> createState() => _PaymentScreenState();
}

class _PaymentScreenState extends State<PaymentScreen>
    with WidgetsBindingObserver {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed) {
      _checkPaymentStatus();
    }
  }

  String _paymentMessage = 'Initiate a payment or check status.';
  final Uuid _uuid = Uuid();

  Future<void> _initiatePayment() async {
    setState(() {
      _paymentMessage = 'Initiating payment...';
    });
    try {
      final transactionId = _uuid.v4();
      final result = await TeyaPosPayment.startPayment(
        amount: 750.0,
        uuid: transactionId,
        invoiceRefs: 'ORDER-${DateTime.now().millisecondsSinceEpoch}',
        cardPaymentOff: false,
      );
      setState(() {
        _paymentMessage =
            'Payment initiated! Request ID: ${result['requestId']}';
      });
    } catch (e) {
      setState(() {
        _paymentMessage = 'Error initiating payment: $e';
      });
    }
  }

  Future<void> _checkPaymentStatus() async {
    setState(() {
      _paymentMessage = 'Checking payment status...';
    });
    try {
      final status = await TeyaPosPayment.getStatus();
      setState(() {
        _paymentMessage = 'Status: ${status['status']}\n';
        if (status.containsKey('amount')) {
          _paymentMessage +=
              'Amount: ${status['amount']} ${status['currency']}\n';
        }
        if (status.containsKey('message')) {
          _paymentMessage += 'Message: ${status['message']}\n';
        }
        if (status.containsKey('requestId')) {
          _paymentMessage += 'Request ID: ${status['requestId']}\n';
        }
      });
    } catch (e) {
      setState(() {
        _paymentMessage = 'Error checking status: $e';
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Teya POS Demo'),
      ),
      body: Center(
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Text(
                _paymentMessage,
                textAlign: TextAlign.center,
                style: const TextStyle(fontSize: 18),
              ),
              const SizedBox(height: 30),
              ElevatedButton(
                onPressed: _initiatePayment,
                child: const Text('Start 750 HUF Payment'),
              ),
              const SizedBox(height: 10),
              ElevatedButton(
                onPressed: _checkPaymentStatus,
                child: const Text('Check Last Payment Status'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
