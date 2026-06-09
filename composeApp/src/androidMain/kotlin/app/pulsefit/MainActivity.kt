package app.pulsefit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import app.pulsefit.db.DatabaseDriverFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val driver = DatabaseDriverFactory(this).createDriver()
        setContent {
            PulseApp(driver)
        }
    }
}
