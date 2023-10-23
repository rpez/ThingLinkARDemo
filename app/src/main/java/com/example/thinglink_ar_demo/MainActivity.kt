package com.example.thinglink_ar_demo

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.example.thinglink_ar_demo.ui.theme.ThingLinkARDemoTheme
import com.example.thinglink_ar_demo.components.MainView

class MainActivity : ComponentActivity() {

    // Coroutine launcher for permissions
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ask for camera permissions
        requestPermissionLauncher.launch(
            Manifest.permission.CAMERA
        )

        // App UI
        setContent {
            ThingLinkARDemoTheme {
                MainView(applicationContext)
            }
        }
    }
}