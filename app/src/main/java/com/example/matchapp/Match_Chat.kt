package com.example.matchapp

import android.view.Menu
import android.view.MenuItem
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth

class Match_Chat : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_match_chat)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val navChat = findViewById<ImageButton>(R.id.nav_chat)
        val navHome = findViewById<ImageButton>(R.id.nav_home)
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

        val chips = listOf(
            findViewById<Chip>(R.id.chipCocina),
            findViewById<Chip>(R.id.chipHistoria),
            findViewById<Chip>(R.id.chipArte),
            findViewById<Chip>(R.id.chipVideojuegos),
            findViewById<Chip>(R.id.chipLectura),
            findViewById<Chip>(R.id.chipFotografia)
        )
        chips.forEach { chip ->
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
        return when (item.itemId) {
            R.id.action_logout -> {
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, Login::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}