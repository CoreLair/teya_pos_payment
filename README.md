# teya_pos_payment

A Flutter plugin to seamlessly integrate with Teya POS terminals for on device payments.

---

## Table of Contents

* [Features](#features)
* [Installation](#installation)
* [Usage](#usage)
* [API Reference](#api-reference)
* [Example](#example)
* [Contributing](#contributing)
* [License](#license)
* [Support](#support)

---

## Features

* List the key features of your project in bullet points.
    * Initiate payments on Teya POS devices directly from your Flutter app.
    * Retrieve the status of the last payment transaction.
    * Supports configurable payment options like card-only or cash-only transactions.
    * Platform-agnostic (designed for Flutter, with Android implementation provided).

---

## Installation

### For Flutter Packages

1.  **Add the dependency** to your `pubspec.yaml` file:

    ```yaml
    dependencies:
      teya_pos_payment:
        git:
         url: https://github.com/CoreLair/teya_pos_payment.git
    ```

2.  **Run `flutter pub get`** to fetch the package.

---

## Usage

### Initializing the Plugin

```dart
// Import package
   import 'package:teya_pos_payment/teya_pos_payment.dart';
```

### [Specific Use Case 1: e.g., Starting a Payment]

```dart
import 'package:teya_pos_payment/teya_pos_payment.dart';
import 'package:uuid/uuid.dart'; // Recommended for generating unique UUIDs

Future<void> startTeyaPayment() async {
  try {
    final uuid = Uuid(); // Create a UUID generator instance
    final transactionId = uuid.v4(); // Generate a unique ID for this transaction

    final result = await TeyaPosPayment.startPayment(
      amount: 1250.75, // Amount in HUF (e.g., 1250.75 Ft)
      uuid: transactionId, // A unique identifier for this transaction
      invoiceRefs: 'INV-2024-001-A', // Your invoice or reference number
      cardPaymentOff: false, // Set to true to disable card payments (cash only)
    );
    print('Payment initiation result: $result');
    // Result will typically be {'status': 'started', 'requestId': '...'}.
    // You might want to store this requestId to query status later.
  } catch (e) {
    print('Failed to start payment: $e');
  }
}
```

### [Specific Use Case 2: e.g., Getting Payment Status]

```dart
import 'package:teya_pos_payment/teya_pos_payment.dart';

Future<void> checkLastPaymentStatus() async {
  try {
    final status = await TeyaPosPayment.getStatus();
    print('Current payment status: $status');

    if (status['status'] == 'approved') {
      print('Payment approved!');
      print('Amount: ${status['amount']} ${status['currency']}');
      print('Request ID: ${status['requestId']}');
    } else if (status['status'] == 'failed') {
      print('Payment failed!');
      print('Message: ${status['message']}');
    } else if (status['status'] == 'pending') {
      print('Payment is still pending...');
    } else {
      print('Unknown status or no active payment.');
    }
  } catch (e) {
    print('Error checking status: $e');
  }
}
```

---

## API Reference

Provide a quick reference to the main methods/classes. For more complex APIs, link to separate documentation.

### `TeyaPosPayment` Class

* **`static Future<Map<String, dynamic>> startPayment({required double amount, required String uuid, required String invoiceRefs, bool cardPaymentOff = false})`**
    * Initiates a payment on the Teya POS device.
    * Returns a `Map` indicating the status (`'started'`) and the `requestId`.
* **`static Future<Map<String, dynamic>> getStatus()`**
    * Retrieves the status of the last payment attempt.
    * Returns a `Map` with the payment status (`'approved'`, `'failed'`, `'pending'`, `'raw'`) and additional details.

---

## Example

```dart
import 'package:flutter/material.dart';
import 'package:teya_pos_payment/teya_pos_payment.dart';
import 'package:uuid/uuid.dart'; // Add uuid: ^latest_version to your pubspec.yaml

class PaymentScreen extends StatefulWidget {
  const PaymentScreen({super.key});

  @override
  State<PaymentScreen> createState() => _PaymentScreenState();
}

class _PaymentScreenState extends State<PaymentScreen> with WidgetsBindingObserver {

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
        _paymentMessage = 'Payment initiated! Request ID: ${result['requestId']}';
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
          _paymentMessage += 'Amount: ${status['amount']} ${status['currency']}\n';
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
```

---

## Contributing

We welcome contributions! If you have suggestions for improvements, find a bug, or want to add a new feature, please follow these steps:

1.  **Fork** the repository.
2.  **Create a new branch** for your feature or bug fix: `git checkout -b feature/your-feature-name` or `bugfix/fix-issue-number`.
3.  **Make your changes** and ensure tests pass (if applicable).
4.  **Commit your changes** with a clear and concise message: `git commit -m "feat: Add new awesome feature"` or `fix: Resolve issue with payment status handling`.
5.  **Push your branch** to your forked repository.
6.  **Open a Pull Request** to the `main` branch of this repository. Provide a detailed description of your changes.

---

## License

This project is licensed under the [MIT License](LICENSE) - see the `LICENSE` file for details.

---

## Support

If you encounter any issues or have questions, please feel free to:

* Open an [issue](https://github.com/CoreLair/teya_pos_payment/issues) on GitHub.
