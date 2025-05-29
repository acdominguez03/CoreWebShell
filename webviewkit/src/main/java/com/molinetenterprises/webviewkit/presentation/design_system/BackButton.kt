package com.molinetenterprises.webviewkit.presentation.design_system

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.molinetenterprises.webviewkit.R

@Composable
fun BackButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Image(
        painter = painterResource(R.drawable.arrow_back),
        contentDescription = "Back Button",
        modifier = modifier
            .size(40.dp)
            .clickable {
                onClick()
            }
    )
}