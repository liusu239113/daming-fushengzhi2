package com.daming.fushengzhi2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.daming.fushengzhi2.audio.GameAudio
import com.daming.fushengzhi2.data.BgmKey
import com.daming.fushengzhi2.logic.GameController
import com.daming.fushengzhi2.persistence.SaveStore
import com.daming.fushengzhi2.ui.screens.CreateGameScreen
import com.daming.fushengzhi2.ui.screens.GameScreen
import com.daming.fushengzhi2.ui.screens.MainMenuScreen
import com.daming.fushengzhi2.ui.theme.MingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        }
    }
}
