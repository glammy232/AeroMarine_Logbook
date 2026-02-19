package com.aeromarine.logbook.ror.presentation.ui.load

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.aeromarine.logbook.MainActivity
import com.aeromarine.logbook.ror.data.shar.AeroMarineSharedPreference
import com.aeromarine.logbook.databinding.FragmentLoadChickenBinding
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.aeromarine.logbook.R


class AeroMarineLoadFragment : Fragment(R.layout.fragment_load_chicken) {
    private lateinit var chickenLoadBinding: FragmentLoadChickenBinding

    private val volcanoLoadViewModel by viewModel<AeroMarineLoadViewModel>()

    private val chickenSharedPreference by inject<AeroMarineSharedPreference>()

    private var chickenUrl = ""

    private val chickenRequestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            chickenNavigateToSuccess(chickenUrl)
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                chickenSharedPreference.chickenNotificationRequest =
                    (System.currentTimeMillis() / 1000) + 259200
                chickenNavigateToSuccess(chickenUrl)
            } else {
                chickenNavigateToSuccess(chickenUrl)
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chickenLoadBinding = FragmentLoadChickenBinding.bind(view)

        chickenLoadBinding.chickenGrandButton.setOnClickListener {
            val todoSpherePermission = Manifest.permission.POST_NOTIFICATIONS
            chickenRequestNotificationPermission.launch(todoSpherePermission)
            chickenSharedPreference.chickenNotificationRequestedBefore = true
        }

        chickenLoadBinding.chickenSkipButton.setOnClickListener {
            chickenSharedPreference.chickenNotificationRequest =
                (System.currentTimeMillis() / 1000) + 259200
            chickenNavigateToSuccess(chickenUrl)
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                volcanoLoadViewModel.chickenHomeScreenState.collect {
                    when (it) {
                        is AeroMarineLoadViewModel.ChickenHomeScreenState.ChickenLoading -> {

                        }

                        is AeroMarineLoadViewModel.ChickenHomeScreenState.ChickenError -> {
                            requireActivity().startActivity(
                                Intent(
                                    requireContext(),
                                    MainActivity::class.java
                                )
                            )
                            requireActivity().finish()
                        }

                        is AeroMarineLoadViewModel.ChickenHomeScreenState.ChickenSuccess -> {
                            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
                                val chickenPermission = Manifest.permission.POST_NOTIFICATIONS
                                val chickenPermissionRequestedBefore = chickenSharedPreference.chickenNotificationRequestedBefore

                                if (ContextCompat.checkSelfPermission(requireContext(), chickenPermission) == PackageManager.PERMISSION_GRANTED) {
                                    chickenNavigateToSuccess(it.data)
                                } else if (!chickenPermissionRequestedBefore && (System.currentTimeMillis() / 1000 > chickenSharedPreference.chickenNotificationRequest)) {
                                    // первый раз — показываем UI для запроса
                                    chickenLoadBinding.chickenNotiGroup.visibility = View.VISIBLE
                                    chickenLoadBinding.chickenLoadingGroup.visibility = View.GONE
                                    chickenUrl = it.data
                                } else if (shouldShowRequestPermissionRationale(chickenPermission)) {
                                    // временный отказ — через 3 дня можно показать
                                    /*if (System.currentTimeMillis() / 1000 > chickenSharedPreference.chickenNotificationRequest) {
                                        chickenLoadBinding.chickenNotiGroup.visibility = View.VISIBLE
                                        chickenLoadBinding.chickenLoadingGroup.visibility = View.GONE
                                        chickenUrl = it.data
                                    } else {*/
                                        chickenNavigateToSuccess(it.data)
                                    //}
                                } else {
                                    // навсегда отклонено — просто пропускаем
                                    chickenNavigateToSuccess(it.data)
                                }
                            } else {
                                chickenNavigateToSuccess(it.data)
                            }
                        }

                        AeroMarineLoadViewModel.ChickenHomeScreenState.ChickenNotInternet -> {
                            chickenLoadBinding.chickenLoadConnectionStateText.visibility = View.VISIBLE
                            chickenLoadBinding.chickenLoadingGroup.visibility = View.GONE
                            chickenLoadBinding.nointernetpanel?.visibility = View.VISIBLE
                            chickenLoadBinding.nointernetBackgrounda.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }


    private fun chickenNavigateToSuccess(data: String) {
        findNavController().navigate(
            R.id.action_chickenLoadFragment_to_chickenV,
            bundleOf(CHICKEN_D to data)
        )
    }

    companion object {
        const val CHICKEN_D = "probubbleboplingData"
    }
}