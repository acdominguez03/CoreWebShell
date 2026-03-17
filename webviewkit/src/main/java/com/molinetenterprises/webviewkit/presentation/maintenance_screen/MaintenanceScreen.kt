package com.molinetenterprises.webviewkit.presentation.maintenance_screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.molinetenterprises.webviewkit.R
import com.molinetenterprises.webviewkit.core.Utils.epochToCET

@Composable
fun MaintenanceScreen(
    startEpoch: Long,
    endEpoch: Long
) {
    val startText = remember { epochToCET(startEpoch) }
    val endText = remember { epochToCET(endEpoch) }

    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        verticalArrangement = Arrangement.spacedBy(15.dp, alignment = Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.service_maintenance),
            contentDescription = "Maintenance",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxWidth().height(300.dp)
        )

        Text(
            text = "Service is under maintenance between",
            color = Color.Black,
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = startText,
            color = Color.Black,
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = MaterialTheme.typography.titleLarge.fontSize * 1.15f
            )
        )
        Text(
            text = "and",
            color = Color.Black,
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = endText,
            color = Color.Black,
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = MaterialTheme.typography.titleLarge.fontSize * 1.15f
            )
        )
    }
}

@Preview
@Composable
fun MaintenanceScreenPreview() {
    MaintenanceScreen(startEpoch = 1766782061, endEpoch = 1766868461)
}