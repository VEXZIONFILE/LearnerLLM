package com.learner.lm.ui.navigation

enum class AppDestination(
    val title: String,
    val shortLabel: String,
    val route: String
) {
    Login("Account", "Account", "login"),
    Chat("Study Chat", "Chat", "chat"),
    Scanner("Scanner", "Scan", "scanner"),
    Progress("Progress", "Progress", "progress"),
    Profile("Profile", "Profile", "profile"),
    Subscription("Upgrade", "Plans", "subscription");

    val showsBottomNav: Boolean
        get() = this in bottomNavDestinations

    companion object {
        val bottomNavDestinations = listOf(Chat, Scanner, Progress, Profile)
    }
}
