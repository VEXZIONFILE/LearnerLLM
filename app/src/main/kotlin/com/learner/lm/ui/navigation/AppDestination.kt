package com.learner.lm.ui.navigation

enum class AppDestination(
    val title: String,
    val shortLabel: String,
    val route: String
) {
    Login("Sign In", "Auth", "login"),
    Chat("Learner", "Chat", "chat"),
    Scanner("Homework Scan", "Scan", "scanner"),
    Progress("Progress", "Progress", "progress"),
    Profile("My Account", "Account", "profile"),
    Subscription("Premium", "Plans", "subscription");

    val showsBottomNav: Boolean
        get() = this in bottomNavDestinations

    companion object {
        val bottomNavDestinations = listOf(Chat, Scanner, Progress, Profile)
    }
}
