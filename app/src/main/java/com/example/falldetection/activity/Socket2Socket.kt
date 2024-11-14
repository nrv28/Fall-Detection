package com.example.falldetection.activity

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.falldetection.R
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class Socket2Socket : AppCompatActivity() {

    private lateinit var messageView: TextView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var ipAddressView: TextView

    private var clientSocket: Socket? = null
    private var out: PrintWriter? = null

    private val sharedPreferencesName = "FallDetectionPreferences"
    private val ipAddressKey = "IP_ADDRESS"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_socket2socket)

        messageView = findViewById(R.id.messageView)
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)
        ipAddressView = findViewById(R.id.ipAddressView)

        // Load saved IP address from shared preferences
        val sharedPreferences = getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
        val savedIp = sharedPreferences.getString(ipAddressKey, "")
        ipAddressView.text = "Connected Device IP: $savedIp"

        // Establish connection to the server
        if (!savedIp.isNullOrEmpty()) {
            connectToServer(savedIp)
        } else {
            showToast("No IP Address found! Please set the IP in settings.")
        }

        // Set onClick listener for the send button
        sendButton.setOnClickListener { onSendButtonClick() }
    }

    private fun connectToServer(serverIp: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                clientSocket = Socket(serverIp, 12345)  // Port number should match the server's port
                out = PrintWriter(clientSocket?.getOutputStream(), true)
                withContext(Dispatchers.Main) {
                    showToast("Connected to server at $serverIp")
                }
                listenForMessages(clientSocket)
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    showToast("Failed to connect to the server.")
                }
            }
        }
    }

    private fun listenForMessages(socket: Socket?) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val reader = BufferedReader(InputStreamReader(socket?.getInputStream()))
                var message: String?
                while (true) {
                    message = reader.readLine() ?: break
                    withContext(Dispatchers.Main) {
                        displayMessage("Server: $message")
                        if (message.equals("beep", ignoreCase = true)) {
                            startBeeping()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun startBeeping() {
        CoroutineScope(Dispatchers.IO).launch {
            val toneGenerator = android.media.ToneGenerator(android.media.AudioManager.STREAM_MUSIC, 100)
            for (i in 0 until 10) {  // Beep for 10 seconds
                toneGenerator.startTone(android.media.ToneGenerator.TONE_DTMF_1, 500)  // 500 ms beep
                delay(500)  // 500 ms delay
            }
            toneGenerator.release()  // Release the tone generator
        }
    }

    private fun sendMessage(message: String) {
        CoroutineScope(Dispatchers.IO).launch {
            out?.println(message)
            withContext(Dispatchers.Main) {
                displayMessage("You: $message")
                messageInput.text.clear()
            }
        }
    }

    private fun displayMessage(message: String) {
        messageView.append("$message\n")
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun onSendButtonClick() {
        val message = messageInput.text.toString()
        if (message.isNotEmpty()) {
            sendMessage(message)
        } else {
            showToast("Please enter a message.")
        }
    }
}
