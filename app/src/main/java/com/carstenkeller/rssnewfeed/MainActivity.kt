package com.carstenkeller.rssnewfeed

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.carstenkeller.rssnewfeed.ui.navigation.AppNavHost
import com.carstenkeller.rssnewfeed.ui.theme.AppearancePreferences
import com.carstenkeller.rssnewfeed.ui.theme.RssNewfeedTheme

// Must extend AppCompatActivity, not plain ComponentActivity - per Android's own docs,
// AppCompatDelegate.setApplicationLocales() (the Sprache-Umschalter) silently does nothing
// otherwise, even in a Compose-only app.
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AppearancePreferences.init(applicationContext)
        setContent {
            val themeMode by AppearancePreferences.themeMode.collectAsState()
            RssNewfeedTheme(themeMode = themeMode) {
                val repository = (application as RssNewfeedApp).repository
                AppNavHost(repository = repository)
            }
        }
    }
}
