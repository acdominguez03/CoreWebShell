package com.molinetenterprises.webviewkit.core

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp

fun Int.toDp(density: Density): Dp = with(density) { toDp() }