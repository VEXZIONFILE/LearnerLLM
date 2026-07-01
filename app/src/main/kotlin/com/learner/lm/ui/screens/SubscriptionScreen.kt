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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import com.learner.lm.billing.SubscriptionFeatures
import com.learner.lm.billing.SubscriptionProducts
import com.learner.lm.billing.SubscriptionTier
import com.learner.lm.ui.components.NotebookBadge
import com.learner.lm.ui.components.NotebookCard
import com.learner.lm.ui.components.PlanComparisonTable
import com.learner.lm.ui.components.PremiumValueProposition
import com.learner.lm.ui.theme.AppColors
import com.learner.lm.ui.theme.AppRadii
import com.learner.lm.ui.theme.AppSpacing
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
    val isPremium = userProfile?.subscriptionTier == SubscriptionTier.BASIC.name ||
        userProfile?.subscriptionTier == SubscriptionTier.PRO.name

    LaunchedEffect(billingState.activeProductId, billingState.activePurchaseToken, userProfile?.uid) {
        val uid = userProfile?.uid ?: return@LaunchedEffect
        billingViewModel.syncSubscriptionToProfile(
            uid = uid,
            productId = billingState.activeProductId,
            purchaseToken = billingState.activePurchaseToken
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(AppSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
    ) {
        Text(
            text = if (isPremium) "Your plan" else "Upgrade your AI tutor",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        onBack?.let { back ->
            androidx.compose.material3.TextButton(onClick = back) {
                Text("← Back to sign in")
            }
        }
        Text(
            text = "Frontier AI models, unlimited scans, and deeper learning tools.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        PremiumValueProposition()

        PlanComparisonTable()

        if (!isPremium) {
            NotebookCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Premium exclusive",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.ProGold
                    )
                    SubscriptionFeatures.premiumExclusiveBenefits.forEach { benefit ->
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = AppColors.ProGold,
                                modifier = Modifier
                                    .padding(top = 2.dp, end = 8.dp)
                                    .size(18.dp)
                            )
                            Text(
                                text = benefit,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        if (billingState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }

        billingState.error?.let { error ->
            Text(text = error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        billingState.purchaseMessage?.let { msg ->
            Surface(
                shape = RoundedCornerShape(AppRadii.md),
                color = AppColors.AccentLight.copy(alpha = 0.5f)
            ) {
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Text(
            text = "Choose a plan",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 4.dp)
        )

        SubscriptionCatalog.plans.forEach { plan ->
            val livePrice = billingViewModel.formattedPrice(plan.productId, plan.price)
            val isActive = billingState.activeProductId == plan.productId
            val isFeatured = plan.isPopular || plan.badge != null

            NotebookCard(elevated = isFeatured) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = plan.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = plan.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        when {
                            plan.badge != null -> NotebookBadge(text = plan.badge, highlighted = true)
                            plan.isPopular -> NotebookBadge(text = "Popular", highlighted = true)
                        }
                    }

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = livePrice,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = plan.period,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
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
                            enabled = false,
                            shape = RoundedCornerShape(AppRadii.md)
                        ) {
                            Text("Current plan", fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        Button(
                            onClick = { billingViewModel.purchase(activity, plan.productId) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(AppRadii.md),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                when (plan.productId) {
                                    SubscriptionProducts.PRO_MONTHLY -> "Subscribe — $9.99/mo"
                                    SubscriptionProducts.PREMIUM_MONTHLY -> "Subscribe — $19.99/mo"
                                    SubscriptionProducts.MEGA_YEARLY -> "Subscribe — $99.99/yr"
                                    else -> "Subscribe"
                                },
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        NotebookCard(elevated = false) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Standard plan includes",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "• 3 homework scans per day\n• All 3 AI models\n• Standard-depth responses",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text(
            text = "Billed through Google Play. Cancel anytime in Play Store settings.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
