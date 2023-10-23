package com.example.thinglink_ar_demo.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
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
                            scaleToUnits = 0.3f // Hardcoded scale could be replaced with a value file reference in the future
                        ) {}
                    }
                modelNode.value?.onTap = { _, _ ->
                    if (modelPlaced.value) showPopup.value = true
                }
                arNodes.add(modelNode.value!!)
            },
        )
        ExtendedFloatingActionButton(
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
            onClick = {
                // Anchor 3D object to current place, if it is not anchored yet
                if (!modelPlaced.value) {
                    modelNode.value?.anchor()
                    modelPlaced.value = true
                }
                else {
                    modelNode.value?.anchor?.detach()
                    modelNode.value?.placementMode = PlacementMode.INSTANT
                    modelPlaced.value = false
                }
            }
        ) {
            if (!modelPlaced.value) {
                Icon(Icons.Filled.Check, contentDescription = "")
                Text("Anchor")
            }
            else {
                Icon(Icons.Filled.Clear, contentDescription = "")
                Text("Detach")
            }
        }
    }
}