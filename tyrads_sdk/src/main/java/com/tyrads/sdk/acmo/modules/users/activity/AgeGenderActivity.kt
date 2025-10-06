package com.tyrads.sdk.acmo.modules.users.activity

import AcmoUsersUpdatePage
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.tyrads.sdk.acmo.core.AcmoOnboardingGate

class AcmoUsersUpdateActivity : ComponentActivity() {
    companion object {
        fun start(context: Context, actionType: Boolean) {
            val intent = Intent(context, AcmoUsersUpdateActivity::class.java)
            intent.putExtra("returnToWidget", actionType)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val actionType = intent.getBooleanExtra("returnToWidget", false)
        setContent {
            AcmoUsersUpdatePage(
                onComplete = {
                    finish()
                    AcmoOnboardingGate.continueFlow(this)
                },
                onClose = {
                    finish()
                    AcmoOnboardingGate.cancel()
                },
                returnToWidget = actionType,
            )
        }
    }
}
