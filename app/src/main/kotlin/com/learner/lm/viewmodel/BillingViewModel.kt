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

    fun syncSubscriptionToProfile(uid: String, productId: String?) {
        val tier = when (productId) {
            SubscriptionProducts.BASIC_MONTHLY -> SubscriptionTier.BASIC.name
            SubscriptionProducts.PRO_MONTHLY, SubscriptionProducts.PRO_YEARLY -> SubscriptionTier.PRO.name
            else -> SubscriptionTier.FREE.name
        }
        viewModelScope.launch {
            authRepository.updateSubscriptionTier(uid, tier)
        }
    }

    override fun onCleared() {
        billingRepository.disconnect()
        super.onCleared()
    }
}
