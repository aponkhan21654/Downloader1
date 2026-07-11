package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.ui.screens.MainScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    // Store the shared URL state to pass it to the Composable
    private val sharedUrlState = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        
        // Request notification permission for Android 13+ to support system download notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        // Process any shared text intents if the app was launched via Share
        handleSendIntent(intent)

        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    var sharedUrl by remember { sharedUrlState }
                    
                    MainScreen(
                        initialSharedUrl = sharedUrl,
                        modifier = Modifier.padding(innerPadding)
                    )
                    
                    // Consume the intent URL after passing it
                    DisposableEffect(sharedUrl) {
                        onDispose {
                            sharedUrl = null
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleSendIntent(intent)
    }

    private fun handleSendIntent(intent: Intent?) {
        if (intent == null) return
        
        if (intent.action == Intent.ACTION_SEND && intent.type != null) {
            if (intent.type == "text/plain" || intent.type?.startsWith("text/") == true) {
                intent.getStringExtra(Intent.EXTRA_TEXT)?.let { sharedText ->
                    if (sharedText.isNotBlank()) {
                        sharedUrlState.value = sharedText
                    }
                }
            }
        }
    }
}
