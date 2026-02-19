package com.aeromarine.logbook.ror.presentation.ui.view

import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.aeromarine.logbook.ror.presentation.app.AeroMarineApp
import com.aeromarine.logbook.ror.presentation.ui.load.AeroMarineLoadFragment
import org.koin.android.ext.android.inject

class AeroMarineV : Fragment(){

    private lateinit var chickenPhoto: Uri
    private var chickenFilePathFromChrome: ValueCallback<Array<Uri>>? = null

    private val chickenTakeFile: ActivityResultLauncher<PickVisualMediaRequest> = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
        chickenFilePathFromChrome?.onReceiveValue(arrayOf(it ?: Uri.EMPTY))
        chickenFilePathFromChrome = null
    }

    private val chickenTakePhoto: ActivityResultLauncher<Uri> = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        if (it) {
            chickenFilePathFromChrome?.onReceiveValue(arrayOf(chickenPhoto))
            chickenFilePathFromChrome = null
        } else {
            chickenFilePathFromChrome?.onReceiveValue(null)
            chickenFilePathFromChrome = null
        }
    }

    private val volcanoDataStore by activityViewModels<AeroMarineDataStore>()


    private val chickenViFun by inject<AeroMarineViFun>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(AeroMarineApp.Companion.CHICKEN_MAIN_TAG, "Fragment onCreate")
        CookieManager.getInstance().setAcceptCookie(true)
        requireActivity().onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (volcanoDataStore.proBubbleBoPlingView.canGoBack()) {
                        volcanoDataStore.proBubbleBoPlingView.goBack()
                        Log.d(AeroMarineApp.Companion.CHICKEN_MAIN_TAG, "WebView can go back")
                    } else if (volcanoDataStore.proBubbleBoPlingViList.size > 1) {
                        Log.d(AeroMarineApp.Companion.CHICKEN_MAIN_TAG, "WebView can`t go back")
                        volcanoDataStore.proBubbleBoPlingViList.removeAt(volcanoDataStore.proBubbleBoPlingViList.lastIndex)
                        Log.d(AeroMarineApp.Companion.CHICKEN_MAIN_TAG, "WebView list size ${volcanoDataStore.proBubbleBoPlingViList.size}")
                        volcanoDataStore.proBubbleBoPlingView.destroy()
                        val previousWebView = volcanoDataStore.proBubbleBoPlingViList.last()
                        attachWebViewToContainer(previousWebView)
                        volcanoDataStore.proBubbleBoPlingView = previousWebView
                    }
                }

            })
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (volcanoDataStore.chickenIsFirstCreate) {
            volcanoDataStore.chickenIsFirstCreate = false
            volcanoDataStore.chickenContainerView = FrameLayout(requireContext()).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                id = View.generateViewId()
            }
            return volcanoDataStore.chickenContainerView
        } else {
            return volcanoDataStore.chickenContainerView
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(AeroMarineApp.Companion.CHICKEN_MAIN_TAG, "onViewCreated")
        if (volcanoDataStore.proBubbleBoPlingViList.isEmpty()) {
            volcanoDataStore.proBubbleBoPlingView = AeroMarineVi(requireContext(), object :
                AeroMarineCallBack {
                override fun chickenHandleCreateWebWindowRequest(proBubbleBoPlingVi: AeroMarineVi) {
                    volcanoDataStore.proBubbleBoPlingViList.add(proBubbleBoPlingVi)
                    Log.d(AeroMarineApp.Companion.CHICKEN_MAIN_TAG, "WebView list size = ${volcanoDataStore.proBubbleBoPlingViList.size}")
                    Log.d(AeroMarineApp.Companion.CHICKEN_MAIN_TAG, "CreateWebWindowRequest")
                    volcanoDataStore.proBubbleBoPlingView = proBubbleBoPlingVi
                    proBubbleBoPlingVi.fishingplannerSetFileChooserHandler { callback ->
                        handleFileChooser(callback)
                    }
                    attachWebViewToContainer(proBubbleBoPlingVi)
                }

                override fun chickenOnPermissionRequest(chickenRequest: PermissionRequest?) {
                    chickenRequest?.grant(chickenRequest.resources)
                }

                override fun chickenOnFirstPageFinished() {
                    volcanoDataStore.chickenSetIsFirstFinishPage()
                }

            }, chickenWindow = requireActivity().window).apply {
                fishingplannerSetFileChooserHandler { callback ->
                    handleFileChooser(callback)
                }
            }
            volcanoDataStore.proBubbleBoPlingView.fishingplannerFLoad(arguments?.getString(
                AeroMarineLoadFragment.Companion.CHICKEN_D) ?: "")
//            ejvview.fLoad("www.google.com")
            volcanoDataStore.proBubbleBoPlingViList.add(volcanoDataStore.proBubbleBoPlingView)
            attachWebViewToContainer(volcanoDataStore.proBubbleBoPlingView)
        } else {
            volcanoDataStore.proBubbleBoPlingViList.forEach { webView ->
                webView.fishingplannerSetFileChooserHandler { callback ->
                    handleFileChooser(callback)
                }
            }
            volcanoDataStore.proBubbleBoPlingView = volcanoDataStore.proBubbleBoPlingViList.last()

            attachWebViewToContainer(volcanoDataStore.proBubbleBoPlingView)
        }
        Log.d(AeroMarineApp.Companion.CHICKEN_MAIN_TAG, "WebView list size = ${volcanoDataStore.proBubbleBoPlingViList.size}")
    }

    private fun handleFileChooser(callback: ValueCallback<Array<Uri>>?) {
        Log.d(AeroMarineApp.Companion.CHICKEN_MAIN_TAG, "handleFileChooser called, callback: ${callback != null}")

        chickenFilePathFromChrome = callback

        val listItems: Array<out String> = arrayOf("Select from file", "To make a photo")
        val listener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                0 -> {
                    Log.d(AeroMarineApp.Companion.CHICKEN_MAIN_TAG, "Launching file picker")
                    chickenTakeFile.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
                1 -> {
                    Log.d(AeroMarineApp.Companion.CHICKEN_MAIN_TAG, "Launching camera")
                    chickenPhoto = chickenViFun.chickenSavePhoto()
                    chickenTakePhoto.launch(chickenPhoto)
                }
            }
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Choose a method")
            .setItems(listItems, listener)
            .setCancelable(true)
            .setOnCancelListener {
                Log.d(AeroMarineApp.Companion.CHICKEN_MAIN_TAG, "File chooser canceled")
                callback?.onReceiveValue(null)
                chickenFilePathFromChrome = null
            }
            .create()
            .show()
    }

    private fun attachWebViewToContainer(w: AeroMarineVi) {
        volcanoDataStore.chickenContainerView.post {
            // Убираем предыдущую WebView, если есть
            (w.parent as? ViewGroup)?.removeView(w)
            volcanoDataStore.chickenContainerView.removeAllViews()
            volcanoDataStore.chickenContainerView.addView(w)
        }
    }


}