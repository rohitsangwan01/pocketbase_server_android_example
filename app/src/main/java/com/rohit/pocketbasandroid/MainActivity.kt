package com.rohit.pocketbasandroid

import android.R.attr.value
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pocketbaseMobile.PocketbaseMobile
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.SocketException


@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {
    private val uiScope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var txtDetails: TextView
    var hostName = "127.0.0.1"
    var port = "8090"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        txtDetails = findViewById(R.id.textView)
        val btnRun: Button = findViewById(R.id.btnRun)
        val btnStop: Button = findViewById(R.id.btnStop)
        val edtHost: TextInputEditText = findViewById(R.id.edtHost)
        val edtPort: TextInputEditText = findViewById(R.id.edtPort)
        val btnAdminView: Button = findViewById(R.id.btnAdminView)
        val sharedPreferences = getPreferences(MODE_PRIVATE)

        hostName = sharedPreferences.getString("hostname", getLocalIpAddress()) ?: hostName
        port = sharedPreferences.getString("port", port) ?: port


        // pass here ip address to access this in other devices under same network
        edtHost.setText(hostName)
        edtPort.setText(port)

        setPocketbaseCallbackListener()

        btnRun.setOnClickListener {
            val dataPath: String = this.cacheDir.absolutePath
            hostName = edtHost.text.toString()
            port = edtPort.text.toString()

            // check if empty values
            if (hostName.isEmpty() || port.isEmpty()) {
                txtDetails.text = "Error : hostName or port is empty"
                return@setOnClickListener
            }

            // save to sharedPref
            val editor = sharedPreferences.edit()
            editor.putString("hostname", hostName)
            editor.putString("port", port)
            editor.apply()

            // Start pocketbase server
            startPocketbase(dataPath, hostName, port)
        }

        btnStop.setOnClickListener {
            stopPocketbase()
        }

        btnAdminView.setOnClickListener {
            val adminUrl = "http://$hostName:$port/_/"
            val intent: Intent = Intent(this@MainActivity, AdminActivity::class.java)
            intent.putExtra("adminUrl", adminUrl)
            startActivity(intent)
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

    // Use localIp address to start pocketbaseServer
    private fun getLocalIpAddress(): String? {
        try {
            val en = NetworkInterface.getNetworkInterfaces()
            while (en.hasMoreElements()) {
                val enumIpAddress = en.nextElement().inetAddresses
                while (enumIpAddress.hasMoreElements()) {
                    val inetAddress = enumIpAddress.nextElement()
                    if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                        return inetAddress.getHostAddress()
                    }
                }
            }
        } catch (ex: SocketException) {
            ex.printStackTrace()
        }
        return null
    }
}

