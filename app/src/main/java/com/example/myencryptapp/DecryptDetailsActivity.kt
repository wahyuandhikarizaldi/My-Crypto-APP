package com.example.myencryptapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText
import android.widget.TextView
import android.widget.Button
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import android.os.AsyncTask
import android.view.View
import android.view.MotionEvent
import android.content.Context
import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import java.util.*
import android.util.Log

import javax.crypto.Cipher
import android.util.Base64
import java.net.HttpURLConnection
import java.net.URL
import java.io.BufferedReader
import java.io.InputStreamReader
import java.security.spec.X509EncodedKeySpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.KeyFactory
import java.security.interfaces.RSAPublicKey
import java.security.interfaces.RSAPrivateKey


class DecryptDetailsActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_docId = "docId"
        private const val TAG = "DecryptDetailsActivity"
    }

    private lateinit var docIdTextView: TextView
    private lateinit var decryptedTextView: TextView

    private val db = FirebaseFirestore.getInstance()

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
        setContentView(R.layout.activity_decrypt_details)

        // Set the toolbar as the action bar for the activity
        val toolbar = findViewById<Toolbar>(R.id.toolbar2)
        toolbar.title = ""

        setSupportActionBar(toolbar)

        // Add a back button to the toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Get product details from intent extras
        val docId = intent.getStringExtra(EXTRA_docId) ?: "default"

        // Initialize UI elements
        docIdTextView = findViewById(R.id.docId)
        decryptedTextView = findViewById(R.id.decrypted)

        // Set product name to text view
        docIdTextView.text = docId

        // Get the document with code "newCode"
        db.collection("user").document(docId).get()
            .addOnSuccessListener { documentSnapshot ->
                val encryptedMessageBase64 = documentSnapshot.getString("message")

                // Move network operation to background thread
                AsyncTask.execute {
                    val url = URL("https://wahyurizaldi80.pythonanywhere.com/private_key")
                    val conn = url.openConnection() as HttpURLConnection
                    conn.requestMethod = "GET"
                    conn.connect()

                    if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                        val inputStream = conn.inputStream
                        val reader = BufferedReader(InputStreamReader(inputStream))
                        val privateKeyStringBuilder = StringBuilder()

                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            privateKeyStringBuilder.append(line)
                        }

                        val privateKeyString = privateKeyStringBuilder.toString()
                        Log.d("PRIVATE_KEY_STRING", privateKeyString)

                        val privateKeyPEM = privateKeyString
                            .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                            .replace("-----END RSA PRIVATE KEY-----", "")
                        Log.d("privateKeyPEM", privateKeyPEM)

                        val privateKeyBytes = Base64.decode(privateKeyPEM, Base64.DEFAULT)
                        val keySpec = PKCS8EncodedKeySpec(privateKeyBytes)
                        val keyFactory = KeyFactory.getInstance("RSA")
                        val privateKey = keyFactory.generatePrivate(keySpec) as RSAPrivateKey

                        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
                        cipher.init(Cipher.DECRYPT_MODE, privateKey)
                        val decryptedMessage = cipher.doFinal(Base64.decode(encryptedMessageBase64, Base64.DEFAULT))

                        runOnUiThread {
                            decryptedTextView.text = String(decryptedMessage)
                        }
                    }
                }
            }
            .addOnFailureListener {
                runOnUiThread {
                    Toast.makeText(this, "Failed to retrieve document", Toast.LENGTH_SHORT).show()
                }
            }

    }

    }
