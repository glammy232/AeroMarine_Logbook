package com.aeromarine.logbook.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.aeromarine.logbook.navigation.NavigationGraph
import com.aeromarine.logbook.ui.components.CosmicBottomNavigation
import com.aeromarine.logbook.ui.theme.BubbleOrbitTheme
import com.aeromarine.logbook.viewmodel.TaskViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FishLocationLogApp(viewModel: TaskViewModel) {
    val navController = rememberNavController()
    BubbleOrbitTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            NavigationGraph(
                navController = navController
            )
            CosmicBottomNavigation(
                navController = navController,
                viewModel
            )
        }
    }
}