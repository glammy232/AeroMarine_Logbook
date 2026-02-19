package com.aeromarine.logbook

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import com.aeromarine.logbook.ui.FishLocationLogApp
import com.aeromarine.logbook.viewmodel.TaskViewModel
import android.util.Base64
import android.util.Log
import androidx.annotation.RequiresApi

import java.security.MessageDigest

class MainActivity : ComponentActivity() {
    private val viewModel: TaskViewModel by viewModels()
    
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //getAppKeyHash("com.aeromarine.logbook")
        //getAppKeyHashHex("com.aeromarine.logbook")
        //getAppKeyHash256("com.aeromarine.logbook")
        //getAppKeyHashHex256("com.aeromarine.logbook")
        // Enable edge-to-edge display
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            //ChickenApp(viewModel = viewModel)
            FishLocationLogApp(viewModel)
        }
    }
    fun getAppKeyHash(pack: String) {
        try {
            val packageInfo: PackageInfo = packageManager.getPackageInfo(
                pack,
                PackageManager.GET_SIGNATURES
            )

            for (signature in packageInfo.signatures!!) {
                val md = MessageDigest.getInstance("SHA-1")
                md.update(signature.toByteArray())
                val keyHash = Base64.encodeToString(md.digest(), Base64.DEFAULT)
                Log.d("KEY_HASH", "Key Hash: $keyHash")
                // Выведет в Logcat что-то вроде: "abc123def456..."
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getAppKeyHashHex(pack: String) {
        try {
            val packageInfo: PackageInfo = packageManager.getPackageInfo(
                pack,
                PackageManager.GET_SIGNATURES
            )

            for (signature in packageInfo.signatures!!) {
                val md = MessageDigest.getInstance("SHA-1")
                md.update(signature.toByteArray())
                val digest = md.digest()

                // HEX формат (как в signingReport)
                val hexString = StringBuilder()
                for (i in digest.indices) {
                    if (i > 0) hexString.append(":")
                    val hex = Integer.toHexString(0xff and digest[i].toInt())
                    if (hex.length == 1) hexString.append('0')
                    hexString.append(hex)
                }
                Log.d("KEY_HASH", "Key Hash (HEX): ${hexString}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getAppKeyHash256(pack: String) {
        try {
            val packageInfo: PackageInfo = packageManager.getPackageInfo(
                pack,
                PackageManager.GET_SIGNATURES
            )

            for (signature in packageInfo.signatures!!) {
                val md = MessageDigest.getInstance("SHA-256")
                md.update(signature.toByteArray())
                val keyHash = Base64.encodeToString(md.digest(), Base64.DEFAULT)
                Log.d("KEY_HASH256", "Key 256: $keyHash")
                // Выведет в Logcat что-то вроде: "abc123def456..."
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getAppKeyHashHex256(pack: String) {
        try {
            val packageInfo: PackageInfo = packageManager.getPackageInfo(
                pack,
                PackageManager.GET_SIGNATURES
            )

            for (signature in packageInfo.signatures!!) {
                val md = MessageDigest.getInstance("SHA-256")
                md.update(signature.toByteArray())
                val digest = md.digest()

                // HEX формат (как в signingReport)
                val hexString = StringBuilder()
                for (i in digest.indices) {
                    if (i > 0) hexString.append(":")
                    val hex = Integer.toHexString(0xff and digest[i].toInt())
                    if (hex.length == 1) hexString.append('0')
                    hexString.append(hex)
                }
                Log.d("KEY_HASH256", "Key Hash (HEX)256: ${hexString}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
