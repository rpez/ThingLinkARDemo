package com.example.thinglink_ar_demo.components

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import com.example.thinglink_ar_demo.R

@Composable
fun InfoPopup(context: Context, showPopup: MutableState<Boolean>) {
    // Popup showing info (placeholder text). Both dismiss and confirm just close the dialog.
    AlertDialog(
        icon = {
            Icon(Icons.Filled.Info, contentDescription = "Info Icon")
        },
        title = {
            Text(context.getString(R.string.placeholder_title))
        },
        text = {
            Text(context.getString(R.string.placeholder_string_long))
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
                Text(context.getString(R.string.ok))
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    showPopup.value = false
                }
            ) {
                Text(context.getString(R.string.dismiss))
            }
        }
    )
}