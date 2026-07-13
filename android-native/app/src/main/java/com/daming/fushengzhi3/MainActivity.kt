package com.daming.fushengzhi3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.daming.fushengzhi3.audio.GameAudio
import com.daming.fushengzhi3.persistence.V3SaveStore
import com.daming.fushengzhi3.ui.screens.MainMenuScreen
import com.daming.fushengzhi3.ui.theme.FontPreference
import com.daming.fushengzhi3.ui.theme.MingTheme
import com.daming.fushengzhi3.v3.logic.V3GameController
import com.daming.fushengzhi3.v3.ui.V3CreateScreen
import com.daming.fushengzhi3.v3.ui.V3GameScreen

class MainActivity : ComponentActivity() {
    private enum class AppScreen { Menu, Create, Game }

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
                DamingApp(fontPreference)
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
