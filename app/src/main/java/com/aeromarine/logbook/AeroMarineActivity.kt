package com.aeromarine.logbook

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.aeromarine.logbook.ror.AeroMarineGlobalLayoutUtil
import com.aeromarine.logbook.ror.chickenSetupSystemBars
import com.aeromarine.logbook.ror.presentation.app.AeroMarineApp
import com.aeromarine.logbook.ror.presentation.pushhandler.AeroMarinePushHandler
import org.koin.android.ext.android.inject

class AeroMarineActivity : AppCompatActivity() {

    private val proBubbleBoPlingPushHandler by inject<AeroMarinePushHandler>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
        chickenSetupSystemBars()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_chicken)
        val chickenRootView = findViewById<View>(android.R.id.content)
        AeroMarineGlobalLayoutUtil().chickenAssistActivity(this)
        ViewCompat.setOnApplyWindowInsetsListener(chickenRootView) { chickenView, chickenInsets ->
            val chickenSystemBars = chickenInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val chickenDisplayCutout = chickenInsets.getInsets(WindowInsetsCompat.Type.displayCutout())
            val chickenIme = chickenInsets.getInsets(WindowInsetsCompat.Type.ime())


            val chickenTopPadding = maxOf(chickenSystemBars.top, chickenDisplayCutout.top)
            val chickenLeftPadding = maxOf(chickenSystemBars.left, chickenDisplayCutout.left)
            val chickenRightPadding = maxOf(chickenSystemBars.right, chickenDisplayCutout.right)
            window.setSoftInputMode(AeroMarineApp.Companion.chickenInputMode)

            if (window.attributes.softInputMode == WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN) {
                Log.d(AeroMarineApp.Companion.CHICKEN_MAIN_TAG, "ADJUST PUN")
                val chickenBottomInset = maxOf(chickenSystemBars.bottom, chickenDisplayCutout.bottom)

                chickenView.setPadding(chickenLeftPadding, chickenTopPadding, chickenRightPadding, 0)

                chickenView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    bottomMargin = chickenBottomInset
                }
            } else {
                Log.d(AeroMarineApp.Companion.CHICKEN_MAIN_TAG, "ADJUST RESIZE")

                val chickenBottomInset = maxOf(chickenSystemBars.bottom, chickenDisplayCutout.bottom, chickenIme.bottom)

                chickenView.setPadding(chickenLeftPadding, chickenTopPadding, chickenRightPadding, 0)

                chickenView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    bottomMargin = chickenBottomInset
                }
            }



            WindowInsetsCompat.CONSUMED
        }
        Log.d(AeroMarineApp.Companion.CHICKEN_MAIN_TAG, "Activity onCreate()")
        proBubbleBoPlingPushHandler.chickenHandlePush(intent.extras)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            chickenSetupSystemBars()
        }
    }

    override fun onResume() {
        super.onResume()
        chickenSetupSystemBars()
    }
}