package com.molinetenterprises.webviewkit.presentation.design_system

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.molinetenterprises.webviewkit.R
import com.molinetenterprises.webviewkit.theme.ErrorBackground

@Composable
fun ErrorPage() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ErrorBackground)
    ) {
        Image(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize(),
            painter = painterResource(R.drawable.server_error_screen),
            contentDescription = "404 Web Error"
        )
    }
}

@Preview
@Composable
fun ErrorPagePreview() {
    ErrorPage()
}