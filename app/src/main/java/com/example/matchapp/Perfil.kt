package com.example.matchapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import de.hdodenhof.circleimageview.CircleImageView

class Perfil : AppCompatActivity() {
    private lateinit var imgUPChatBot: CircleImageView
    private var isChatBotOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_perfil)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            insets
        }

        initCommon()
    }

    private fun initCommon() {
        setupNavigation()
        setupChatBot()
    }

    private fun setupNavigation() {
        val navChat    = findViewById<ImageButton>(R.id.nav_chat)
        val navHome    = findViewById<ImageButton>(R.id.nav_home)
        val navProfile = findViewById<ImageButton>(R.id.nav_profile)

        // Estamos en Perfil, deshabilitamos su ícono
        navProfile.isEnabled = false

        navChat.setOnClickListener {
            startActivity(Intent(this, Match_Select_Category::class.java))
            finish()
        }
        navHome.setOnClickListener {
            startActivity(Intent(this, MainMenu::class.java))
            finish()
        }
        navProfile.setOnClickListener {
            /* nada, está deshabilitado */
        }
    }

    private fun setupChatBot() {
        val view = findViewById<CircleImageView?>(R.id.imgUPChatBot)
        if (view == null) {
            Log.w("Perfil", "imgUPChatBot no en layout")
            return
        }
        imgUPChatBot = view

        imgUPChatBot.setOnClickListener {
            if (!isChatBotOpen) showChatBot() else supportFragmentManager.popBackStack()
        }
        supportFragmentManager.addOnBackStackChangedListener {
            isChatBotOpen = supportFragmentManager.findFragmentByTag("CHATBOT_FRAGMENT") != null
            imgUPChatBot.isEnabled = !isChatBotOpen
            imgUPChatBot.alpha     = if (isChatBotOpen) 0.7f else 1f
            imgUPChatBot.scaleX    = if (isChatBotOpen) 0.9f else 1f
            imgUPChatBot.scaleY    = if (isChatBotOpen) 0.9f else 1f
        }
    }

    private fun showChatBot() {
        if (supportFragmentManager.findFragmentByTag("CHATBOT_FRAGMENT") != null) return
        val frag = ChatBotFragment()
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_up_in, R.anim.slide_down_out,
                R.anim.slide_up_in, R.anim.slide_down_out)
            .add(R.id.main, frag, "CHATBOT_FRAGMENT")
            .addToBackStack(null)
            .commit()
        isChatBotOpen = true
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

    override fun onBackPressed() {
        if (isChatBotOpen) supportFragmentManager.popBackStack()
        else super.onBackPressed()
    }
}