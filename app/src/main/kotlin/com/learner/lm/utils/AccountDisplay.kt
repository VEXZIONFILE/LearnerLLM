package com.learner.lm.utils

import com.learner.lm.auth.UserProfile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AccountDisplay {
    fun handle(profile: UserProfile): String {
        val slug = profile.displayName
            .trim()
            .lowercase(Locale.US)
            .replace(Regex("[^a-z0-9]"), "")
        return if (slug.isNotBlank()) "@$slug" else "Learner member"
    }

    fun memberSince(profile: UserProfile): String {
        val year = SimpleDateFormat("yyyy", Locale.US).format(Date(profile.createdAt))
        return "Member since $year"
    }

    fun planLabel(tier: String): String = when (tier) {
        "PRO" -> "Premium Pro"
        "BASIC" -> "Premium"
        else -> "Standard"
    }
}
