package com.example.myencryptapp

import com.bumptech.glide.Glide
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import java.util.*
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar

class AboutActivity : AppCompatActivity() {
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.about_page)

        // Set the toolbar as the action bar for the activity
        val toolbar = findViewById<Toolbar>(R.id.toolbar4)
        toolbar.title = ""

        setSupportActionBar(toolbar)

        // Add a back button to the toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val myImageView = findViewById<ImageView>(R.id.my_image_view)
        val imageUrl = "https://rec-data.kalibrr.com/www.kalibrr.com/logos/GGTQ9WER388Z4G2U8J7VPTBQXWP7Y64Q54EXH895-5daff70d.jpg"

        Glide.with(this)
            .load(imageUrl)
            .into(myImageView)

    }
}