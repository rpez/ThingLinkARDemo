package com.example.thinglink_ar_demo

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.mediumTopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.thinglink_ar_demo.ui.theme.ThingLinkARDemoTheme
import com.google.ar.core.Config.LightEstimationMode
import com.google.ar.core.Session
import com.google.ar.sceneform.math.Vector3
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.arcore.position
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.ArNode
import io.github.sceneview.ar.node.PlacementMode
import io.github.sceneview.math.toVector3

class MainActivity : ComponentActivity() {

    // How close to the object are taps registered
    private val objectTapRadius: Float = 1.0f
    // The AR session
    private lateinit var session: Session
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

        // Create session
        session = Session(this)

        // App UI
        setContent {
            ThingLinkARDemoTheme {
                MainView()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainView() {
        // Should the info popup be shown
        val showPopup = remember { mutableStateOf<Boolean>(false) }

        // All content of the app is within this scaffold
        Scaffold(
            topBar = {
                TopAppBar(
                    colors = mediumTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                    title = {
                        Text("ThingLink Ar Demo")
                    }
                )
            },
            bottomBar = {
                BottomAppBar(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.primary,
                ) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        text = "Tap the screen to anchor the object. Tap the anchored object to show info.",
                    )
                }
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        ARScreen(showPopup)
                        if (showPopup.value) InfoPopup(showPopup)
                    }
                }
            }
        }
    }

    @Composable
    fun ARScreen(showPopup: MutableState<Boolean>) {
        // The AR nodes in the scene
        val arNodes = remember { mutableListOf<ArNode>() }
        // The 3D models in the scene
        val modelNode = remember { mutableStateOf<ArModelNode?>(null) }
        // Whether or not the model has been placed
        val modelPlaced = remember { mutableStateOf<Boolean>(false) }
        // The current position of the placed model
        val nodePosition = remember { mutableStateOf<Vector3>(Vector3.zero()) }

        // AR scene with a placeable object
        ARScene(
            modifier = Modifier.fillMaxSize(),
            nodes = arNodes,
            planeRenderer = true,
            onCreate = {
                it.lightEstimationMode = LightEstimationMode.DISABLED
                it.planeRenderer.isShadowReceiver = false
                modelNode.value =
                    ArModelNode(it.engine, placementMode = PlacementMode.INSTANT).apply {
                        loadModelGlbAsync(
                            glbFileLocation = "models/sphere.glb",
                        ) {}
                    }
                modelNode.value?.onTap = { _, _ ->
                    if (modelPlaced.value) showPopup.value = true
                }
                arNodes.add(modelNode.value!!)
            },
            onTap = { hitResult ->
                // Anchor 3D object to current place, if it is not anchored yet
                if (!modelPlaced.value) {
                    modelNode.value?.anchor()
                    modelPlaced.value = true
                    nodePosition.value = hitResult.hitPose.position.toVector3()
                }
            }
        )
    }

    @Composable
    fun InfoPopup(showPopup: MutableState<Boolean>) {
        // Popup showing info (placeholder text). Both dismiss and confirm just close the dialog.
        AlertDialog(
            icon = {
                Icon(Icons.Filled.Info, contentDescription = "Info Icon")
            },
            title = {
                Text(getString(R.string.placeholder_title))
            },
            text = {
                Text(getString(R.string.placeholder_string_long))
            },
            onDismissRequest = {
                showPopup.value = false
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPopup.value = false
                    }
                ) {
                    Text(getString(R.string.ok))
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showPopup.value = false
                    }
                ) {
                    Text(getString(R.string.dismiss))
                }
            }
        )
    }
}