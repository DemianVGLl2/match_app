package com.example.matchapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider

class Login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var googleSignInButton: Button

    companion object {
        private const val TAG = "Login"
    }

    // Launcher para el resultado de Google Sign-In
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            Log.w(TAG, "Google sign in failed", e)
            Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pagina_de_inicio)
        enableEdgeToEdge()

        auth = FirebaseAuth.getInstance()

        // Configurar Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Inicializar vistas
        emailEditText = findViewById(R.id.inputUsuario)
        passwordEditText = findViewById(R.id.inputPassword)
        loginButton = findViewById(R.id.btnInicio)
        googleSignInButton = findViewById(R.id.btnGoogleSignIn)

        // Configurar listeners
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (validateUserInput(email, password)) {
                signInUser(email, password)
            }
        }

        googleSignInButton.setOnClickListener {
            signInWithGoogle()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_layout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun validateUserInput(email: String, password: String): Boolean {
        return when {
            email.isEmpty() -> {
                emailEditText.error = "Correo es requerido"
                false
            }
            !email.endsWith("@up.edu.mx") -> {
                emailEditText.error = "Correo no válido"
                false
            }
            password.isEmpty() -> {
                passwordEditText.error = "Contraseña es requerida"
                false
            }
            password.length < 6 -> {
                passwordEditText.error = "La contraseña debe tener al menos 6 caracteres"
                false
            }
            else -> true
        }
    }

    private fun signInUser(email: String, password: String) {
        loginButton.isEnabled = false
        emailEditText.isEnabled = false
        passwordEditText.isEnabled = false
        loginButton.text = "Iniciando sesión..."

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                loginButton.isEnabled = true
                emailEditText.isEnabled = true
                passwordEditText.isEnabled = true
                loginButton.text = "Iniciar sesión"
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    navigateToMainActivity()
                } else {
                    try {
                        throw task.exception!!
                    } catch (e: FirebaseAuthInvalidUserException) {
                        emailEditText.error = "Correo incorrecto"
                        emailEditText.requestFocus()
                    } catch (e: FirebaseAuthInvalidCredentialsException) {
                        passwordEditText.error = "Contraseña incorrecta"
                        passwordEditText.requestFocus()
                    } catch (e: Exception) {
                        Toast.makeText(this, "Error de inicio de sesión: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser

                    // Verificar si el email es del dominio permitido
                    val email = user?.email
                    if (email != null && email.endsWith("@up.edu.mx")) {
                        Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                        navigateToMainActivity()
                    } else {
                        // Si el email no es del dominio permitido, cerrar sesión
                        auth.signOut()
                        googleSignInClient.signOut()
                        Toast.makeText(this, "Solo se permiten correos @up.edu.mx", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Autenticación fallida: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainMenu::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            navigateToMainActivity()
        }
    }
}