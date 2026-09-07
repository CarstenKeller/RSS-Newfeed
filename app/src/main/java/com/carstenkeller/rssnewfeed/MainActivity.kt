package com.carstenkeller.rssnewfeed

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.carstenkeller.rssnewfeed.ui.navigation.AppNavHost
import com.carstenkeller.rssnewfeed.ui.theme.AppearancePreferences
import com.carstenkeller.rssnewfeed.ui.theme.RssNewfeedTheme

// Must extend AppCompatActivity, not plain ComponentActivity - per Android's own docs,
// AppCompatDelegate.setApplicationLocales() (the Sprache-Umschalter) silently does nothing
// otherwise, even in a Compose-only app.
class MainActivity : AppCompatActivity() {

    // Backs the notification-tap deep link (see NewArticlesNotifier). A plain var with
    // mutableStateOf so Compose recomposes when onNewIntent updates it while running.
    private var pendingArticleId by mutableStateOf<Long?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AppearancePreferences.init(applicationContext)
        pendingArticleId = extractArticleId(intent)
        setContent {
            val themeMode by AppearancePreferences.themeMode.collectAsState()
            RssNewfeedTheme(themeMode = themeMode) {
                val repository = (application as RssNewfeedApp).repository
                AppNavHost(
                    repository = repository,
                    deepLinkArticleId = pendingArticleId,
                    onDeepLinkConsumed = { pendingArticleId = null },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingArticleId = extractArticleId(intent)
    }

    private fun extractArticleId(intent: Intent?): Long? {
        val id = intent?.getLongExtra(EXTRA_ARTICLE_ID, -1L) ?: -1L
        return id.takeIf { it >= 0 }
    }

    companion object {
        const val EXTRA_ARTICLE_ID = "article_id"
    }
}
