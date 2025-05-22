package com.corelair.teya_pos_payment

import android.util.Log
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel.Result
import co.saltpay.epos.integrationlib.*
import co.saltpay.epos.models.request.*
import co.saltpay.epos.models.common.Currency
import co.saltpay.epos.models.response.*
import co.saltpay.epos.integrationlib.common.*
import java.math.BigDecimal
import java.util.*
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import android.app.Application


enum class TeyaCurrency(val alphaCode: String, val numericCode: String, val exponent: Int) {
    HUF("HUF", "348", 2),
    EUR("EUR", "978", 2),
    USD("USD", "840", 2),
    GBP("GBP", "826", 2),
    JPY("JPY", "392", 0),
    CHF("CHF", "756", 2);
}

class TeyaPosPaymentPluginImpl : FlutterPlugin, MethodCallHandler {

    private lateinit var channel: MethodChannel
    private lateinit var requestDispatcher: EposRequestDispatcherApi
    private var response: ResponseModel? = null

    object PayAppResponseListener: EposResponseListener {

        var res: ResponseModel? = null
        override fun onResponse(response: ResponseModel) {
            res = response
        }
    }

    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        val context = binding.applicationContext as Application
        requestDispatcher = EposRequestDispatcherApi.init(context, object : EposResponseListener {
            override fun onResponse(response: ResponseModel) {
                Log.d("TeyaPosPlugin", "Response: $response")
                this@TeyaPosPaymentPluginImpl.response = response
            }
        })
        channel = MethodChannel(binding.binaryMessenger, "teya_pos_payment/payment")
        channel.setMethodCallHandler(this)
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "startPaymentFromJavaCode" -> {
                val amount = call.argument<Double>("amount") ?: 0.0
                val billNumber = call.argument<String>("invoice_refs") ?: UUID.randomUUID().toString()
                val uuid = call.argument<String>("uuid") ?: UUID.randomUUID().toString()
                val cardPaymentOff = call.argument<Boolean>("card_payment_off") ?: false
                val currencyArg = call.argument<String>("currency")?.uppercase() ?: "HUF"

                requestDispatcher.request(
                    PayAppConfigRequest(
                        requestId = uuid,
                        receiptHandlingMode = PayAppConfigRequest.ReceiptHandlingMode.IncludedOnPaymentFlows,
                        allowPayAppTips = false
                    )
                )
                
                val currencyEnum = try {
                    TeyaCurrency.valueOf(currencyArg)
                } catch (e: Exception) {
                    TeyaCurrency.HUF
                }
                
                val currency = Currency(currencyEnum.alphaCode, currencyEnum.numericCode, currencyEnum.exponent)
                val requestModel = SalePayment(
                    requestId = uuid,
                    amount = BigDecimal.valueOf(amount),
                    tip = BigDecimal("0.0"),
                    currency = Currency("HUF", "348", 1)
                )

                response = null
                requestDispatcher.request(requestModel)
                result.success(mapOf("status" to "started", "requestId" to uuid))
            }

            "getStatus" -> {
                when (val res = response) {
                    is SalePaymentResponse.Approved -> {
                        result.success(mapOf(
                            "status" to "approved",
                            "requestId" to res.requestId?.toString(),
                            "amount" to res.amount.toString(),
                            "currency" to res.currency?.alphaCode
                        ))
                    }
                    is SalePaymentResponse.Failed -> {
                        result.success(mapOf(
                            "status" to "failed",
                            "requestId" to res.requestId?.toString(),
                            "message" to res.toString()
                        ))
                    }
                    else -> result.success(mapOf("status" to "pending"))
                }
            }

            else -> result.notImplemented()
        }
    }
}
