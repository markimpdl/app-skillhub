package com.osaparecidos.memoriamaker.ui.components

import android.annotation.SuppressLint
import android.util.Log
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewAssetLoader

/**
 * Hosts a self-contained HTML/JS game bundled in `assets/`. The student projects (Geometry Rush,
 * Block World) are web games — running them in a WebView is the faithful, low-risk way to embed them.
 *
 * [assetPath] is relative to `assets/`, e.g. "geometry/index.html".
 * [injectOnLoad] is JS evaluated after each page load (used for kiosk tweaks: control layout, etc.).
 * [onCreated] hands back the WebView so the caller can drive it (e.g. a floating jump button).
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebGameView(
    assetPath: String,
    modifier: Modifier = Modifier,
    injectOnLoad: String = "",
    onCreated: (WebView) -> Unit = {},
) {
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            // Route the WebView's own console + JS errors to logcat (tag "WebGame") so a black
            // screen on a real device can be diagnosed with `adb logcat -s WebGame chromium`.
            WebView.setWebContentsDebuggingEnabled(true)
            // Games are served over https://appassets.androidplatform.net instead of file:// —
            // real-device WebViews refuse ES-module scripts on file:// URLs (Camel Race, Desert),
            // and the emulator masking that made it look like file:// worked everywhere.
            val assetLoader = WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(ctx))
                .build()
            WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                setBackgroundColor(android.graphics.Color.BLACK)
                with(settings) {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    mediaPlaybackRequiresUserGesture = false
                    allowFileAccess = true
                    allowContentAccess = true
                    // ES-module games (Camel Race's Vite bundle, Desert's three.js import) are
                    // CORS-blocked from file:///android_asset (origin "null") without these.
                    @Suppress("DEPRECATION")
                    allowFileAccessFromFileURLs = true
                    @Suppress("DEPRECATION")
                    allowUniversalAccessFromFileURLs = true
                    @Suppress("DEPRECATION")
                    databaseEnabled = true
                    loadWithOverviewMode = true
                    useWideViewPort = false
                }
                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(m: ConsoleMessage): Boolean {
                        Log.i("WebGame", "${m.messageLevel()}: ${m.message()} (${m.sourceId()}:${m.lineNumber()})")
                        return true
                    }
                }
                webViewClient = object : WebViewClient() {
                    override fun shouldInterceptRequest(
                        view: WebView,
                        request: WebResourceRequest,
                    ): WebResourceResponse? = assetLoader.shouldInterceptRequest(request.url)

                    override fun onPageFinished(view: WebView, url: String) {
                        if (injectOnLoad.isNotBlank()) {
                            view.evaluateJavascript(injectOnLoad, null)
                        }
                    }

                    override fun onReceivedError(
                        view: WebView,
                        request: WebResourceRequest,
                        error: WebResourceError,
                    ) {
                        Log.e("WebGame", "load error ${error.errorCode} ${error.description} @ ${request.url}")
                    }
                }
                onCreated(this)
                loadUrl("https://appassets.androidplatform.net/assets/$assetPath")
            }
        },
    )
}
