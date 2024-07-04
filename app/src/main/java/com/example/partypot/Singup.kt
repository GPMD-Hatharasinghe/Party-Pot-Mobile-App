package com.example.partypot

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.partypot.databinding.ActivitySingupBinding
import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast
import android.content.Intent
import android.service.autofill.UserData
import com.google.firebase.database.FirebaseDatabase


class Singup : AppCompatActivity() {

    private lateinit var binding: ActivitySingupBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySingupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()


        binding.button16.setOnClickListener {
            val intent = Intent(this, Loginpage::class.java)
            startActivity(intent)
        }
        binding.button13.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }


        binding.button13.setOnClickListener {
            val name = binding.userName.text.toString()
            val email = binding.emailET.text.toString()
            val pass = binding.PassET.text.toString()
            val confirmPass = binding.confirmPassEt.text.toString()

            if (name.isEmpty()) {
                binding.userName.error = "Name is required"
                return@setOnClickListener
            }
            if (email.isEmpty()) {
                binding.emailET.error = "Email is required"
                return@setOnClickListener
            }
            if (pass.isEmpty()) {
                binding.PassET.error = "Password is required"
                return@setOnClickListener
            }
            if (pass == confirmPass){
                signUpUser(name, email, pass)}
            else{
               binding.confirmPassEt.error = "Check Your comfrem password"
               return@setOnClickListener
            }
        }
    }


    private fun signUpUser(name: String, email: String, password: String) {

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    val user = firebaseAuth.currentUser
                    user?.let {

                        val userData = com.example.partypot.ui.UserData(name, email, password)
                        firebaseDatabase.reference.child("users").child(user.uid).setValue(userData)
                            .addOnSuccessListener {
                                Toast.makeText(this@Singup, "User signed up successfully", Toast.LENGTH_SHORT).show()
                                // Navigate to the login page
                                val intent = Intent(this@Singup, Loginpage::class.java)
                                startActivity(intent)
                                finish() // Optional: Finish the current activity to prevent the user from returning to it using the back button
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this@Singup, "Failed to sign up user: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    // Registration failed
                    Toast.makeText(this@Singup, "Failed to sign up user: " +
                            "${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
                }

    }
}