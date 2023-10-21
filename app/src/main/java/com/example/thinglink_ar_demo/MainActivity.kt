package com.example.thinglink_ar_demo

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.thinglink_ar_demo.common.helpers.CameraPermissionHelper
import com.example.thinglink_ar_demo.common.helpers.SnackbarHelper
import com.example.thinglink_ar_demo.ui.theme.ThingLinkARDemoTheme
import com.google.android.material.snackbar.Snackbar
import com.google.ar.core.ArCoreApk
import com.google.ar.core.AugmentedImage
import com.google.ar.core.AugmentedImageDatabase
import com.google.ar.core.Config
import com.google.ar.core.Config.LightEstimationMode
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.core.exceptions.UnavailableApkTooOldException
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException
import com.google.ar.core.exceptions.UnavailableSdkTooOldException
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.arcore.position
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.ArNode
import io.github.sceneview.ar.node.PlacementMode
import androidx.compose.material3.TopAppBarDefaults.mediumTopAppBarColors
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material.icons.filled.Info
import kotlinx.coroutines.launch
import android.Manifest
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.view.Surface
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.MutableState
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat

class MainActivity : ComponentActivity() {

    private lateinit var session: Session
    private lateinit var imageDatabase: AugmentedImageDatabase
    private lateinit var config: Config

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { _ ->

        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissionLauncher.launch(
            Manifest.permission.CAMERA
        )

        session = Session(this)
        imageDatabase = this.assets.open("imagedb/images.imgdb").use {
            AugmentedImageDatabase.deserialize(session, it)
        }
        config = Config(session)
        config.augmentedImageDatabase = imageDatabase
        session.configure(config)
        session.resume()

        val genTextures = IntArray(1){0}
        GLES20.glGenTextures(1, genTextures, 0)
        session.setCameraTextureName(genTextures[0])

        setContent {
            ThingLinkARDemoTheme {
                MainView()
            }
        }
    }

    @Preview
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainView() {
        val scope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }

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
                        text = "Scan Mona Lisa",
                    )
                }
            },
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    text = { Text("Start") },
                    icon = { Icon(Icons.Filled.Info, contentDescription = "") },
                    onClick = {

                    }
                )
            }
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
                        ARScreen(session)
                    }
                }
            }
        }
    }

//    @Preview
    @Composable
    fun ARScreen(session: Session) {
        val arNodes = remember { mutableListOf<ArNode>() }
        val modelNode = remember { mutableStateOf<ArModelNode?>(null) }

        // Construct AR scene
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
                        onFrame = { _, _ ->
                            updateAugmentedImages(session, modelNode.value!!)
                        }
                    }
                arNodes.add(modelNode.value!!)
            }
        )
    }

    private fun updateAugmentedImages(session: Session, model: ArModelNode) {
        val frame = session.update()
        val updatedAugmentedImages = frame.getUpdatedTrackables(AugmentedImage::class.java)

        for (img in updatedAugmentedImages) {
            if (img.trackingState == TrackingState.TRACKING) {
                when (img.trackingMethod) {
                    AugmentedImage.TrackingMethod.LAST_KNOWN_POSE -> {
                        // The planar target is currently being tracked based on its last known pose.
                        model.isVisible = true
                        model.position = img.centerPose.position
                        model.anchor()
                    }

                    AugmentedImage.TrackingMethod.FULL_TRACKING -> {
                        // The planar target is being tracked using the current camera image.
                        model.isVisible = true
                        model.position = img.centerPose.position
                        model.anchor()
                    }

                    AugmentedImage.TrackingMethod.NOT_TRACKING -> {
                        // The planar target isn't been tracked.
                        model.isVisible = false
                    }
                }
            }
        }
    }
}