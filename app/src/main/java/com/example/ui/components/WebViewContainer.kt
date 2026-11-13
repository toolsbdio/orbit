package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.BrowserSettings
import com.example.model.BrowserTabState
import com.example.ui.BrowserWebCommand
import kotlinx.coroutines.flow.SharedFlow

private const val DESKTOP_USER_AGENT =
    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"

@Composable
fun BrowserWebViewHost(
    activeTab: BrowserTabState,
    settings: BrowserSettings,
    commands: SharedFlow<BrowserWebCommand>,
    onPageStarted: (tabId: String, url: String) -> Unit,
    onPageFinished: (tabId: String, url: String, title: String, canGoBack: Boolean, canGoForward: Boolean) -> Unit,
    onProgressChanged: (tabId: String, progress: Int) -> Unit,
    onTitleChanged: (tabId: String, title: String) -> Unit,
    onNavigationStateChanged: (tabId: String, canGoBack: Boolean, canGoForward: Boolean) -> Unit,
    onPageError: (tabId: String, description: String) -> Unit,
    onFindMatchesUpdated: (activeOrdinal: Int, total: Int) -> Unit,
    onReloadClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val webViews = remember { mutableMapOf<String, WebView>() }

    val currentOnPageStarted by rememberUpdatedState(onPageStarted)
    val currentOnPageFinished by rememberUpdatedState(onPageFinished)
    val currentOnProgressChanged by rememberUpdatedState(onProgressChanged)
    val currentOnTitleChanged by rememberUpdatedState(onTitleChanged)
    val currentOnNavChanged by rememberUpdatedState(onNavigationStateChanged)
    val currentOnError by rememberUpdatedState(onPageError)
    val currentOnFindMatches by rememberUpdatedState(onFindMatchesUpdated)

    fun getOrCreateWebView(tab: BrowserTabState): WebView {
        return webViews[tab.id] ?: createConfiguredWebView(
            context = context,
            tab = tab,
            settings = settings,
            onPageStarted = { u -> currentOnPageStarted(tab.id, u) },
            onPageFinished = { u, t, back, fwd -> currentOnPageFinished(tab.id, u, t, back, fwd) },
            onProgressChanged = { p -> currentOnProgressChanged(tab.id, p) },
            onTitleChanged = { t -> currentOnTitleChanged(tab.id, t) },
            onNavChanged = { back, fwd -> currentOnNavChanged(tab.id, back, fwd) },
            onError = { desc -> currentOnError(tab.id, desc) },
            onFindMatches = { ord, total -> currentOnFindMatches(ord, total) },
        ).also { created ->
            webViews[tab.id] = created
            if (tab.url.isNotBlank()) {
                created.loadUrl(tab.url)
            }
        }
    }

    LaunchedEffect(commands) {
        commands.collect { cmd ->
            when (cmd) {
                is BrowserWebCommand.LoadUrl -> {
                    val targetTab = if (cmd.tabId == activeTab.id) activeTab else null
                    val wv = webViews[cmd.tabId] ?: targetTab?.let { getOrCreateWebView(it) }
                    wv?.loadUrl(cmd.url)
                }
                is BrowserWebCommand.Reload -> webViews[cmd.tabId]?.reload()
                is BrowserWebCommand.StopLoading -> webViews[cmd.tabId]?.stopLoading()
                is BrowserWebCommand.GoBack -> webViews[cmd.tabId]?.let { if (it.canGoBack()) it.goBack() }
                is BrowserWebCommand.GoForward -> webViews[cmd.tabId]?.let { if (it.canGoForward()) it.goForward() }
                is BrowserWebCommand.FindAll -> webViews[cmd.tabId]?.findAllAsync(cmd.query)
                is BrowserWebCommand.FindNext -> webViews[cmd.tabId]?.findNext(cmd.forward)
                is BrowserWebCommand.ClearFind -> webViews[cmd.tabId]?.clearMatches()
                is BrowserWebCommand.DestroyTab -> {
                    webViews.remove(cmd.tabId)?.let { wv ->
                        wv.stopLoading()
                        wv.destroy()
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webViews.values.forEach { wv ->
                wv.stopLoading()
                wv.destroy()
            }
            webViews.clear()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { getOrCreateWebView(activeTab) },
            update = { wv ->
                val currentWv = getOrCreateWebView(activeTab)
                applyWebSettings(currentWv, activeTab, settings)
                if (wv !== currentWv) {
                    (wv.parent as? ViewGroup)?.let { parent ->
                        val idx = parent.indexOfChild(wv)
                        parent.removeView(wv)
                        (currentWv.parent as? ViewGroup)?.removeView(currentWv)
                        parent.addView(
                            currentWv,
                            idx,
                            ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT,
                            ),
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .testTag("browser_webview"),
        )

        if (activeTab.errorMessage != null) {
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("webview_error_overlay"),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CloudOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Page unavailable",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = activeTab.errorMessage,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(onClick = onReloadClick) {
                        Icon(Icons.Filled.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Try Again")
                    }
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
private fun createConfiguredWebView(
    context: android.content.Context,
    tab: BrowserTabState,
    settings: BrowserSettings,
    onPageStarted: (String) -> Unit,
    onPageFinished: (String, String, Boolean, Boolean) -> Unit,
    onProgressChanged: (Int) -> Unit,
    onTitleChanged: (String) -> Unit,
    onNavChanged: (Boolean, Boolean) -> Unit,
    onError: (String) -> Unit,
    onFindMatches: (Int, Int) -> Unit,
): WebView {
    return WebView(context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
        )
        applyWebSettings(this, tab, settings)

        if (tab.isPrivate) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, false)
        }

        setFindListener { activeMatchOrdinal, numberOfMatches, isDoneCounting ->
            if (isDoneCounting) {
                onFindMatches(activeMatchOrdinal, numberOfMatches)
            }
        }

        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val uri = request?.url ?: return false
                val scheme = uri.scheme?.lowercase().orEmpty()
                if (scheme == "http" || scheme == "https") {
                    return false
                }
                if (scheme in setOf("tel", "mailto", "geo", "sms")) {
                    runCatching {
                        context.startActivity(Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    }
                }
                return true
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                if (!url.isNullOrBlank()) {
                    onPageStarted(url)
                }
                view?.let { onNavChanged(it.canGoBack(), it.canGoForward()) }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                if (!url.isNullOrBlank()) {
                    onPageFinished(
                        url,
                        view?.title.orEmpty(),
                        view?.canGoBack() == true,
                        view?.canGoForward() == true,
                    )
                }
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?,
            ) {
                super.onReceivedError(view, request, error)
                if (request?.isForMainFrame == true) {
                    onError(error?.description?.toString().orEmpty())
                }
            }
        }

        webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                onProgressChanged(newProgress)
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                if (!title.isNullOrBlank()) {
                    onTitleChanged(title)
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
private fun applyWebSettings(
    webView: WebView,
    tab: BrowserTabState,
    settings: BrowserSettings,
) {
    webView.settings.apply {
        javaScriptEnabled = settings.javaScriptEnabled
        domStorageEnabled = !tab.isPrivate
        databaseEnabled = !tab.isPrivate
        builtInZoomControls = true
        displayZoomControls = false
        useWideViewPort = true
        loadWithOverviewMode = true
        mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        userAgentString = if (tab.isDesktopMode) DESKTOP_USER_AGENT else null
    }
}
