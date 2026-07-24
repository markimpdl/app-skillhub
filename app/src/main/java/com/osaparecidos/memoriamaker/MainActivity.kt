package com.osaparecidos.memoriamaker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.osaparecidos.memoriamaker.nav.AppNavHost
import com.osaparecidos.memoriamaker.ui.theme.SkillsCampTheme

/** Single Compose host for the whole Skills Camp arcade (portrait, fullscreen kiosk). */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SkillsCampTheme {
                // Opaque app-colored backdrop: if the nav graph ever renders an empty frame
                // (e.g. a rapid double-back momentarily empties the back stack), the user sees the
                // cream background instead of a stark-white "stuck" screen.
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    AppNavHost()
                }
            }
        }
    }
}
