package com.learner.lm.ui.screens

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.learner.lm.auth.UserProfile
import com.learner.lm.billing.SubscriptionCatalog
import com.learner.lm.billing.SubscriptionProducts
import com.learner.lm.ui.components.NotebookBadge
import com.learner.lm.ui.components.NotebookCard
import com.learner.lm.viewmodel.BillingViewModel

@Composable
fun SubscriptionScreen(
    userProfile: UserProfile?,
    modifier: Modifier = Modifier,
    billingViewModel: BillingViewModel = viewModel(),
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val activity = context as Activity
    val billingState by billingViewModel.billingState.collectAsStateWithLifecycle()

    LaunchedEffect(billingState.activeProductId, userProfile?.uid) {
        val uid = userProfile?.uid ?: return@LaunchedEffect
        billingViewModel.syncSubscriptionToProfile(uid, billingState.activeProductId)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Choose your plan",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Normal
        )
        Text(
            text = "Unlock LearnerLM tutoring with a plan that fits your learning goals.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (billingState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }

        billingState.error?.let { error ->
            Text(text = error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        billingState.purchaseMessage?.let { msg ->
            Text(text = msg, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
        }

        SubscriptionCatalog.plans.forEach { plan ->
            val livePrice = billingViewModel.formattedPrice(plan.productId, plan.price)
            val isActive = billingState.activeProductId == plan.productId

            NotebookCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = plan.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = plan.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (plan.badge != null) {
                            NotebookBadge(text = plan.badge, highlighted = true)
                        } else if (plan.isPopular) {
                            NotebookBadge(text = "Popular")
                        }
                    }

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = livePrice,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = plan.period,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                        )
                    }

                    if (plan.productId == SubscriptionProducts.PRO_YEARLY) {
                        Text(
                            text = "Pay for 10 months, get 12 — save \$40/year vs monthly Pro",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    plan.features.forEach { feature ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(text = feature, style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (isActive) {
                        OutlinedButton(
                            onClick = { },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = false
                        ) {
                            Text("Current plan")
                        }
                    } else {
                        Button(
                            onClick = { billingViewModel.purchase(activity, plan.productId) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (plan.isPopular || plan.badge != null)
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Text(
                                when (plan.productId) {
                                    SubscriptionProducts.BASIC_MONTHLY -> "Subscribe — $9.99/mo"
                                    SubscriptionProducts.PRO_MONTHLY -> "Subscribe — $19.99/mo"
                                    SubscriptionProducts.PRO_YEARLY -> "Subscribe — $239.88/yr"
                                    else -> "Subscribe"
                                }
                            )
                        }
                    }
                }
            }
        }

        Text(
            text = "Subscriptions are billed through Google Play. Cancel anytime in Play Store settings.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
