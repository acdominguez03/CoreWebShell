package com.molinetenterprises.webviewkit.presentation.design_system

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.molinetenterprises.webviewkit.R
import com.molinetenterprises.webviewkit.theme.ErrorBackground
import com.molinetenterprises.webviewkit.theme.RecoveryBackground

@Composable
fun ConnectionBanner(
    haveBottomBar: Boolean = false,
    isError: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = if (haveBottomBar) 65.dp else 0.dp)
            .background(if (isError) ErrorBackground else RecoveryBackground)
            .padding(5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(id = if (isError) R.string.no_connection_error else R.string.connection_recovered),
            color = Color.White,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Preview
@Composable
fun ConnectionBannerPreview() {
    ConnectionBanner(isError = false)
}