package com.example.partypot

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.partypot.databinding.ActivityLoginpageBinding
import com.google.firebase.auth.FirebaseAuth


class Loginpage : AppCompatActivity() {

    private lateinit var binding: ActivityLoginpageBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginpageBinding.inflate(layoutInflater)
        setContentView(binding.root)


        firebaseAuth = FirebaseAuth.getInstance()
        binding.textView.setOnClickListener {
            val intent = Intent(this, Singup::class.java)
            startActivity(intent)
        }



        binding.button12.setOnClickListener {
            val email = binding.emailEt.text.toString()
            val pass = binding.passET.text.toString()


            if (email.isNotEmpty() && pass.isNotEmpty()) {

                firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(this, "Login Successfully", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, FirstPage::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "Incorrect Email or Pass word", Toast.LENGTH_SHORT).show()

                    }
                }
            } else {
                Toast.makeText(this, "Empty Fields Are not Allowed !!", Toast.LENGTH_SHORT).show()

            }
        }
    }


    }
