package app.pulsefit

import androidx.compose.ui.window.ComposeUIViewController
import app.pulsefit.db.DatabaseDriverFactory

fun MainViewController() = ComposeUIViewController {
    val driver = DatabaseDriverFactory().createDriver()
    PulseApp(driver)
}
