package app.pulsefit

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import app.pulsefit.db.DatabaseDriverFactory

fun main() = application {
    val driver = DatabaseDriverFactory().createDriver()
    Window(
        onCloseRequest = ::exitApplication,
        title = "PulseKMP"
    ) {
        PulseApp(driver)
    }
}
