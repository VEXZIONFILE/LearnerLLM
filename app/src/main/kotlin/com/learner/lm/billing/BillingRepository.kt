package com.learner.lm.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class BillingUiState(
    val isConnected: Boolean = false,
    val isLoading: Boolean = true,
    val productDetails: Map<String, ProductDetails> = emptyMap(),
    val activeProductId: String? = null,
    val error: String? = null,
    val purchaseMessage: String? = null
)

class BillingRepository(private val context: Context) : PurchasesUpdatedListener {

    private val _state = MutableStateFlow(BillingUiState())
    val state: StateFlow<BillingUiState> = _state.asStateFlow()

    private var billingClient: BillingClient? = null

    fun connect() {
        if (billingClient?.isReady == true) return

        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    _state.value = _state.value.copy(isConnected = true, error = null)
                    queryProducts()
                    queryActivePurchases()
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Billing unavailable: ${result.debugMessage}"
                    )
                }
            }

            override fun onBillingServiceDisconnected() {
                _state.value = _state.value.copy(isConnected = false)
            }
        })
    }

    private fun queryProducts() {
        val client = billingClient ?: return
        val productList = SubscriptionProducts.allProductIds.map { productId ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        client.queryProductDetailsAsync(params) { result, details ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                val map = details.associateBy { it.productId }
                _state.value = _state.value.copy(
                    productDetails = map,
                    isLoading = false,
                    error = null
                )
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Could not load plans. Set up products in Google Play Console."
                )
            }
        }
    }

    private fun queryActivePurchases() {
        val client = billingClient ?: return
        client.queryPurchasesAsync(
            com.android.billingclient.api.QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        ) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                val active = purchases.firstOrNull { it.purchaseState == Purchase.PurchaseState.PURCHASED }
                _state.value = _state.value.copy(
                    activeProductId = active?.products?.firstOrNull()
                )
            }
        }
    }

    fun launchPurchase(activity: Activity, productId: String) {
        val client = billingClient ?: return
        val productDetails = _state.value.productDetails[productId]
        if (productDetails == null) {
            _state.value = _state.value.copy(
                error = "Plan not available yet. Configure $productId in Google Play Console."
            )
            return
        }

        val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken
        if (offerToken == null) {
            _state.value = _state.value.copy(error = "No subscription offer found for this plan.")
            return
        }

        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
            .setOfferToken(offerToken)
            .build()

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        client.launchBillingFlow(activity, flowParams)
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { handlePurchase(it) }
                _state.value = _state.value.copy(purchaseMessage = "Subscription activated!")
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _state.value = _state.value.copy(purchaseMessage = null)
            }
            else -> {
                _state.value = _state.value.copy(
                    error = "Purchase failed: ${result.debugMessage}"
                )
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient?.acknowledgePurchase(params) { }
            }
            _state.value = _state.value.copy(
                activeProductId = purchase.products.firstOrNull()
            )
        }
    }

    fun formattedPrice(productId: String, fallback: String): String {
        val details = _state.value.productDetails[productId] ?: return fallback
        return details.subscriptionOfferDetails
            ?.firstOrNull()
            ?.pricingPhases
            ?.pricingPhaseList
            ?.firstOrNull()
            ?.formattedPrice
            ?: fallback
    }

    fun disconnect() {
        billingClient?.endConnection()
        billingClient = null
    }
}
