package com.aeromarine.logbook.ror

import android.R
import android.app.Activity
import android.graphics.Rect
import android.view.View
import android.widget.FrameLayout
import com.aeromarine.logbook.ror.presentation.app.AeroMarineApp

class AeroMarineGlobalLayoutUtil {

    private var chickenMChildOfContent: View? = null
    private var chickenUsableHeightPrevious = 0

    fun chickenAssistActivity(activity: Activity) {
        val content = activity.findViewById<FrameLayout>(R.id.content)
        chickenMChildOfContent = content.getChildAt(0)

        chickenMChildOfContent?.viewTreeObserver?.addOnGlobalLayoutListener {
            possiblyResizeChildOfContent(activity)
        }
    }

    private fun possiblyResizeChildOfContent(activity: Activity) {
        val chickenUsableHeightNow = chickenComputeUsableHeight()
        if (chickenUsableHeightNow != chickenUsableHeightPrevious) {
            val chickenUsableHeightSansKeyboard = chickenMChildOfContent?.rootView?.height ?: 0
            val chickenHeightDifference = chickenUsableHeightSansKeyboard - chickenUsableHeightNow

            if (chickenHeightDifference > (chickenUsableHeightSansKeyboard / 4)) {
                activity.window.setSoftInputMode(AeroMarineApp.Companion.chickenInputMode)
            } else {
                activity.window.setSoftInputMode(AeroMarineApp.Companion.chickenInputMode)
            }
//            mChildOfContent?.requestLayout()
            chickenUsableHeightPrevious = chickenUsableHeightNow
        }
    }

    private fun chickenComputeUsableHeight(): Int {
        val r = Rect()
        chickenMChildOfContent?.getWindowVisibleDisplayFrame(r)
        return r.bottom - r.top  // Visible height без status bar
    }
}