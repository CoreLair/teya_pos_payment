# [YOUR_REPO_NAME]

A concise and informative description of your project. What does it do? What problem does it solve? (e.g., "A Flutter plugin to seamlessly integrate with Teya POS terminals for in-app payments.")

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
    * (e.g., "Initiate payments on Teya POS devices directly from your Flutter app.")
    * (e.g., "Retrieve the status of the last payment transaction.")
    * (e.g., "Supports configurable payment options like card-only or cash-only transactions.")
    * (e.g., "Platform-agnostic (designed for Flutter, with Android implementation provided).")

---

## Installation

### For Flutter Packages

1.  **Add the dependency** to your `pubspec.yaml` file:

    ```yaml
    dependencies:
      [your_package_name]: ^latest_version # e.g., teya_pos_payment: ^1.0.0
    ```

2.  **Run `flutter pub get`** to fetch the package.

### Android Specific Setup

If your project requires platform-specific setup (like a Flutter plugin needing Android or iOS configurations):

This plugin relies on the Teya (formerly SaltPay) ePOS integration library. You'll need to ensure your Android project is correctly configured to use their SDK. This typically involves:

* **Adding necessary permissions** to your `android/app/src/main/AndroidManifest.xml` file. (Refer to Teya's official ePOS SDK documentation for exact requirements).
* **Ensuring the minimum SDK version** in your `android/app/build.gradle` matches the ePOS SDK requirements (e.g., `minSdkVersion 21`).
* **Any other specific setup** as per Teya's integration guide.

---

## Usage

A clear, step-by-step guide on how to use your project. Provide code snippets.

### Initializing the Plugin (if applicable)

If your plugin needs initialization, explain it here.

```dart
// Example: No explicit initialization needed for TeyaPosPayment in Flutter
// as it relies on static methods and Android setup.
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

Show a complete, runnable example, perhaps a simple Flutter screen.

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

* Open an [issue](https://github.com/[YOUR_USERNAME]/[YOUR_REPO_NAME]/issues) on GitHub.
* (Optional: Add a link to your email, Discord, or other support channels if applicable.)