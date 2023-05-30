package com.example.myencryptapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText
import android.widget.Button
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import android.view.View
import android.view.MotionEvent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.util.TypedValue
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import android.widget.ScrollView
import java.util.*

class DecryptActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_decrypt)

        // Set the toolbar as the action bar for the activity
        val toolbar = findViewById<Toolbar>(R.id.toolbar3)
        toolbar.title = ""

        setSupportActionBar(toolbar)

        // Add a back button to the toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val docIdList = mutableListOf<String>()

        db.collection("user").get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {
                    docIdList.add(document.id)
                }
                // Do something with the list of document IDs (docIdList)
                showDocumentsInScrollView(docIdList)
            }
            .addOnFailureListener { exception ->
                // Handle any errors here
            }
    }

    // Define the function outside of onCreate
    private fun showDocumentsInScrollView(docIdList: List<String>) {
        val linearLayout = findViewById<LinearLayout>(R.id.linear_layout_documents)
        linearLayout.removeAllViews() // Remove any existing TextViews

        for (docId in docIdList) {
            val productLayout = LinearLayout(this)
            productLayout.orientation = LinearLayout.HORIZONTAL
            productLayout.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            productLayout.setPadding(20, 20, 20, 20)

            // Add product to layout
            val productDetailsLayout = LinearLayout(this)
            productDetailsLayout.orientation = LinearLayout.VERTICAL
            productDetailsLayout.layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
            ) // Set weight to 1

            productDetailsLayout.setPadding(20, 0, 0, 0)

            val textView = TextView(this)
            textView.text = docId
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            textView.setTextColor(Color.BLACK)
            productDetailsLayout.addView(textView)

            productLayout.addView(productDetailsLayout)

            // Add OnClickListener to product layout
            productLayout.setOnClickListener {
                val intent = Intent(this, DecryptDetailsActivity::class.java)
                intent.putExtra("docId", docId)
                startActivity(intent)
            }

            linearLayout.addView(productLayout)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


}
