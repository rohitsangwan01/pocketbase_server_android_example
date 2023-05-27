package com.rohit.pocketbasandroid

import android.Manifest.permission.POST_NOTIFICATIONS
import android.R.attr.value
import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.textfield.TextInputEditText
import pocketbaseMobile.PocketbaseMobile
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.SocketException


@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {
    private lateinit var txtDetails: TextView
    private lateinit var edtHost: TextInputEditText
    private lateinit var edtPort: TextInputEditText
    private lateinit var sharedPreferences: SharedPreferences
    private val notificationPermissionResultCode = 11
    private var hostName = Utils.defaultHostName
    private var port = Utils.defaultPort

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        txtDetails = findViewById(R.id.textView)
        edtHost = findViewById(R.id.edtHost)
        edtPort = findViewById(R.id.edtPort)
        val btnRun: Button = findViewById(R.id.btnRun)
        val btnStop: Button = findViewById(R.id.btnStop)
        val btnAdminView: Button = findViewById(R.id.btnAdminView)
        sharedPreferences = getPreferences(MODE_PRIVATE)
        hostName = sharedPreferences.getString("hostname", getLocalIpAddress()) ?: hostName
        port = sharedPreferences.getString("port", port) ?: port

        // pass here ip address to access this in other devices under same network
        edtHost.setText(hostName)
        edtPort.setText(port)

        setPocketbaseCallbackListener()

        btnRun.setOnClickListener {
            startPocketbaseService()
        }

        btnStop.setOnClickListener {
            stopPocketbaseService()
        }

        btnAdminView.setOnClickListener {
            val adminUrl = "http://$hostName:$port/_/"
            val intent: Intent = Intent(this@MainActivity, AdminActivity::class.java)
            intent.putExtra("adminUrl", adminUrl)
            startActivity(intent)
        }
    }

    private fun setPocketbaseCallbackListener() {
        PocketbaseMobile.registerNativeBridgeCallback { command, data ->
            Log.e("Test", "PocketbaseLogs :$command : $data \n")
            this.runOnUiThread {
                var text = "${txtDetails.text} \nCommand: $command \n"
                if (data != null && data.isNotEmpty()) {
                    text = "$text \nData: $data \n"
                }
                txtDetails.text = text
            }
            // return response back to pocketbase
            "response from native"
        }
    }

    private fun startPocketbaseService() {
        if (PocketbaseService.isRunning) {
            Toast.makeText(this, "PocketbaseService already running", Toast.LENGTH_LONG).show()
            return
        }
        if (!haveNotificationPermission()) return
        val dataPath: String = Utils.getStoragePath(this)
        Log.e("MainActivity", "PocketbaseDataPath : $dataPath")
        hostName = edtHost.text.toString()
        port = edtPort.text.toString()

        // check if empty values
        if (hostName.isEmpty() || port.isEmpty()) {
            txtDetails.text = "Error : hostName or port is empty"
            return
        }

        // save to sharedPref
        val editor = sharedPreferences.edit()
        editor.putString("hostname", hostName)
        editor.putString("port", port)
        editor.apply()

        // Start pocketbase server
        val intent = Intent(this, PocketbaseService::class.java)
        intent.putExtra("dataPath", dataPath)
        intent.putExtra("hostname", hostName)
        intent.putExtra("port", port)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopPocketbaseService() {
        if (!PocketbaseService.isRunning) {
            Toast.makeText(this, "PocketbaseService not running", Toast.LENGTH_LONG).show()
            return
        }
        val intent = Intent(this, PocketbaseService::class.java)
        intent.action = PocketbaseService.stopServiceAction
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun haveNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        if (ActivityCompat.checkSelfPermission(
                this,
                POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(POST_NOTIFICATIONS),
                notificationPermissionResultCode
            )
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == notificationPermissionResultCode
            && grantResults.isNotEmpty()
            && grantResults.first() == PackageManager.PERMISSION_GRANTED
        ) {
            startPocketbaseService()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
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

