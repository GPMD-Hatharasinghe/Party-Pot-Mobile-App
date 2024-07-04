package com.example.partypot

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.view.View // This import is needed for the OnClickListener
import android.widget.Button


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)




        val buttonPlace = findViewById<Button>(R.id.button3)
        buttonPlace.setOnClickListener {
            // Create an Intent to launch the target Activity
            val intent = Intent(this@MainActivity, Singup::class.java)
            startActivity(intent)
        }

        val button1 = findViewById<Button>(R.id.button11)  // Replace R.id.imageButton2 with your actual button ID
        button1.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                // Create an Intent to launch the target Activity (replace TargetActivity with your actual class name)
                val intent = Intent(this@MainActivity, Loginpage::class.java)  // this@MainActivity refers to the current Activity context
                startActivity(intent)
            }
        })

    }
}
