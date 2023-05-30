package com.example.myencryptapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText
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
import java.security.KeyFactory
import java.security.interfaces.RSAPublicKey



class MainActivity : AppCompatActivity() {
    private lateinit var getMessage: EditText
    private lateinit var saveButton: Button

    private val db = FirebaseFirestore.getInstance()

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_dropdown -> {
                val anchorView = findViewById<View>(R.id.menu_item_dropdown)

                val popupMenu = PopupMenu(this, anchorView)
                popupMenu.menuInflater.inflate(R.menu.dropdown_menu, popupMenu.menu)
                popupMenu.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.menu_item_1 -> {
                            startActivity(Intent(this, DecryptActivity::class.java))
                            true
                        }

                        R.id.menu_item_2 -> {
                            startActivity(Intent(this, AboutActivity::class.java))
                            true
                        }

                        else -> false
                    }
                }
                popupMenu.show()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set the toolbar as the action bar for the activity
        val toolbar = findViewById<Toolbar>(R.id.toolbar1)
        toolbar.title = ""
        setSupportActionBar(toolbar)

        getMessage = findViewById(R.id.message)
        saveButton = findViewById(R.id.submitBtn)

        // Get the root view of the activity
        val rootView = findViewById<View>(android.R.id.content)

        // Set an OnTouchListener on the root view
        rootView.setOnTouchListener { v, event ->
            // Check if the user tapped outside of the EditText
            if (event.action == MotionEvent.ACTION_DOWN &&
                currentFocus is EditText &&
                !v.equals(currentFocus)
            ) {
                // Hide the keyboard
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
            }
            false
        }


        saveButton.setOnClickListener {
            val message = getMessage.text.toString().trim()

            AsyncTask.execute {
                // Move network operation to background thread
                val url = URL("https://wahyurizaldi80.pythonanywhere.com/public_key")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connect()

                if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = conn.inputStream
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val publicKeyStringBuilder = StringBuilder()

                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        publicKeyStringBuilder.append(line)
                    }

                    val publicKeyString = publicKeyStringBuilder.toString()
                    Log.d("PUBLIC_KEY_STRING", publicKeyString)

                    val publicKeyPEM = publicKeyString
                        .replace("-----BEGIN PUBLIC KEY-----", "")
                        .replace("-----END PUBLIC KEY-----", "")
                    Log.d("publicKeyPEM", publicKeyPEM)

                    val publicKeyBytes = Base64.decode(publicKeyPEM, Base64.DEFAULT)
                    val keySpec = X509EncodedKeySpec(publicKeyBytes)
                    val keyFactory = KeyFactory.getInstance("RSA")
                    val publicKey = keyFactory.generatePublic(keySpec) as RSAPublicKey

                    val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
                    cipher.init(Cipher.ENCRYPT_MODE, publicKey)
                    val encryptedMessage = cipher.doFinal(message.toByteArray(Charsets.UTF_8))

                    val encryptedMessageBase64 = Base64.encodeToString(encryptedMessage, Base64.DEFAULT)

                    val userMap = hashMapOf(
                        "message" to encryptedMessageBase64
                    )

                    val newCode = Random().nextInt(899999999) + 100000000

                    db.collection("user").document(newCode.toString()).set(userMap)
                        .addOnSuccessListener {
                            runOnUiThread {
                                Toast.makeText(this, "successfully added", Toast.LENGTH_SHORT).show()
                                getMessage.text.clear()
                            }
                        }
                        .addOnFailureListener {
                            runOnUiThread {
                                Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
        }


    }
}
