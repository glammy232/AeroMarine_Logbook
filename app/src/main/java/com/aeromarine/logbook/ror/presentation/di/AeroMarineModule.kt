package com.aeromarine.logbook.ror.presentation.di

import com.aeromarine.logbook.ror.data.repo.AeroMarineRepository
import com.aeromarine.logbook.ror.data.shar.AeroMarineSharedPreference
import com.aeromarine.logbook.ror.data.utils.AeroMarinePushToken
import com.aeromarine.logbook.ror.data.utils.AeroMarineSystemService
import com.aeromarine.logbook.ror.domain.usecases.AeroMarineGetAllUseCase
import com.aeromarine.logbook.ror.presentation.pushhandler.AeroMarinePushHandler
import com.aeromarine.logbook.ror.presentation.ui.load.AeroMarineLoadViewModel
import com.aeromarine.logbook.ror.presentation.ui.view.AeroMarineViFun
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val volcanoModule = module {
    factory {
        AeroMarinePushHandler()
    }
    single {
        AeroMarineRepository()
    }
    single {
        AeroMarineSharedPreference(get())
    }
    factory {
        AeroMarinePushToken()
    }
    factory {
        AeroMarineSystemService(get())
    }
    factory {
        AeroMarineGetAllUseCase(
            get(), get(), get()
        )
    }
    factory {
        AeroMarineViFun(get())
    }
    viewModel {
        AeroMarineLoadViewModel(
            get(),
            get(),
            get()
        )
    }
}