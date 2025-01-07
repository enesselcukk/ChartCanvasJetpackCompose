package com.example.chartcanvasjetpackcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.chartcanvasjetpackcompose.component.CryptoChart
import com.example.chartcanvasjetpackcompose.ui.theme.ChartCanvasJetpackComposeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChartCanvasJetpackComposeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CryptoChart()
                }
            }
        }
    }
}
