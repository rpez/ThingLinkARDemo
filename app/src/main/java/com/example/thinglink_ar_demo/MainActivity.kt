package com.example.thinglink_ar_demo

import android.Manifest
import android.opengl.GLES20
import android.os.Bundle
import android.util.Log
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.mediumTopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.thinglink_ar_demo.ui.theme.ThingLinkARDemoTheme
import com.google.ar.core.AugmentedImage
import com.google.ar.core.AugmentedImageDatabase
import com.google.ar.core.Camera
import com.google.ar.core.Config
import com.google.ar.core.Config.LightEstimationMode
import com.google.ar.core.Frame
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.ArNode
import io.github.sceneview.ar.node.PlacementMode
import javax.microedition.khronos.opengles.GL10


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
//        imageDatabase = this.assets.open("imagedb/images.imgdb").use {
//            AugmentedImageDatabase.deserialize(session, it)
//        }
//        config = Config(session)
//        config.augmentedImageDatabase = imageDatabase
//        session.configure(config)
//        session.resume()

//        val genTextures = IntArray(1){0}
//        GLES20.glGenTextures(1, genTextures, 0)
//        session.setCameraTextureName(genTextures[0])

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
                        text = "Press the button to anchor the object",
                    )
                }
            },
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    text = { Text("Anchor") },
                    icon = { Icon(Icons.Filled.Check, contentDescription = "") },
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
                    }
                arNodes.add(modelNode.value!!)
            }
        )
    }

    private fun onDrawFrame(model: ArModelNode) {
        // Clear screen to notify driver it should not load any pixels from previous frame.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        try {
            // session.setCameraTextureName(backgroundRenderer.getTextureId())

            // Obtain the current frame from ARSession. When the configuration is set to
            // UpdateMode.BLOCKING (it is by default), this will throttle the rendering to the
            // camera framerate.
            val frame = session.update()

            // Visualize augmented images.
            updateAugmentedImages(model, frame)
        } catch (t: Throwable) {
            // Avoid crashing the application due to unhandled exceptions.
            Log.e("Error", "Exception on the OpenGL thread", t)
        }
    }

    private fun updateAugmentedImages(model: ArModelNode, frame: Frame) {
        val updatedAugmentedImages = frame.getUpdatedTrackables(AugmentedImage::class.java)

        for (img in updatedAugmentedImages) {
            if (img.trackingState == TrackingState.TRACKING) {
                when (img.trackingMethod) {
                    AugmentedImage.TrackingMethod.LAST_KNOWN_POSE -> {
                        // The planar target is currently being tracked based on its last known pose.
                        Log.d("DEBUG", "LAST KNOW POSE")
                    }

                    AugmentedImage.TrackingMethod.FULL_TRACKING -> {
                        // The planar target is being tracked using the current camera image.
                        Log.d("DEBUG", "FULL TRACKING")
                    }

                    AugmentedImage.TrackingMethod.NOT_TRACKING -> {
                        // The planar target isn't been tracked.
                        Log.d("DEBUG", "NOT TRACKING")
                    }
                }
            }
        }
    }
}