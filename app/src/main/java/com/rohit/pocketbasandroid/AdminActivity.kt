package com.rohit.pocketbasandroid

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AdminActivity : AppCompatActivity() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)
        val adminUrl = intent.getStringExtra("adminUrl")
        val webView = findViewById<WebView>(R.id.webView)

        if (adminUrl == null) {
            Toast.makeText(this, "AdminUrl is empty", Toast.LENGTH_LONG).show()
        } else {
            webView.settings.javaScriptEnabled = true
            webView.settings.domStorageEnabled = true
            webView.settings.databaseEnabled = true
            webView.webViewClient = WebViewClient()
            CookieManager.getInstance().setAcceptCookie(true)
            webView.loadUrl(adminUrl)
        }

    }
}