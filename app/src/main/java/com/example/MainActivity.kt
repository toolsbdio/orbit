package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.BrowserRepository
import com.example.data.local.BrowserDatabase
import com.example.ui.BrowserViewModel
import com.example.ui.OrbitBrowserApp
import com.example.ui.theme.OrbitTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = BrowserDatabase.getInstance(applicationContext)
        val repository = BrowserRepository(
            dao = database.browserDao(),
            context = applicationContext,
        )
        val viewModel = ViewModelProvider(
            this,
            BrowserViewModel.Factory(repository),
        )[BrowserViewModel::class.java]

        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            OrbitTheme(
                preset = state.settings.themePreset,
                radiusPreset = state.settings.radiusPreset,
            ) {
                OrbitBrowserApp(viewModel = viewModel)
            }
        }
    }
}
