package com.molinetenterprises.webviewkit.presentation.error_screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.molinetenterprises.webviewkit.R
import com.molinetenterprises.webviewkit.theme.MaintenanceErrorBackground

@Composable
fun MaintenanceErrorScreen(
    statusCode: Int
) {
    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(MaintenanceErrorBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp, alignment = Alignment.CenterVertically),
        horizontalAlignment = Alignment.Start
    ) {
        Image(
            painter = painterResource(R.drawable.maintenance_error_screen),
            contentDescription = "Maintenance",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxWidth().height(250.dp),
        )

        Text(
            text = "App crashed due to an unexpected error",
            color = Color.White,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge,
        )

        Text(
            modifier = Modifier.padding(start = 20.dp),
            text = if (statusCode == 999) "Error desconocido" else "Error code: $statusCode",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge,
        )

        Text(
            text = "Please close and reopen the app to try again",
            color = Color.White,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ErrorScreenPreview() {
    MaterialTheme {
        MaintenanceErrorScreen(statusCode = 404)
    }
}