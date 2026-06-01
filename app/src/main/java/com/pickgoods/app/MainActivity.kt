package com.pickgoods.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pickgoods.app.ui.navigation.AppNavGraph
import com.pickgoods.app.ui.theme.PickGoodsTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PickGoodsTheme {
                AppNavGraph()
            }
        }
    }
}
