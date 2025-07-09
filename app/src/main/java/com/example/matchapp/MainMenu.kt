package com.example.matchapp

import android.content.Intent
import android.net.Uri
import android.view.Menu
import android.view.MenuItem
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class MainMenu : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pagina_principal)
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val webView = findViewById<WebView>(R.id.videoContainer)
        webView.settings.javaScriptEnabled = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true
        webView.webChromeClient = WebChromeClient()

        val videoId = "OoIKav61iiU"
        val html = """
            <html>
              <body style="margin:0;padding:0;">
                <iframe 
                  width="100%" height="100%" 
                  src="https://www.youtube.com/embed/$videoId" 
                  frameborder="0" 
                  allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" 
                  allowfullscreen>
                </iframe>
              </body>
            </html>
        """.trimIndent()

        webView.loadData(html, "text/html", "utf-8")

        findViewById<Button>(R.id.btnUp4U).setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://up4u.up.edu.mx/p/inicio"))
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnPortalUP).setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://portal.up.edu.mx/"))
            startActivity(intent)
        }

        val navChat = findViewById<ImageButton>(R.id.nav_chat)
        val navHome = findViewById<ImageButton>(R.id.nav_home)
        val navProfile = findViewById<ImageButton>(R.id.nav_profile)

        navHome.isEnabled = false

        navChat.setOnClickListener {
            startActivity(Intent(this, Match_Chat::class.java))
            finish()
        }

        navProfile.setOnClickListener {
            startActivity(Intent(this, Perfil::class.java))
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, Login::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK))
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}