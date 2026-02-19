package com.aeromarine.logbook.ror.presentation.ui.view


import android.webkit.PermissionRequest

interface AeroMarineCallBack {
    fun chickenHandleCreateWebWindowRequest(proBubbleBoPlingVi: AeroMarineVi)

    fun chickenOnPermissionRequest(todoSphereRequest: PermissionRequest?)

    fun chickenOnFirstPageFinished()
}