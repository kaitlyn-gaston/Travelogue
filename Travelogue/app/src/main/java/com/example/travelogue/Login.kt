package com.example.travelogue

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import com.example.travelogue.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val signup = findViewById<Button>(R.id.signup)
        signup.setOnClickListener{
            val intent: Intent = Intent(
                this,
                Signup::class.java
            )
            startActivity(intent)
        }

        val login = findViewById<Button>(R.id.log_in)
        val auth = FirebaseAuth.getInstance();
        login.setOnClickListener {
            val editTextEmailAddress = findViewById<EditText>(R.id.email_signup)
            val editTextPassword = findViewById<EditText>(R.id.password_signup)
            val email=editTextEmailAddress.text.toString()
            val password=editTextPassword.text.toString()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both email and password.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }.addOnFailureListener { exception ->
                    Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show()
                    Toast.makeText(
                        applicationContext,
                        exception.localizedMessage,
                        Toast.LENGTH_LONG
                    ).show();
                }
        }
    }
}