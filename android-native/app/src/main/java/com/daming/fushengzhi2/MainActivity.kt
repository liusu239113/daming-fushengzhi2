package com.daming.fushengzhi2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.daming.fushengzhi2.audio.GameAudio
import com.daming.fushengzhi2.data.BgmKey
import com.daming.fushengzhi2.logic.GameController
import com.daming.fushengzhi2.persistence.SaveStore
import com.daming.fushengzhi2.persistence.V3SaveStore
import com.daming.fushengzhi2.ui.screens.CreateGameScreen
import com.daming.fushengzhi2.ui.screens.GameScreen
import com.daming.fushengzhi2.ui.screens.MainMenuScreen
import com.daming.fushengzhi2.ui.theme.MingTheme
import com.daming.fushengzhi2.v3.logic.V3GameController
import com.daming.fushengzhi2.v3.ui.V3CreateScreen
import com.daming.fushengzhi2.v3.ui.V3GameScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        setContent {
            MingTheme {
                DamingApp()
            }
        }
    }

    @Composable
    private fun DamingApp() {
        val audio = remember { GameAudio(this) }
        val controller = remember { GameController(SaveStore(this), audio) }
        val v3Controller = remember { V3GameController(V3SaveStore(this), audio) }
        DisposableEffect(Unit) {
            onDispose { audio.release() }
        }
        LaunchedEffect(controller.screen, controller.state?.year) {
            if (controller.screen == GameController.Screen.Game) {
                controller.state?.let { audio.playGameBgm(it.year) }
            } else {
                audio.playBgm(BgmKey.Menu)
            }
        }
        when (controller.screen) {
            GameController.Screen.Menu -> MainMenuScreen(controller)
            GameController.Screen.Create -> CreateGameScreen(controller)
            GameController.Screen.Game -> GameScreen(controller)
            GameController.Screen.V3Create -> V3CreateScreen(
                controller = v3Controller,
                onBack = controller::backToMenu,
                onStart = controller::openV3Game
            )
            GameController.Screen.V3Game -> V3GameScreen(
                controller = v3Controller,
                onBackToV2 = controller::backToMenu
            )
        }
    }
}
