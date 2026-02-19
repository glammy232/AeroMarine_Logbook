package com.aeromarine.logbook.ror.domain.usecases

import android.util.Log
import com.aeromarine.logbook.ror.data.repo.AeroMarineRepository
import com.aeromarine.logbook.ror.data.utils.AeroMarinePushToken
import com.aeromarine.logbook.ror.data.utils.AeroMarineSystemService
import com.aeromarine.logbook.ror.domain.model.AeroMarineEntity
import com.aeromarine.logbook.ror.domain.model.AeroMarineParam
import com.aeromarine.logbook.ror.presentation.app.AeroMarineApp

class AeroMarineGetAllUseCase(
    private val aeroMarineRepository: AeroMarineRepository,
    private val volcanoSystemService: AeroMarineSystemService,
    private val aeroMarinePushToken: AeroMarinePushToken,
) {
    suspend operator fun invoke(conversion: MutableMap<String, Any>?) : AeroMarineEntity?{
        val params = AeroMarineParam(
            chickenLocale = volcanoSystemService.volcanoGetLocale(),
            chickenPushToken = aeroMarinePushToken.chickenGetToken(),
            chickenAfId = volcanoSystemService.volcanoGetAppsflyerId()
        )
        Log.d(AeroMarineApp.Companion.CHICKEN_MAIN_TAG, "Params for request: $params")
        return aeroMarineRepository.chickenGetClient(params, conversion)
    }



}