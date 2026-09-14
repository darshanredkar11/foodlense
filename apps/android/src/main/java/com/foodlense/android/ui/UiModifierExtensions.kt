package com.foodlense.android.ui

import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.widthCompat(width: Dp): Modifier = this.then(Modifier.size(width, 24.dp))
