package com.foodlense.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.foodlense.android.ui.FoodLenseApp

private val FoodLenseColors = lightColorScheme(
    primary = Color(0xFF315C45),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD7EBDD),
    onPrimaryContainer = Color(0xFF10311F),
    secondary = Color(0xFF7B5E35),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF4E2C5),
    background = Color(0xFFFBF9F4),
    onBackground = Color(0xFF1A1C19),
    surface = Color(0xFFFBF9F4),
    onSurface = Color(0xFF1A1C19),
    surfaceVariant = Color(0xFFECEFE8),
    onSurfaceVariant = Color(0xFF5D625B),
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = FoodLenseColors) {
                Surface {
                    FoodLenseApp()
                }
            }
        }
    }
}
