package com.streaminghell.app.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.android.billingclient.api.*
import com.streaminghell.app.R

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : Activity(), PurchasesUpdatedListener {
    private lateinit var billingClient: BillingClient
    private lateinit var donateSkuDetails: SkuDetails

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        donateButton.setOnClickListener {
            val params = BillingFlowParams
                .newBuilder()
                .setSkuDetails(donateSkuDetails)
                .build()

            billingClient.launchBillingFlow(this@MainActivity, params)
        }

        billingClient = BillingClient.newBuilder(this).setListener(this).build()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(responseCode: Int) {
                if (responseCode == BillingClient.BillingResponse.OK) {
                    println("BILLING RESPONSE: OK")
                }

                onBillingCheckSuccess()
            }

            override fun onBillingServiceDisconnected() {
            }
        })
    }

    override fun onPurchasesUpdated(responseCode: Int, purchases: MutableList<Purchase>?) {
        if (responseCode == BillingClient.BillingResponse.OK && purchases != null) {
            showAlert(getString(R.string.alert_success), getString(R.string.alert_success_message), false)
        } else if (responseCode == BillingClient.BillingResponse.ITEM_ALREADY_OWNED) {
            showAlert(getString(R.string.alert_info), getString(R.string.alert_info_message), false)
        } else if (responseCode != BillingClient.BillingResponse.USER_CANCELED) {
            showAlert(getString(R.string.alert_error), getString(R.string.alert_error_message), true)
        }
    }

    private fun showAlert(title: String, message: String, isNegative: Boolean) {
        val alert = AlertDialog.Builder(this@MainActivity)

        alert.setTitle(title)
        alert.setMessage(message)
        alert.setCancelable(true)

        if (isNegative) {
            alert.setNegativeButton(getString(R.string.alert_got_it)) { dialog, _ -> dialog.dismiss() }
        } else {
            alert.setPositiveButton(getString(R.string.alert_got_it)) { dialog, _ -> dialog.dismiss() }
        }

        alert.show()
    }

    private fun onBillingCheckSuccess() {
        val params = SkuDetailsParams
            .newBuilder()
            .setSkusList(arrayListOf("standard_donation"))
            .setType(BillingClient.SkuType.INAPP)

        billingClient.querySkuDetailsAsync(params.build()) { responseCode, skuDetailsList ->
            println("querySku: $responseCode, $skuDetailsList")

            if (responseCode != BillingClient.BillingResponse.OK || skuDetailsList.size != 1) {
                return@querySkuDetailsAsync
            }

            donateSkuDetails = skuDetailsList[0]

            donateButton.isEnabled = true
        }
    }
}
