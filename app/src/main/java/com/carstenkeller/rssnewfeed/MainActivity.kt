package com.carstenkeller.rssnewfeed

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.carstenkeller.rssnewfeed.ui.navigation.AppNavHost
import com.carstenkeller.rssnewfeed.ui.theme.AppearancePreferences
import com.carstenkeller.rssnewfeed.ui.theme.RssNewfeedTheme

class MainActivity : ComponentActivity() {
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
