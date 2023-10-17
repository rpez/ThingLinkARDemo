package com.example.thinglink_ar_demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.thinglink_ar_demo.ui.theme.ThingLinkARDemoTheme
import com.google.ar.core.Config.LightEstimationMode
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.ArNode
import io.github.sceneview.ar.node.PlacementMode

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
}

@Composable
fun ARScreen() {
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
                    glbFileLocation = "",
                ){

                }
                onAnchorChanged = {

                }
                onHitResult = {node, hitResult ->

                }
                arNodes.add(modelNode.value!!)
            }
        }
    )
}