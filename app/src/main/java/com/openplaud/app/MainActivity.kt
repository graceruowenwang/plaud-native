package com.openplaud.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.openplaud.app.data.api.OpenPlaudApi
import com.openplaud.app.data.repository.PreferencesRepository
import com.openplaud.app.data.repository.RecordingRepository
import com.openplaud.app.ui.navigation.PlaudNavHost
import com.openplaud.app.ui.theme.PlaudTheme

val LocalApi = staticCompositionLocalOf<OpenPlaudApi> { error("No API provided") }
val LocalPrefs = staticCompositionLocalOf<PreferencesRepository> { error("No Prefs provided") }
val LocalRepo = staticCompositionLocalOf<RecordingRepository> { error("No Repo provided") }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = PreferencesRepository(applicationContext)
        val api = AppContainer.createApi(prefs)
        val repo = RecordingRepository(api)

        setContent {
            PlaudTheme {
                CompositionLocalProvider(
                    LocalApi provides api,
                    LocalPrefs provides prefs,
                    LocalRepo provides repo
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = PlaudTheme.colors.background
                    ) { innerPadding ->
                        val navController = rememberNavController()
                        PlaudNavHost(
                            navController = navController,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}
