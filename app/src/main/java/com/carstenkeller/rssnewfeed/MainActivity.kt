package com.carstenkeller.rssnewfeed

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.carstenkeller.rssnewfeed.ui.navigation.AppNavHost
import com.carstenkeller.rssnewfeed.ui.theme.RssNewfeedTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RssNewfeedTheme {
                val repository = (application as RssNewfeedApp).repository
                AppNavHost(repository = repository)
            }
        }
    }
}
