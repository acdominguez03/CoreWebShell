package com.molinetenterprises.webviewkit.presentation.design_system

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.molinetenterprises.webviewkit.R

@Composable
fun DonateButton(
    modifier: Modifier = Modifier,
    onCLick: () -> Unit = {}
) {
    Image(
        modifier = modifier
            .size(40.dp)
            .clickable {
                onCLick()
            },
        painter = painterResource(R.drawable.donate_logo),
        contentDescription = "Donate Button"
    )
}

@Preview
@Composable
fun DonateButtonPreview() {
    DonateButton()
}