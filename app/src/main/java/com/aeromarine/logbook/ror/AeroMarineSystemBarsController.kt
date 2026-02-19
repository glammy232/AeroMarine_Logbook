package com.aeromarine.logbook.ror

import android.app.Activity
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.core.view.WindowCompat

class ChickenSystemBarsController(private val activity: Activity) {

    private val window = activity.window
    private val decorView = window.decorView

    fun chickenSetupSystemBars() {
        val isLandscape = activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            chickenSetupForApi30Plus(isLandscape)
        } else {
            chickenSetupForApi24To29(isLandscape)
        }
    }

    private fun chickenSetupForApi30Plus(isLandscape: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowCompat.setDecorFitsSystemWindows(window, false)

            val insetsController = window.insetsController ?: return

            insetsController.systemBarsBehavior =
                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

            if (isLandscape) {
                insetsController.hide(
                    WindowInsets.Type.navigationBars() or WindowInsets.Type.statusBars()
                )
            } else {
                insetsController.hide(WindowInsets.Type.navigationBars())
                insetsController.show(WindowInsets.Type.statusBars())
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun chickenSetupForApi24To29(isLandscape: Boolean) {
        var flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        if (isLandscape) {
            flags = flags or (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }

        decorView.systemUiVisibility = flags

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.navigationBarColor = Color.TRANSPARENT
            // Светлые иконки навигации (если нужно)
            decorView.systemUiVisibility = flags and
                    View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
        }
    }

//    fun setupSystemBarsCompat() {
//        val isLandscape = activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
//
//        WindowCompat.setDecorFitsSystemWindows(window, false)
//
//        val insetsController = WindowCompat.getInsetsController(window, decorView)
//        insetsController.systemBarsBehavior =
//            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
//
//        if (isLandscape) {
//            // Landscape: скрываем оба бара
//            insetsController.hide(
//                WindowInsetsCompat.Type.navigationBars() or
//                        WindowInsetsCompat.Type.statusBars()
//            )
//        } else {
//            // Portrait: только Navigation Bar
//            insetsController.hide(WindowInsetsCompat.Type.navigationBars())
//            insetsController.show(WindowInsetsCompat.Type.statusBars())
//        }
//    }
}

fun Activity.chickenSetupSystemBars() {
    ChickenSystemBarsController(this).chickenSetupSystemBars()
}

//fun Activity.setupSystemBarsCompat() {
//    SystemBarsController(this).setupSystemBarsCompat()
//}
