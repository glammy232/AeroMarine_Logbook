package com.aeromarine.logbook.ror.presentation.ui.load

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aeromarine.logbook.ror.data.shar.AeroMarineSharedPreference
import com.aeromarine.logbook.ror.data.utils.AeroMarineSystemService
import com.aeromarine.logbook.ror.domain.usecases.AeroMarineGetAllUseCase
import com.aeromarine.logbook.ror.presentation.app.AeroMarineApp
import com.aeromarine.logbook.ror.presentation.app.AeroMarineAppsFlyerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AeroMarineLoadViewModel(
    private val aeroMarineGetAllUseCase: AeroMarineGetAllUseCase,
    private val chickenSharedPreference: AeroMarineSharedPreference,
    private val volcanoSystemService: AeroMarineSystemService
) : ViewModel() {

    private val _chickenHomeScreenState: MutableStateFlow<ChickenHomeScreenState> =
        MutableStateFlow(ChickenHomeScreenState.ChickenLoading)
    val chickenHomeScreenState = _chickenHomeScreenState.asStateFlow()

    private var chickenGetApps = false


    init {
        viewModelScope.launch {
            when (chickenSharedPreference.chickenAppState) {
                0 -> {
                    if (volcanoSystemService.volcanoIsOnline()) {
                        AeroMarineApp.Companion.chickenConversionFlow.collect {
                            when(it) {
                                AeroMarineAppsFlyerState.AeroMarineDefault -> {}
                                AeroMarineAppsFlyerState.AeroMarineError -> {
                                    chickenSharedPreference.chickenAppState = 2
                                    _chickenHomeScreenState.value =
                                        ChickenHomeScreenState.ChickenError
                                    chickenGetApps = true
                                }
                                is AeroMarineAppsFlyerState.AeroMarineSuccess -> {
                                    if (!chickenGetApps) {
                                        chickenGetData(it.chickenData)
                                        chickenGetApps = true
                                    }
                                }
                            }
                        }
                    } else {
                        _chickenHomeScreenState.value =
                            ChickenHomeScreenState.ChickenNotInternet
                    }
                }
                1 -> {
                    if (volcanoSystemService.volcanoIsOnline()) {
                        if (AeroMarineApp.Companion.CHICKEN_FB_LI != null) {
                            _chickenHomeScreenState.value =
                                ChickenHomeScreenState.ChickenSuccess(
                                    AeroMarineApp.Companion.CHICKEN_FB_LI.toString()
                                )
                        } else if (System.currentTimeMillis() / 1000 > chickenSharedPreference.chickenExpired) {
                            Log.d(AeroMarineApp.Companion.CHICKEN_MAIN_TAG, "Current time more then expired, repeat request")
                            AeroMarineApp.Companion.chickenConversionFlow.collect {
                                when(it) {
                                    AeroMarineAppsFlyerState.AeroMarineDefault -> {}
                                    AeroMarineAppsFlyerState.AeroMarineError -> {
                                        _chickenHomeScreenState.value =
                                            ChickenHomeScreenState.ChickenSuccess(
                                                chickenSharedPreference.chickenSavedUrl
                                            )
                                        chickenGetApps = true
                                    }
                                    is AeroMarineAppsFlyerState.AeroMarineSuccess -> {
                                        if (!chickenGetApps) {
                                            chickenGetData(it.chickenData)
                                            chickenGetApps = true
                                        }
                                    }
                                }
                            }
                        } else {
                            Log.d(AeroMarineApp.Companion.CHICKEN_MAIN_TAG, "Current time less then expired, use saved url")
                            _chickenHomeScreenState.value =
                                ChickenHomeScreenState.ChickenSuccess(
                                    chickenSharedPreference.chickenSavedUrl
                                )
                        }
                    } else {
                        _chickenHomeScreenState.value =
                            ChickenHomeScreenState.ChickenNotInternet
                    }
                }
                2 -> {
                    _chickenHomeScreenState.value =
                        ChickenHomeScreenState.ChickenError
                }
            }
        }
    }


    private suspend fun chickenGetData(conversation: MutableMap<String, Any>?) {
        val chickenData = aeroMarineGetAllUseCase.invoke(conversation)
        if (chickenSharedPreference.chickenAppState == 0) {
            if (chickenData == null) {
                chickenSharedPreference.chickenAppState = 2
                _chickenHomeScreenState.value =
                    ChickenHomeScreenState.ChickenError
            } else {
                chickenSharedPreference.chickenAppState = 1
                chickenSharedPreference.apply {
                    chickenExpired = chickenData.chickenExpires
                    chickenSavedUrl = chickenData.chickenUrl
                }
                _chickenHomeScreenState.value =
                    ChickenHomeScreenState.ChickenSuccess(chickenData.chickenUrl)
            }
        } else  {
            if (chickenData == null) {
                _chickenHomeScreenState.value =
                    ChickenHomeScreenState.ChickenSuccess(chickenSharedPreference.chickenSavedUrl)
            } else {
                chickenSharedPreference.apply {
                    chickenExpired = chickenData.chickenExpires
                    chickenSavedUrl = chickenData.chickenUrl
                }
                _chickenHomeScreenState.value =
                    ChickenHomeScreenState.ChickenSuccess(chickenData.chickenUrl)
            }
        }
    }


    sealed class ChickenHomeScreenState {
        data object ChickenLoading : ChickenHomeScreenState()
        data object ChickenError : ChickenHomeScreenState()
        data class ChickenSuccess(val data: String) : ChickenHomeScreenState()
        data object ChickenNotInternet: ChickenHomeScreenState()
    }
}