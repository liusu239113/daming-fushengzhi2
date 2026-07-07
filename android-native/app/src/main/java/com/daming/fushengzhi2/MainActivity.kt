package com.daming.fushengzhi2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
        val controller = remember { GameController(SaveStore(this)) }
        when (controller.screen) {
            GameController.Screen.Menu -> MainMenuScreen(controller)
            GameController.Screen.Create -> CreateGameScreen(controller)
            GameController.Screen.Game -> GameScreen(controller)
        }
    }
}
