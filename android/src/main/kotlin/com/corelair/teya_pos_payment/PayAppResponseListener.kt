package eu.urbanticketing.hopon

import co.saltpay.epos.integrationlib.EposResponseListener
import co.saltpay.epos.models.response.SalePaymentResponse
import co.saltpay.epos.models.response.ResponseModel
import android.util.Log
//import co.saltpay.epos.models.response.EposDisableResponse
//import co.saltpay.epos.models.response.LibrariesVersionMismatch
//import co.saltpay.epos.models.response.PayAppBusy
import co.saltpay.epos.models.response.PayAppConfigResponse
//import co.saltpay.epos.models.response.PayAppNotConfiguredError

object PayAppResponseListener: EposResponseListener {

    lateinit var res: ResponseModel

    override fun onResponse(response: ResponseModel) {
        when (response) {
            is SalePaymentResponse.Approved -> Log.d("LOG", response.toString()) //Log.d("LOG", "APPROVED")
            is SalePaymentResponse.Failed -> Log.d("LOG", response.toString())
            //is EposDisableResponse -> print("EposDisableResponse")
            //is LibrariesVersionMismatch -> print("LibrariesVersionMismatch")
            //is PayAppBusy -> print("PayAppBusy")
            is PayAppConfigResponse -> Log.d("LOG", response.toString())
            //is PayAppNotConfiguredError -> print("PayAppNotConfiguredError")
            //is Failure -> print("Failure")
            //is Success -> print("Success")
            else -> Log.d("LOG", "ELSE - $response")
        }

        res = response
    }    
}