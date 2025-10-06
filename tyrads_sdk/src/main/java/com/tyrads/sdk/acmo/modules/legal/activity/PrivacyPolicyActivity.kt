package com.tyrads.sdk.acmo.modules.legal.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.tyrads.sdk.Tyrads
import com.tyrads.sdk.acmo.core.AcmoOnboardingGate
import com.tyrads.sdk.acmo.modules.legal.AcmoPrivacyPolicyPage

class AcmoPrivacyPolicyActivity : ComponentActivity() {
    companion object {
        fun start(context: Context, actionType: Boolean) {
            val intent = Intent(context, AcmoPrivacyPolicyActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("returnToWidget", actionType)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val actionType = intent.getBooleanExtra("returnToWidget", false)
        setContent {
            AcmoPrivacyPolicyPage(
                onAccepted = {
                    Tyrads.getInstance().setPrivacyAccepted(true)
                    finish()
                    AcmoOnboardingGate.continueFlow(this)
                },
                onCancelled = {
                    finish()
                    AcmoOnboardingGate.cancel()
                },
                returnToWidget = actionType
            )
        }
    }
}
