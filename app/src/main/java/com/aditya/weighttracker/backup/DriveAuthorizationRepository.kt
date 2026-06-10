package com.aditya.weighttracker.backup

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.tasks.await

class DriveAuthorizationRepository(context: Context) {
    private val appContext = context.applicationContext
    private val client = Identity.getAuthorizationClient(appContext)

    suspend fun authorize(): DriveAuthorizationState {
        val result = client.authorize(authorizationRequest()).await()
        return result.toState()
    }

    fun authorizationResultFromIntent(data: Intent?): DriveAuthorizationState {
        val result = client.getAuthorizationResultFromIntent(data)
        return result.toState()
    }

    private fun AuthorizationResult.toState(): DriveAuthorizationState {
        val resolution = pendingIntent
        val token = accessToken
        return when {
            resolution != null -> DriveAuthorizationState.NeedsResolution(resolution)
            !token.isNullOrBlank() -> DriveAuthorizationState.Authorized(token)
            else -> DriveAuthorizationState.Failed("Google Drive authorization did not return an access token.")
        }
    }

    private fun authorizationRequest(): AuthorizationRequest =
        AuthorizationRequest.builder()
            .setRequestedScopes(listOf(Scope(DRIVE_APPDATA_SCOPE)))
            .build()

    companion object {
        const val DRIVE_APPDATA_SCOPE = "https://www.googleapis.com/auth/drive.appdata"
    }
}

sealed interface DriveAuthorizationState {
    data class Authorized(val accessToken: String) : DriveAuthorizationState
    data class NeedsResolution(val pendingIntent: PendingIntent) : DriveAuthorizationState
    data class Failed(val message: String) : DriveAuthorizationState
}
