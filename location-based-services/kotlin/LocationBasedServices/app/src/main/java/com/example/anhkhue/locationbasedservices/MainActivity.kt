package com.example.anhkhue.locationbasedservices

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button

class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "MainActivity"
    }

    private lateinit var btnOpen: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "On Create")

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnOpen = findViewById(R.id.btnOpen)
        btnOpen.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }
    }
}
