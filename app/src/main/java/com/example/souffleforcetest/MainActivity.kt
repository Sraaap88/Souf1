package com.example.souffleforcetest

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            Toast.makeText(this, "App démarrée !", Toast.LENGTH_LONG).show()
            setContentView(R.layout.activity_main)
            Toast.makeText(this, "Layout chargé !", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "ERREUR: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
