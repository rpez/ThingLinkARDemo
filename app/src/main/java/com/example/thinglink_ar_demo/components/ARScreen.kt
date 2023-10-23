package com.example.thinglink_ar_demo.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.google.ar.core.Config
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.ArNode
import io.github.sceneview.ar.node.PlacementMode

@Composable
fun ARScreen(showPopup: MutableState<Boolean>) {
    // The AR nodes in the scene
    val arNodes = remember { mutableListOf<ArNode>() }
    // The 3D models in the scene
    val modelNode = remember { mutableStateOf<ArModelNode?>(null) }
    // Whether or not the model has been placed
    val modelPlaced = remember { mutableStateOf(false) }

    // AR scene with a placeable object
    ARScene(
        modifier = Modifier.fillMaxSize(),
        nodes = arNodes,
        planeRenderer = true,
        onCreate = {
            it.lightEstimationMode = Config.LightEstimationMode.DISABLED
            it.planeRenderer.isShadowReceiver = false
            modelNode.value =
                ArModelNode(it.engine, placementMode = PlacementMode.INSTANT).apply {
                    loadModelGlbAsync(
                        glbFileLocation = "models/map_pointer_3d_icon.glb",
                        scaleToUnits = 0.3f
                    ) {}
                }
            modelNode.value?.onTap = { _, _ ->
                if (modelPlaced.value) showPopup.value = true
            }
            arNodes.add(modelNode.value!!)
        },
        onTap = { _ ->
            // Anchor 3D object to current place, if it is not anchored yet
            if (!modelPlaced.value) {
                modelNode.value?.anchor()
                modelPlaced.value = true
            }
        }
    )
}