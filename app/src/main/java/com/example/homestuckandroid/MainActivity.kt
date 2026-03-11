package com.example.homestuckandroid

import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        setContentView(R.layout.activity_main)
        val wv: WebView = findViewById(R.id.webview)

        // enable JS and local file access so the HTML can fetch JSON and GIFs from assets
        wv.settings.apply {
            javaScriptEnabled = true
            allowFileAccess = true
            allowFileAccessFromFileURLs = true
            allowUniversalAccessFromFileURLs = true
            domStorageEnabled = true
        }

        // register JS interface so page can read the JSON directly
        class JsBridge(val ctx: android.content.Context) {
            @android.webkit.JavascriptInterface
            fun getTexto(): String {
                return ctx.assets.open("texto.json").bufferedReader().use { it.readText() }
            }
        }
        wv.addJavascriptInterface(JsBridge(this), "Android")

        // log console messages to adb so we can debug JS errors
        wv.webChromeClient = object : android.webkit.WebChromeClient() {
            override fun onConsoleMessage(message: android.webkit.ConsoleMessage): Boolean {
                android.util.Log.d("WebView", "${message.message()} -- line ${message.lineNumber()}")
                return true
            }
        }
        wv.webViewClient = object : android.webkit.WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView?,
                request: android.webkit.WebResourceRequest?
            ): android.webkit.WebResourceResponse? {
                // keep the old interceptor in case other assets need rewriting
                val url = request?.url?.toString()
                if (url != null && url.endsWith("texto.json")) {
                    try {
                        val stream = assets.open("texto.json")
                        return android.webkit.WebResourceResponse(
                            "application/json",
                            "UTF-8",
                            stream
                        )
                    } catch (e: Exception) {
                        android.util.Log.e("MainActivity", "error opening texto.json", e)
                    }
                }
                return super.shouldInterceptRequest(view, request)
            }
        }

        wv.loadUrl("file:///android_asset/homestuck.html")
    }
}