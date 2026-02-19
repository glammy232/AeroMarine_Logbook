package com.aeromarine.logbook.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChangeCircle
import androidx.compose.material.icons.filled.GolfCourse
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.aeromarine.logbook.viewmodel.TaskViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CosmicBottomNavigation(navController: NavHostController, viewModel: TaskViewModel) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    val items = listOf(
        NavItem("Home", Icons.Default.Home, "main"),
        NavItem("Onboarding", Icons.Default.ChangeCircle, "onboarding"),
        NavItem("Add Location", Icons.Default.GolfCourse, "addLocation"),
        NavItem("Fishes", Icons.Default.Settings, "fishTypes")
    )
}

data class NavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)