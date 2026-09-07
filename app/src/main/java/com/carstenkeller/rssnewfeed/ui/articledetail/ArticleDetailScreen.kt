package com.carstenkeller.rssnewfeed.ui.articledetail

import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.browser.customtabs.CustomTabsIntent
import com.carstenkeller.rssnewfeed.data.network.CachedImageLoader
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.carstenkeller.rssnewfeed.R

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
                colors = com.carstenkeller.rssnewfeed.ui.theme.brandedTopAppBarColors(),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
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
                        // evaluateJavascript() below is a no-op without this.
                        settings.javaScriptEnabled = true
                        // WebView.getContentHeight()/getScale() are documented as unreliable for
                        // this purpose (scale in particular can silently stay 1.0 regardless of
                        // actual zoom), which meant "reached the end" almost never triggered.
                        // Asking the page itself, purely in CSS-pixel space, is exact.
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView, url: String?) {
                                // Short articles that fit on one screen never fire a scroll
                                // event at all, so also check right after the page settles.
                                checkScrolledToBottom(view, viewModel)
                            }

                            // Routes <img> requests through a disk-cached OkHttp client so
                            // images already seen once are still shown when offline — the
                            // WebView's own network stack has no such cache.
                            override fun shouldInterceptRequest(
                                view: WebView,
                                request: WebResourceRequest,
                            ): WebResourceResponse? {
                                if (request.isForMainFrame) return null
                                return CachedImageLoader.load(view.context, request.url.toString())
                                    ?: super.shouldInterceptRequest(view, request)
                            }
                        }
                        setOnScrollChangeListener { view, _, _, _, _ ->
                            checkScrolledToBottom(view as WebView, viewModel)
                        }
                        loadDataWithBaseURL(null, wrapHtml(current.contentHtml), "text/html", "UTF-8", null)
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
                    stringResource(R.string.no_fulltext_message),
                    style = MaterialTheme.typography.bodySmall,
                )
                Button(onClick = {
                    viewModel.markAsRead()
                    CustomTabsIntent.Builder().build().launchUrl(context, Uri.parse(current.link))
                }) {
                    Text(stringResource(R.string.open_original_button))
                }
            }
        }
    }
}

private fun checkScrolledToBottom(webView: WebView, viewModel: ArticleDetailViewModel) {
    val script = """
        (function() {
            var doc = document.documentElement;
            var scrollHeight = Math.max(doc.scrollHeight, document.body.scrollHeight);
            return (window.scrollY + window.innerHeight) >= (scrollHeight - 24);
        })();
    """.trimIndent()
    webView.evaluateJavascript(script) { result ->
        if (result == "true") viewModel.markAsRead()
    }
}

private fun wrapHtml(body: String): String = """
    <html><head><meta name="viewport" content="width=device-width, initial-scale=1">
    <style>body{font-family:sans-serif;font-size:16px;line-height:1.5;padding:16px;}img{max-width:100%;height:auto;}</style>
    </head><body>$body</body></html>
""".trimIndent()
