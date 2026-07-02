package com.learner.lm.repository

import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object NetworkErrors {
    fun isNetworkFailure(error: Throwable): Boolean {
        var current: Throwable? = error
        while (current != null) {
            when (current) {
                is IOException,
                is ConnectException,
                is SocketTimeoutException,
                is UnknownHostException -> return true
            }
            val message = current.message.orEmpty()
            if (
                message.contains("failed to connect", ignoreCase = true) ||
                message.contains("unable to resolve host", ignoreCase = true) ||
                message.contains("timeout", ignoreCase = true)
            ) {
                return true
            }
            current = current.cause
        }
        return false
    }

    fun friendlyMessage(error: Throwable): String = when {
        isNetworkFailure(error) ->
            "Can't reach the LearnerLM server. Using offline tutor mode — connect to the internet for full AI responses."
        error is HttpException && error.code() == 401 ->
            "Session expired. Sign out and sign in again."
        error is HttpException && error.code() == 403 ->
            "This account doesn't have access. Check your subscription."
        error is HttpException && error.code() == 429 ->
            "Daily message limit reached. Upgrade for more messages."
        error.message?.contains("LEARNER_API_BASE_URL", ignoreCase = true) == true ->
            "API not configured. Set LEARNER_API_BASE_URL in local.properties and rebuild."
        else ->
            error.message ?: "Something went wrong. Please try again."
    }
}
