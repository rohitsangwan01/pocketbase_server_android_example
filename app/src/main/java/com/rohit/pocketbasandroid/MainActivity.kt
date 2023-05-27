package com.rohit.pocketbasandroid

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import pocketbaseMobile.PocketbaseMobile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {

    private val uiScope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var txtDetails: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        txtDetails = findViewById(R.id.textView)
        val btnRun: Button = findViewById(R.id.btnRun)
        val btnStop: Button = findViewById(R.id.btnStop)
        val edtHost: TextInputEditText = findViewById(R.id.edtHost)
        val edtPort: TextInputEditText = findViewById(R.id.edtPort)

        // pass here ip address to access this in other devices under same network
        edtHost.setText("127.0.0.1")
        edtPort.setText("8090")

        setPocketbaseCallbackListener()

        btnRun.setOnClickListener {
            val dataPath: String = this.cacheDir.absolutePath
            val hostName = edtHost.text.toString()
            val port = edtPort.text.toString()
            if (hostName.isEmpty() || port.isEmpty()) {
                txtDetails.text = "Error : hostName or port is empty"
                return@setOnClickListener
            }
            startPocketbase(dataPath, hostName, port)
        }

        btnStop.setOnClickListener {
            stopPocketbase()
        }
    }

    // Register a listener to get callbacks from pocketBase and return response
    private fun setPocketbaseCallbackListener() {
        PocketbaseMobile.registerNativeBridgeCallback { command, data ->
            Log.e("Test", "PocketbaseLogs :$command : $data \n")
            this.runOnUiThread {
                txtDetails.text = "${txtDetails.text} \nCommand: $command \nData: $data \n\n"
            }
            // return response back to pocketbase
            "response from native"
        }
    }

    // StartPocketbase server
    private fun startPocketbase(dataPath: String, hostname: String, port: String) {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                PocketbaseMobile.startPocketbase(dataPath, hostname, port)
            }
        }
    }

    // StopPocketbase server, this will close application as well (find better way to stop pocketbase)
    private fun stopPocketbase() {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                PocketbaseMobile.stopPocketbase()
            }
        }
    }
}

