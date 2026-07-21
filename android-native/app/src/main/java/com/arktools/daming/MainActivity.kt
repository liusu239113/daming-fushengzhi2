package com.arktools.daming

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.arktools.daming.ads.AdSdkInitializer
import com.arktools.daming.ads.RewardedAdController
import com.arktools.daming.audio.GameAudio
import com.arktools.daming.auth.ComplianceGate
import com.arktools.daming.auth.ComplianceManager
import com.arktools.daming.auth.TapSdkInitializer
import com.arktools.daming.auth.TapTapLoginGate
import com.arktools.daming.persistence.V3SaveStore
import com.arktools.daming.privacy.PrivacyConsentDialog
import com.arktools.daming.privacy.PrivacyPreferences
import com.arktools.daming.ui.screens.MainMenuScreen
import com.arktools.daming.ui.theme.FontPreference
import com.arktools.daming.ui.theme.MingTheme
import com.arktools.daming.v3.logic.V3GameController
import com.arktools.daming.v3.ui.V3CreateScreen
import com.arktools.daming.v3.ui.V3GameScreen

class MainActivity : ComponentActivity() {
    private enum class AppScreen { Menu, Create, Game }
    private enum class GateStage { Loading, Privacy, Login, Compliance, Done, Error }

    private var gameAudio: GameAudio? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        setContent {
            val fontPreference = remember { FontPreference(this@MainActivity) }
            MingTheme(fontStyle = fontPreference.style) {
                DamingGate(fontPreference)
            }
        }
    }

    override fun onPause() {
        gameAudio?.suspendAudio()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        gameAudio?.resumeAudio()
    }

    override fun onDestroy() {
        RewardedAdController.destroy()
        super.onDestroy()
    }

    @Composable
    private fun DamingGate(fontPreference: FontPreference) {
        val privacyPreferences = remember { PrivacyPreferences(this) }
        val privacyAccepted: Boolean? by privacyPreferences.accepted.collectAsState(initial = null)
        var stage by remember { mutableStateOf(GateStage.Loading) }
        var complianceUserId by remember { mutableStateOf("") }
        var initializationError by remember { mutableStateOf("") }

        fun initializeSdks() {
            val tapResult = TapSdkInitializer.initialize(this)
            if (tapResult.isFailure) {
                initializationError = tapResult.exceptionOrNull()?.message ?: "TapTap SDK 初始化失败"
                stage = GateStage.Error
                return
            }
            runCatching { AdSdkInitializer.initialize(application) }
                .onFailure { android.util.Log.e("MainActivity", "广告 SDK 初始化失败", it) }
            stage = GateStage.Login
        }

        LaunchedEffect(privacyAccepted) {
            when (privacyAccepted) {
                null -> stage = GateStage.Loading
                false -> stage = GateStage.Privacy
                true -> if (stage == GateStage.Loading) initializeSdks()
            }
        }

        when (stage) {
            GateStage.Loading -> GateMessage("正在读取隐私设置…")
            GateStage.Privacy -> PrivacyConsentDialog(
                onAccepted = { initializeSdks() },
                onRejected = { finishAffinity() }
            )
            GateStage.Login -> TapTapLoginGate { account ->
                complianceUserId = account.openId ?: account.unionId ?: ""
                if (complianceUserId.isBlank()) {
                    initializationError = "TapTap 账号缺少可用用户标识"
                    stage = GateStage.Error
                } else {
                    stage = GateStage.Compliance
                }
            }
            GateStage.Compliance -> ComplianceGate(
                activity = this,
                userId = complianceUserId,
                onAllowed = { stage = GateStage.Done },
                onReturnToLogin = {
                    ComplianceManager.exit()
                    stage = GateStage.Login
                }
            )
            GateStage.Done -> DamingApp(fontPreference)
            GateStage.Error -> GateMessage(initializationError)
        }
    }

    @Composable
    private fun GateMessage(message: String) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color(0xFFB98E59)),
            contentAlignment = Alignment.Center
        ) {
            Text(message, color = Color(0xFF2B2016))
        }
    }

    @Composable
    private fun DamingApp(fontPreference: FontPreference) {
        val audio = remember { GameAudio(this).also { gameAudio = it } }
        val v3Controller = remember { V3GameController(V3SaveStore(this), audio) }
        var screen by remember { mutableStateOf(AppScreen.Menu) }

        DisposableEffect(Unit) {
            onDispose {
                audio.release()
                if (gameAudio === audio) gameAudio = null
            }
        }
        LaunchedEffect(screen) {
            v3Controller.ensureV3Bgm()
        }

        when (screen) {
            AppScreen.Menu -> MainMenuScreen(
                v3Controller = v3Controller,
                fontPreference = fontPreference,
                onNewGame = { screen = AppScreen.Create },
                onContinue = { screen = AppScreen.Game }
            )
            AppScreen.Create -> V3CreateScreen(
                controller = v3Controller,
                onBack = { screen = AppScreen.Menu },
                onStart = { screen = AppScreen.Game }
            )
            AppScreen.Game -> V3GameScreen(
                controller = v3Controller,
                fontPreference = fontPreference,
                onBackToMenu = { screen = AppScreen.Menu }
            )
        }
    }
}
