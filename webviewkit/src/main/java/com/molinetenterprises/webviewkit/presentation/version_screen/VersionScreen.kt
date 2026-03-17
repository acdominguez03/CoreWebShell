package com.molinetenterprises.webviewkit.presentation.version_screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.molinetenterprises.webviewkit.R
import com.molinetenterprises.webviewkit.theme.NewVersionBackground

@Composable
fun VersionScreen() {
    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(NewVersionBackground),
        verticalArrangement = Arrangement.spacedBy(15.dp, alignment = Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.new_version_screen),
            contentDescription = "Maintenance",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxHeight()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun VersionScreenPreview() {
    VersionScreen()
}