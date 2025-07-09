package com.tyrads.sdk.acmo.core.utils

import android.content.Context
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.IntegrityTokenRequest
import java.util.UUID
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun getPlayIntegrityToken(context: Context): String = suspendCancellableCoroutine { continuation ->
    val integrityManager = IntegrityManagerFactory.create(context)
    val nonce = UUID.randomUUID().toString()
    val cloudProjectNumber = 986245594258;
    val request = IntegrityTokenRequest.builder()
        .setNonce(nonce)
        .setCloudProjectNumber(cloudProjectNumber)
        .build()

    integrityManager.requestIntegrityToken(request)
        .addOnSuccessListener { response ->
            continuation.resume(response.token())
        }
        .addOnFailureListener { exception ->
            continuation.resumeWithException(exception)
        }
}

