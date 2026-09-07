package com.carstenkeller.rssnewfeed.ui.articledetail

import android.net.Uri
import android.webkit.WebView
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(
    viewModel: ArticleDetailViewModel,
    onBack: () -> Unit,
) {
    val article by viewModel.article.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(article?.title.orEmpty(), maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                },
            )
        },
    ) { innerPadding ->
        val current = article
        if (current == null) {
            Box(Modifier.fillMaxSize().padding(innerPadding))
            return@Scaffold
        }

        if (!current.contentHtml.isNullOrBlank()) {
            AndroidView(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                factory = { ctx ->
                    WebView(ctx).apply {
                        loadDataWithBaseURL(null, wrapHtml(current.contentHtml), "text/html", "UTF-8", null)
                        setOnScrollChangeListener { view, _, scrollY, _, _ ->
                            val webView = view as WebView
                            val contentHeightPx = (webView.contentHeight * webView.scale).toInt()
                            val reachedEnd = scrollY + webView.height >= contentHeightPx - 24
                            if (reachedEnd) viewModel.markAsRead()
                        }
                    }
                },
            )
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(current.title, style = MaterialTheme.typography.headlineSmall)
                Text(current.summary, style = MaterialTheme.typography.bodyMedium)
                Text(
                    "Dieser Feed liefert keinen Volltext. Öffne den Original-Artikel, um ihn vollständig zu lesen.",
                    style = MaterialTheme.typography.bodySmall,
                )
                Button(onClick = {
                    viewModel.markAsRead()
                    CustomTabsIntent.Builder().build().launchUrl(context, Uri.parse(current.link))
                }) {
                    Text("Original-Artikel öffnen")
                }
            }
        }
    }
}

private fun wrapHtml(body: String): String = """
    <html><head><meta name="viewport" content="width=device-width, initial-scale=1">
    <style>body{font-family:sans-serif;font-size:16px;line-height:1.5;padding:16px;}img{max-width:100%;height:auto;}</style>
    </head><body>$body</body></html>
""".trimIndent()
