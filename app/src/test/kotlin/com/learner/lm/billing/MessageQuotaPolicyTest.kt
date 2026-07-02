package com.learner.lm.billing

import com.learner.lm.ai.AppMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MessageQuotaPolicyTest {
    @Test
    fun `free users get 60 messages per mode`() {
        assertTrue(MessageQuotaPolicy.canSend(0, SubscriptionTier.FREE.name))
        assertTrue(MessageQuotaPolicy.canSend(59, SubscriptionTier.FREE.name))
        assertFalse(MessageQuotaPolicy.canSend(60, SubscriptionTier.FREE.name))
    }

    @Test
    fun `pro users get 500 messages per mode`() {
        assertTrue(MessageQuotaPolicy.canSend(499, SubscriptionTier.BASIC.name))
        assertFalse(MessageQuotaPolicy.canSend(500, SubscriptionTier.BASIC.name))
    }

    @Test
    fun `premium users have unlimited messages per mode`() {
        assertTrue(MessageQuotaPolicy.canSend(1000, SubscriptionTier.PRO.name))
        assertNull(MessageQuotaPolicy.dailyLimit(SubscriptionTier.PRO.name))
    }

    @Test
    fun `quota label includes mode name`() {
        assertEquals(
            "1 of 60 Tutor messages left today",
            MessageQuotaPolicy.quotaLabel(59, SubscriptionTier.FREE.name, AppMode.TUTOR)
        )
    }
}
