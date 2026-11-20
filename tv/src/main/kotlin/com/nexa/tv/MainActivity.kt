package com.nexa.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.nexa.tv.ui.TvApp
import com.nexa.ui.NexaTheme

class MainActivity : ComponentActivity() {
    private val vm: TvViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NexaTheme { TvApp(vm) }
        }
    }
}
