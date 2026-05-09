package me.asteroidus.swissgrades

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import me.asteroidus.swissgrades.ui.app.GradeTrackerApp
import me.asteroidus.swissgrades.ui.theme.SwissGradesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SwissGradesTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    GradeTrackerApp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GradeTrackerPreview() {
    SwissGradesTheme {
        GradeTrackerApp(modifier = Modifier.fillMaxSize())
    }
}
