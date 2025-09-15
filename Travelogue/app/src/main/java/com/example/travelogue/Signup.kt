package com.example.travelogue

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class Signup : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val auth = FirebaseAuth.getInstance();

        val editTextEmailAddress: EditText = findViewById(R.id.email_signup)
        val editTextPassword:EditText = findViewById(R.id.password_signup)
        val buttonRegister:Button = findViewById((R.id.create_acc))
        val passwordReentry:EditText = findViewById(R.id.password_confirm)

        buttonRegister.setOnClickListener{
            val email = editTextEmailAddress.text.toString()
            val password = editTextPassword.text.toString()
            val passwordConfirm = passwordReentry.text.toString()
            if (password != passwordConfirm) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.length < 6){
                Toast.makeText(this, "Password should be at least 6 characters long", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener {
                    task ->
                if(task.isSuccessful){
                    val intent= Intent(this,MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(this, "Creating new user failed", Toast.LENGTH_LONG).show()
            }
        }

    }
}