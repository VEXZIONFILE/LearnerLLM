package com.learner.lm.billing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScanQuotaPolicyTest {

    @Test
    fun `free tier allows up to 3 scans`() {
        assertTrue(ScanQuotaPolicy.canScan(0, isPremium = false))
        assertTrue(ScanQuotaPolicy.canScan(2, isPremium = false))
        assertFalse(ScanQuotaPolicy.canScan(3, isPremium = false))
    }

    @Test
    fun `premium tier has unlimited scans`() {
        assertTrue(ScanQuotaPolicy.canScan(0, isPremium = true))
        assertTrue(ScanQuotaPolicy.canScan(100, isPremium = true))
        assertNull(ScanQuotaPolicy.remainingScans(50, isPremium = true))
    }

    @Test
    fun `remaining scans counts down for free tier`() {
        assertEquals(3, ScanQuotaPolicy.remainingScans(0, isPremium = false))
        assertEquals(1, ScanQuotaPolicy.remainingScans(2, isPremium = false))
        assertEquals(0, ScanQuotaPolicy.remainingScans(3, isPremium = false))
    }

    @Test
    fun `quota label reflects tier`() {
        assertEquals("Unlimited scans", ScanQuotaPolicy.quotaLabel(10, isPremium = true))
        assertEquals("2 of 3 scans left today", ScanQuotaPolicy.quotaLabel(1, isPremium = false))
    }
}
