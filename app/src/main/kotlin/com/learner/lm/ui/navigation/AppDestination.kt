package com.learner.lm.ui.navigation

enum class AppDestination(val title: String, val route: String) {
    Login("Account", "login"),
    Chat("Study Chat", "chat"),
    Scanner("Homework Scanner", "scanner"),
    Progress("Progress", "progress"),
    Profile("Profile", "profile"),
    Subscription("Upgrade", "subscription")
}
