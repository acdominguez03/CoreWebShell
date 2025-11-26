package com.molinetenterprises.webviewkit.presentation.design_system

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.getValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap

@Composable
fun FlatLinearProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(2.dp),
    backgroundColor: Color = Color.LightGray,
    colorSelector: (Float) -> Color = { progressValue ->
        when {
            progressValue < 0.4f -> Color(0xff53BDEB)
            progressValue < 0.85f -> Color.Yellow
            else -> Color.Green
        }
    }
) {

    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 400),
        label = "animatedProgress"
    )

    val color = colorSelector(animatedProgress)

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val filledWidth = width * animatedProgress

        drawRect(
            color = backgroundColor,
            size = Size(width, height)
        )

        drawLine(
            color = color,
            start = Offset(0f, height / 2),
            end = Offset(filledWidth, height / 2),
            strokeWidth = height,
            cap = StrokeCap.Butt
        )
    }
}