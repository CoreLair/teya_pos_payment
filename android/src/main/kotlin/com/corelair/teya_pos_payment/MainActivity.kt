package com.corelair.teya_pos_payment
import android.os.Bundle
import io.flutter.embedding.android.FlutterActivity
import io.flutter.plugin.common.MethodChannel
import co.saltpay.epos.integrationlib.EposRequestDispatcherApi
import android.app.Application
import android.util.Log
import co.saltpay.epos.models.request.SalePayment
import co.saltpay.epos.models.request.ActiveSummaryRequest
import java.util.UUID
import java.math.BigDecimal
import co.saltpay.epos.models.request.PayAppConfigRequest
import co.saltpay.epos.models.request.PayAppConfigRequest.ReceiptHandlingMode
import co.saltpay.epos.integrationlib.common.FailureSendingRequest
import co.saltpay.epos.models.common.Currency
import co.saltpay.epos.integrationlib.common.SentSuccessfully
import android.content.Intent
import io.flutter.embedding.engine.FlutterEngine;
import co.saltpay.epos.integrationlib.EposResponseListener
import co.saltpay.epos.models.response.SalePaymentResponse
import co.saltpay.epos.models.response.ResponseModel
import co.saltpay.epos.models.response.PayAppConfigResponse
import org.json.JSONObject

class MainActivity: FlutterActivity() {

    private val CHANNEL = "teya_pos_payment/payment"
    lateinit var requestDispatcher: EposRequestDispatcherApi
    lateinit var requestId: String

    object PayAppResponseListener: EposResponseListener {

        var res: ResponseModel? = null
        override fun onResponse(response: ResponseModel) {

            Log.d("LOG", "PayAppResponseListener.onResponse")

            when (response) {
                is SalePaymentResponse.Approved -> Log.d("LOG", response.toString())
                is SalePaymentResponse.Failed -> Log.d("LOG", response.toString())
                is PayAppConfigResponse -> Log.d("LOG", response.toString())
                else -> Log.d("LOG", "ELSE - $response")
            }

            res = response
        }
    }

    override fun onResume() {
        super.onResume()

        if (PayAppResponseListener::res != null) {
            Log.d("LOG", "ONRESUME P: " + PayAppResponseListener.res.toString())
            Log.d("LOG", "ONRESUME P2: " + PayAppResponseListener.res?.requestId?.toString())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("LOG", "onCreate")

        val application = application

        requestDispatcher = EposRequestDispatcherApi.init(
            application,
            PayAppResponseListener
        )

        flutterEngine?.dartExecutor?.binaryMessenger?.let { messenger ->
            MethodChannel(messenger, CHANNEL).setMethodCallHandler { call, result ->
                when (call.method) {
                    "startPaymentFromJavaCode" -> {

                        val amount = call.argument<Double>("amount") ?: 0
                        val billNumber = call.argument<String>("invoice_refs") ?: UUID.randomUUID().toString();
                        val uuid = call.argument<String>("uuid") ?: "";
                        val cardPaymentOff = call.argument<Boolean>("card_payment_off") ?: false;

                        val reqResult = requestDispatcher.request(
                            PayAppConfigRequest(
                                requestId = uuid,
                                receiptHandlingMode = ReceiptHandlingMode.IncludedOnPaymentFlows,
                                allowPayAppTips = false
                            )
                        )

                        Log.d("LOG", reqResult.toString())

                        requestId = uuid

                        val requestModel = SalePayment(
                            requestId = requestId,
                            amount = BigDecimal(amount.toString()),
                            tip = BigDecimal("0.0"),
                            currency = Currency(
                                alphaCode = "HUF",
                                decimals = 1,
                                numericCode = "348"
                            )
                        )

                        Log.d("LOG", requestModel.toString())

                        when (reqResult) {
                            is SentSuccessfully -> {
                                Log.d("LOG", SentSuccessfully.toString())
                            }
                            is FailureSendingRequest.HandlerAppNotAvailable -> {
                                Log.d("LOG", "Install a supported pay app!")
                            }
                            is FailureSendingRequest.Unknown -> {
                                Log.d("LOG", "Error making request $requestModel, error= ${reqResult.throwable.message}")
                            }
                            else -> {
                                Log.d("LOG", "Error making request $requestModel, error= ${reqResult.toString()}")
                                Log.d("LOG", "### ELSE")
                            }
                        }
                        PayAppResponseListener.res = null
                        val reqRes = requestDispatcher.request(requestModel)
                        Log.d("LOG", "reqRes: $reqRes")

                        result.success(requestId)
                    }
                    "getStatus" -> {
                        when (val res = PayAppResponseListener.res) {
                            is SalePaymentResponse.Approved -> {
                                val map = mapOf(
                                    "status" to "approved",
                                    "requestId" to res.requestId?.toString(),
                                    "amount" to res.amount.toString(),
                                    "currency" to res.currency?.alphaCode
                                )
                                result.success(map)
                            }
                            is SalePaymentResponse.Failed -> {
                                val map = mapOf(
                                    "status" to "failed",
                                    "requestId" to res.requestId?.toString(),
                                    "message" to res.toString()
                                )
                                result.success(map)
                            }
                            else -> {
                                result.success(mapOf("status" to "pending"))
                                Log.d("LOG", "GET STATUS NULL")
                            }
                        }
                    }
                    else -> {
                        result.notImplemented()
                    }
                }

            }
        } ?: run {
            throw IllegalStateException("Flutter engine is not initialized")
        }
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        Log.d("LOG", "configureFlutterEngine")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d("LOG", "onActivityResult")
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d("LOG", "onNewIntent");
    }
}