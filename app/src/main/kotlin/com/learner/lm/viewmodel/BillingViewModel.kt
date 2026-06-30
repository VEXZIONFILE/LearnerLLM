package com.learner.lm.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.learner.lm.LearnerLMApplication
import com.learner.lm.auth.AuthRepository
import com.learner.lm.billing.BillingRepository
import com.learner.lm.billing.SubscriptionProducts
import com.learner.lm.billing.SubscriptionTier
import com.learner.lm.repository.LearnerApiConfig
import com.learner.lm.repository.LearnerProfileRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BillingViewModel(application: Application) : AndroidViewModel(application) {
    private val billingRepository = BillingRepository(application.applicationContext)
    private val authRepository = AuthRepository(
        context = application,
        userProfileDao = (application as LearnerLMApplication).database.userProfileDao()
    )
    private val profileRepository: LearnerProfileRepository? by lazy {
        if (LearnerApiConfig.isConfigured) {
            try {
                LearnerProfileRepository()
            } catch (_: Exception) {
                null
            }
        } else {
            null
        }
    }

    val billingState = billingRepository.state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), billingRepository.state.value)

    init {
        billingRepository.connect()
    }

    fun purchase(activity: Activity, productId: String) {
        billingRepository.launchPurchase(activity, productId)
    }

    fun formattedPrice(productId: String, fallback: String): String =
        billingRepository.formattedPrice(productId, fallback)

    fun syncSubscriptionToProfile(uid: String, productId: String?, purchaseToken: String?) {
        if (productId.isNullOrBlank()) return

        viewModelScope.launch {
            val tier = profileRepository?.verifyPurchase(
                productId = productId,
                purchaseToken = purchaseToken.orEmpty()
            )?.getOrNull() ?: localTierForProduct(productId)

            authRepository.updateSubscriptionTier(uid, tier)
        }
    }

    private fun localTierForProduct(productId: String?): String = when (productId) {
        SubscriptionProducts.PREMIUM_MONTHLY,
        SubscriptionProducts.PREMIUM_YEARLY -> SubscriptionTier.BASIC.name
        SubscriptionProducts.PRO_MONTHLY -> SubscriptionTier.PRO.name
        else -> SubscriptionTier.FREE.name
    }

    override fun onCleared() {
        billingRepository.disconnect()
        super.onCleared()
    }
}
