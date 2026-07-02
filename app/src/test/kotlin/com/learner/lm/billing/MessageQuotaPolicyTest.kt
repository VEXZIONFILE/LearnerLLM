package com.learner.lm.billing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MessageQuotaPolicyTest {
    @Test
    fun `free users can send under daily limit`() {
        assertTrue(MessageQuotaPolicy.canSend(0, isPremium = false))
        assertTrue(MessageQuotaPolicy.canSend(24, isPremium = false))
        assertFalse(MessageQuotaPolicy.canSend(25, isPremium = false))
    }

    @Test
    fun `premium users have unlimited messages`() {
        assertTrue(MessageQuotaPolicy.canSend(100, isPremium = true))
        assertEquals(null, MessageQuotaPolicy.remainingMessages(100, isPremium = true))
    }

    @Test
    fun `quota label reflects remaining messages`() {
        assertEquals(
            "1 of 25 messages left today",
            MessageQuotaPolicy.quotaLabel(24, isPremium = false)
        )
    }
}
