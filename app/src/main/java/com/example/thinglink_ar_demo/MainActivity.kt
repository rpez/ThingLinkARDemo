package com.example.thinglink_ar_demo

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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


class MainActivity : ComponentActivity() {

    private lateinit var session: Session
    private lateinit var imageDatabase: AugmentedImageDatabase
    private lateinit var config: Config

    private val messageSnackbarHelper = SnackbarHelper()

    private var installRequested: Boolean = false
    private var shouldConfigureSession: Boolean = false
    private var shouldCreateSession: Boolean = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        session = Session(this)

        setContent {
            ThingLinkARDemoTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()){
                        ARScreen(session)
                    }
                }
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Log.i("Permission: ", "Granted")
            } else {
                Log.i("Permission: ", "Denied")
            }
        }

    fun View.showSnackbar(
        view: View,
        msg: String,
        length: Int,
        actionMessage: CharSequence?,
        action: (View) -> Unit
    ) {
        val snackbar = Snackbar.make(view, msg, length)
        if (actionMessage != null) {
            snackbar.setAction(actionMessage) {
                action(this)
            }.show()
        } else {
            snackbar.show()
        }
    }

    fun requestPermission(view: View) {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                showSnackbar(
                    view,
                    getString(R.string.permission_granted),
                    Snackbar.LENGTH_INDEFINITE,
                    null
                ) {}
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CAMERA
            ) -> {
                layout.showSnackbar(
                    view,
                    getString(R.string.permission_required),
                    Snackbar.LENGTH_INDEFINITE,
                    getString(R.string.ok)
                ) {
                    requestPermissionLauncher.launch(
                        Manifest.permission.CAMERA
                    )
                }
            }

            else -> {
                requestPermissionLauncher.launch(
                    Manifest.permission.CAMERA
                )
            }
        }
    }

//    override fun onResume() {
//        super.onResume()
//
//        if (shouldCreateSession) {
//            var exception: Exception? = null
//            var message: String? = null
//            try {
//                when (ArCoreApk.getInstance().requestInstall(this, !installRequested)) {
//                    ArCoreApk.InstallStatus.INSTALL_REQUESTED -> {
//                        installRequested = true
//                        return
//                    }
//
//                    ArCoreApk.InstallStatus.INSTALLED -> {}
//                }
//
//                // ARCore requires camera permissions to operate. If we did not yet obtain runtime
//                // permission on Android M and above, now is a good time to ask the user for it.
//                if (!CameraPermissionHelper.hasCameraPermission(this)) {
//                    CameraPermissionHelper.requestCameraPermission(this)
//                    return
//                }
//                session = Session(this)
//            } catch (e: UnavailableArcoreNotInstalledException) {
//                message = "Please install ARCore"
//                exception = e
//            } catch (e: UnavailableUserDeclinedInstallationException) {
//                message = "Please install ARCore"
//                exception = e
//            } catch (e: UnavailableApkTooOldException) {
//                message = "Please update ARCore"
//                exception = e
//            } catch (e: UnavailableSdkTooOldException) {
//                message = "Please update this app"
//                exception = e
//            } catch (e: Exception) {
//                message = "This device does not support AR"
//                exception = e
//            }
//            if (message != null) {
//                messageSnackbarHelper.showError(this, message)
//                Log.e("Error", "Exception creating session", exception)
//                return
//            }
//            shouldConfigureSession = true
//            shouldCreateSession = false
//        }
//        if (shouldConfigureSession) {
//            configureSession()
//            shouldConfigureSession = false
//        }
//
//        // Note that order matters - see the note in onPause(), the reverse applies here.
//        try {
//            session.resume()
//        } catch (e: CameraNotAvailableException) {
//            messageSnackbarHelper.showError(this, "Camera not available. Try restarting the app.")
//            session.close()
//            return
//        }
////        surfaceView.onResume()
////        displayRotationHelper.onResume()
////        fitToScanView.setVisibility(View.VISIBLE)
//    }

//    override fun onPause() {
//        super.onPause()
//        shouldCreateSession = true
//        session.pause()
//    }

//    override fun onDestroy() {
//        session.close()
//        shouldCreateSession = true
//        super.onDestroy()
//    }

//    private fun configureSession() {
//        config = Config(session)
//        config.focusMode = Config.FocusMode.AUTO
//        if (!setupAugmentedImageDatabase(config)) {
//            messageSnackbarHelper.showError(this, "Could not setup augmented image database")
//        }
//        session.configure(config)
//    }
//
//    private fun setupAugmentedImageDatabase(config: Config): Boolean {
//        imageDatabase = this.assets.open("imagedb/images.imgdb").use {
//            AugmentedImageDatabase.deserialize(session, it)
//        }
//        if (imageDatabase.numImages == 0) return false
//
//        config.augmentedImageDatabase = imageDatabase
//        return true
//    }
}

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
            modelNode.value = ArModelNode(it.engine, placementMode = PlacementMode.INSTANT).apply {
                loadModelGlbAsync(
                    glbFileLocation = "models/sphere.glb",
                ){}
                onFrame = {_, _ ->
                    updateAugmentedImages(session, modelNode.value!!)
                }
            }
            arNodes.add(modelNode.value!!)
        }
    )
}

fun updateAugmentedImages(session: Session, model: ArModelNode) {
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