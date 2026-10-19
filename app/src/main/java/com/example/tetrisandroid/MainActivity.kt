package com.example.tetrisandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.tetrisandroid.ui.TetrisScreen
import com.example.tetrisandroid.ui.theme.TetrisAndroidTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TetrisAndroidTheme {
                TetrisScreen()
            }
        }
    }
}