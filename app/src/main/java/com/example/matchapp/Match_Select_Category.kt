package com.example.matchapp

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import de.hdodenhof.circleimageview.CircleImageView

class Match_Select_Category : AppCompatActivity() {

    private lateinit var imgUPChatBot: CircleImageView
    private var isChatBotOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_match_select_category)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        setupNavigation()
        setupChatBot()
        listOf(
            findViewById<Chip>(R.id.chipCocina),
            findViewById<Chip>(R.id.chipHistoria),
            findViewById<Chip>(R.id.chipArte),
            findViewById<Chip>(R.id.chipVideojuegos),
            findViewById<Chip>(R.id.chipLectura),
            findViewById<Chip>(R.id.chipFotografia)
        ).forEach { chip ->
            chip.setOnClickListener {
                Toast.makeText(this, "Interés: ${chip.text}", Toast.LENGTH_SHORT).show()
            }
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

    private fun setupNavigation() {
        val navChat    = findViewById<ImageButton>(R.id.nav_chat)
        val navHome    = findViewById<ImageButton>(R.id.nav_home)
        val navProfile = findViewById<ImageButton>(R.id.nav_profile)

        navChat.isEnabled = false

        navHome.setOnClickListener {
            startActivity(Intent(this, MainMenu::class.java))
            finish()
        }
        navProfile.setOnClickListener {
            startActivity(Intent(this, Perfil::class.java))
            finish()
        }
    }

    private fun setupChatBot() {
        val view = findViewById<CircleImageView?>(R.id.imgUPChatBot)
        if (view == null) {
            android.util.Log.w("Match_Select_Category", "imgUPChatBot no encontrado")
            return
        }
        imgUPChatBot = view

        imgUPChatBot.setOnClickListener {
            if (!isChatBotOpen) {
                showChatBot()
            } else {
                supportFragmentManager.popBackStack()
            }
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

        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_up_in,
                R.anim.slide_down_out,
                R.anim.slide_up_in,
                R.anim.slide_down_out
            )
            .add(R.id.main, ChatBotFragment(), "CHATBOT_FRAGMENT")
            .addToBackStack(null)
            .commit()

        isChatBotOpen = true
    }

    override fun onBackPressed() {
        if (isChatBotOpen) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }
}