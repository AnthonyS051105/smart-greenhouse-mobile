package com.teti2026.smartgreenhouse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.teti2026.smartgreenhouse.ui.navigation.GreenhouseNavGraph
import com.teti2026.smartgreenhouse.ui.theme.SmartgreenhousemobileTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartgreenhousemobileTheme {
                GreenhouseNavGraph()
            }
        }
    }
}
