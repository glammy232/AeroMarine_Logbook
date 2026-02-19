package com.aeromarine.logbook.navigation

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.abs
import kotlin.random.Random
import kotlin.collections.iterator
import com.aeromarine.logbook.R
import kotlinx.serialization.Serializable
import java.util.UUID

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavigationGraph(
    navController: NavHostController
) {
    // Состояния для хранения данных
    val flights = remember { mutableStateListOf<FlightEntry>() }
    val marineEntries = remember { mutableStateListOf<MarineEntry>() }
    val vehicles = remember { mutableStateListOf<Vehicle>() }

    // Обработка возвращаемых данных
    LaunchedEffect(Unit) {
        navController.currentBackStackEntry?.savedStateHandle?.get<FlightEntry>("new_flight")?.let { flight ->
            flights.add(0, flight)
            navController.currentBackStackEntry?.savedStateHandle?.remove<FlightEntry>("new_flight")
        }

        navController.currentBackStackEntry?.savedStateHandle?.get<MarineEntry>("new_marine")?.let { marine ->
            marineEntries.add(0, marine)
            navController.currentBackStackEntry?.savedStateHandle?.remove<MarineEntry>("new_marine")
        }
    }

    NavHost(
        navController = navController,
        startDestination = "dashboard"
    ) {
        composable("dashboard") {
            DashboardScreen(
                navController = navController,
                flights = flights,
                marineEntries = marineEntries
            )
        }

        composable("add_flight") {
            AddFlightScreen(
                navController = navController,
                onFlightAdded = { flight ->
                    flights.add(0, flight)
                    navController.popBackStack()
                }
            )
        }

        composable("add_marine") {
            AddMarineScreen(
                navController = navController,
                onMarineAdded = { marine ->
                    marineEntries.add(0, marine)
                    navController.popBackStack()
                }
            )
        }

        composable("list") {
            ListScreen(
                navController = navController,
                flights = flights,
                marineEntries = marineEntries
            )
        }

        composable("statistics") {
            StatisticsScreen(
                navController = navController,
                flights = flights,
                marineEntries = marineEntries
            )
        }

        composable("vehicles") {
            VehiclesScreen(
                navController = navController,
                vehicles = vehicles
            )
        }

        composable("map") {
            MapScreen(
                navController = navController,
                flights = flights,
                marineEntries = marineEntries
            )
        }

        composable("settings") {
            SettingsScreen(navController = navController)
        }

        composable("export") {
            ExportScreen(
                navController = navController,
                flights = flights,
                marineEntries = marineEntries
            )
        }

        composable(
            route = "flight_detail/{flightId}",
            arguments = listOf(navArgument("flightId") { type = NavType.StringType })
        ) { backStackEntry ->
            val flightId = backStackEntry.arguments?.getString("flightId") ?: ""
            FlightDetailScreen(
                navController = navController,
                flightId = flightId,
                flights = flights
            )
        }

        composable(
            route = "marine_detail/{marineId}",
            arguments = listOf(navArgument("marineId") { type = NavType.StringType })
        ) { backStackEntry ->
            val marineId = backStackEntry.arguments?.getString("marineId") ?: ""
            MarineDetailScreen(
                navController = navController,
                marineId = marineId,
                marineEntries = marineEntries
            )
        }
    }
}
@Serializable
data class FlightEntry(
    val id: String = UUID.randomUUID().toString(),
    val from: String,
    val to: String,
    val date: String,
    val timeDepart: String,
    val timeArrive: String,
    val aircraftType: String,
    val otherType: String = "",
    val registration: String,
    val airline: String,
    val flightClass: String,
    val purpose: String,
    val notes: String
)
@kotlinx.serialization.Serializable
data class MarineEntry(
    val id: String = UUID.randomUUID().toString(),
    val from: String,
    val to: String,
    val dateDepart: String,
    val dateArrive: String,
    val vesselType: String,
    val vesselName: String,
    val flag: String,
    val distance: String,
    val avgSpeed: String,
    val weather: String
)
@kotlinx.serialization.Serializable
data class Vehicle(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val type: String,
    val year: String,
    val country: String,
    val comment: String,
    val vehicleType: String
)

data class Settings(
    var distanceUnit: String = "km",
    var speedUnit: String = "km/h",
    var timeFormat: String = "24h",
    var darkTheme: Boolean = false
)
@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFlightScreen(
    navController: NavController,
    onFlightAdded: (FlightEntry) -> Unit
) {
    var departure by remember { mutableStateOf("") }
    var arrival by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var timeDepart by remember { mutableStateOf("") }
    var timeArrive by remember { mutableStateOf("") }
    var aircraftType by remember { mutableStateOf("Boeing") }
    var showOtherInput by remember { mutableStateOf(false) }
    var otherType by remember { mutableStateOf("") }
    var registration by remember { mutableStateOf("") }
    var airline by remember { mutableStateOf("") }
    var flightClass by remember { mutableStateOf("Economy") }
    var purpose by remember { mutableStateOf("Business") }
    var notes by remember { mutableStateOf("") }

    val aircraftTypes = listOf("Boeing", "Airbus", "Private", "Other")
    val flightClasses = listOf("Economy", "Business", "First")
    val purposes = listOf("Business", "Leisure", "Other")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Flight") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Text("←")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "📍 Basic Information",
                            style = MaterialTheme.typography.titleMedium
                        )

                        OutlinedTextField(
                            value = departure,
                            onValueChange = { departure = it },
                            label = { Text("From") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = arrival,
                            onValueChange = { arrival = it },
                            label = { Text("To") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = date,
                                onValueChange = { date = it },
                                label = { Text("Date") },
                                placeholder = { Text("YYYY-MM-DD") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = timeDepart,
                                onValueChange = { timeDepart = it },
                                label = { Text("Departure") },
                                placeholder = { Text("HH:MM") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        OutlinedTextField(
                            value = timeArrive,
                            onValueChange = { timeArrive = it },
                            label = { Text("Arrival Time") },
                            placeholder = { Text("HH:MM") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "✈️ Aircraft",
                            style = MaterialTheme.typography.titleMedium
                        )

                        var expanded by remember { mutableStateOf(false) }

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = it }
                        ) {
                            OutlinedTextField(
                                value = aircraftType,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Aircraft Type") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                aircraftTypes.forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type) },
                                        onClick = {
                                            aircraftType = type
                                            showOtherInput = type == "Other"
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        if (showOtherInput) {
                            OutlinedTextField(
                                value = otherType,
                                onValueChange = { otherType = it },
                                label = { Text("Specify aircraft type") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        OutlinedTextField(
                            value = registration,
                            onValueChange = { registration = it },
                            label = { Text("Registration Number") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = airline,
                            onValueChange = { airline = it },
                            label = { Text("Airline") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "📝 Additional Information",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = "Class",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            flightClasses.forEach { flightClassOption ->
                                FilterChip(
                                    selected = flightClass == flightClassOption,
                                    onClick = { flightClass = flightClassOption },
                                    label = { Text(flightClassOption) }
                                )
                            }
                        }

                        Text(
                            text = "Purpose",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            purposes.forEach { purposeOption ->
                                FilterChip(
                                    selected = purpose == purposeOption,
                                    onClick = { purpose = purposeOption },
                                    label = { Text(purposeOption) }
                                )
                            }
                        }

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Notes") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        val flight = FlightEntry(
                            from = departure,
                            to = arrival,
                            date = date,
                            timeDepart = timeDepart,
                            timeArrive = timeArrive,
                            aircraftType = aircraftType,
                            otherType = otherType,
                            registration = registration,
                            airline = airline,
                            flightClass = flightClass,
                            purpose = purpose,
                            notes = notes
                        )
                        onFlightAdded(flight)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("💾 Save Flight")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMarineScreen(
    navController: NavController,
    onMarineAdded: (MarineEntry) -> Unit
) {
    var departurePort by remember { mutableStateOf("") }
    var arrivalPort by remember { mutableStateOf("") }
    var dateDepart by remember { mutableStateOf("") }
    var dateArrive by remember { mutableStateOf("") }
    var vesselType by remember { mutableStateOf("Container") }
    var vesselName by remember { mutableStateOf("") }
    var flag by remember { mutableStateOf("") }
    var distance by remember { mutableStateOf("") }
    var avgSpeed by remember { mutableStateOf("") }
    var weather by remember { mutableStateOf("Calm") }

    val vesselTypes = listOf("Container", "Cruise", "Yacht", "Military")
    val weatherTypes = listOf("Calm", "Moderate", "Storm")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Marine Route") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Text("←")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "⚓ Basic Information",
                            style = MaterialTheme.typography.titleMedium
                        )

                        OutlinedTextField(
                            value = departurePort,
                            onValueChange = { departurePort = it },
                            label = { Text("Departure Port") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = arrivalPort,
                            onValueChange = { arrivalPort = it },
                            label = { Text("Arrival Port") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = dateDepart,
                                onValueChange = { dateDepart = it },
                                label = { Text("Departure Date") },
                                placeholder = { Text("YYYY-MM-DD") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = dateArrive,
                                onValueChange = { dateArrive = it },
                                label = { Text("Arrival Date") },
                                placeholder = { Text("YYYY-MM-DD") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "🚢 Vessel",
                            style = MaterialTheme.typography.titleMedium
                        )

                        var expanded by remember { mutableStateOf(false) }

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = it }
                        ) {
                            OutlinedTextField(
                                value = vesselType,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Vessel Type") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                vesselTypes.forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type) },
                                        onClick = {
                                            vesselType = type
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = vesselName,
                            onValueChange = { vesselName = it },
                            label = { Text("Vessel Name") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = flag,
                            onValueChange = { flag = it },
                            label = { Text("Flag Country") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "🌊 Route Parameters",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = distance,
                                onValueChange = { distance = it },
                                label = { Text("Distance (nm)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = avgSpeed,
                                onValueChange = { avgSpeed = it },
                                label = { Text("Avg Speed (knots)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Text(
                            text = "Weather Conditions",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            weatherTypes.forEach { condition ->
                                FilterChip(
                                    selected = weather == condition,
                                    onClick = { weather = condition },
                                    label = { Text(condition) }
                                )
                            }
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        val marine = MarineEntry(
                            from = departurePort,
                            to = arrivalPort,
                            dateDepart = dateDepart,
                            dateArrive = dateArrive,
                            vesselType = vesselType,
                            vesselName = vesselName,
                            flag = flag,
                            distance = distance,
                            avgSpeed = avgSpeed,
                            weather = weather
                        )
                        onMarineAdded(marine)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("💾 Save Marine Route")
                }
            }
        }
    }
}

@Composable
fun StatisticRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun VehicleCard(vehicle: Vehicle, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (vehicle.vehicleType == "Aircraft") "✈️ " else "🚢 ",
                        fontSize = MaterialTheme.typography.headlineSmall.fontSize
                    )
                    Text(
                        text = vehicle.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Row {
                    IconButton(onClick = onDelete) {
                        Text("🗑️")
                    }
                }
            }

            Divider()

            Text(
                text = "Type: ${vehicle.type}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Year: ${vehicle.year} | Country: ${vehicle.country}",
                style = MaterialTheme.typography.bodyMedium
            )
            if (vehicle.comment.isNotEmpty()) {
                Text(
                    text = "Note: ${vehicle.comment}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AddVehicleDialog(onDismiss: () -> Unit, onAdd: (Vehicle) -> Unit) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    var vehicleType by remember { mutableStateOf("Aircraft") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Vehicle") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Select Type:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = vehicleType == "Aircraft",
                        onClick = { vehicleType = "Aircraft" },
                        label = { Text("✈️ Aircraft") }
                    )
                    FilterChip(
                        selected = vehicleType == "Vessel",
                        onClick = { vehicleType = "Vessel" },
                        label = { Text("🚢 Vessel") }
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = type,
                    onValueChange = { type = it },
                    label = { Text("Type / Model") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = year,
                    onValueChange = { year = it },
                    label = { Text("Year") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = country,
                    onValueChange = { country = it },
                    label = { Text("Country of Registration") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Comment (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && type.isNotBlank()) {
                        onAdd(
                            Vehicle(
                                name = name,
                                type = type,
                                year = year,
                                country = country,
                                comment = comment,
                                vehicleType = vehicleType
                            )
                        )
                    }
                },
                enabled = name.isNotBlank() && type.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DetailCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Divider()
            content()
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

fun calculateFlightDuration(flight: FlightEntry): String {
    return "2h 15m"
}

fun calculateMarineDuration(marine: MarineEntry): String {
    return "3d 4h"
}

fun calculateTotalTime(flights: List<FlightEntry>, marineEntries: List<MarineEntry>): String {
    return "156h"
}

///

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    flights: List<FlightEntry>,
    marineEntries: List<MarineEntry>
) {
    var selectedPeriod by remember { mutableStateOf("Week") }
    var selectedType by remember { mutableStateOf("All") }
    val periods = listOf("Today", "Week", "Month", "All")

    val allEntries = remember(flights, marineEntries) {
        val entries = mutableListOf<Any>()
        entries.addAll(flights.take(3))
        entries.addAll(marineEntries.take(3))
        entries.take(3)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LogBook Dashboard") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Text("⚙️")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { navController.navigate("add_flight") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("➕ Add Flight")
                    }
                    Button(
                        onClick = { navController.navigate("add_marine") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("➕ Add Marine")
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Statistics",
                            style = MaterialTheme.typography.titleLarge
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem("Flights", flights.size.toString())
                            StatItem("Marine", marineEntries.size.toString())
                            StatItem("Total Time", calculateTotalTime(flights, marineEntries))
                        }

                        Divider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            var expanded by remember { mutableStateOf(false) }

                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = it }
                            ) {
                                OutlinedTextField(
                                    value = selectedPeriod,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Period") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .menuAnchor()
                                )

                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    periods.forEach { period ->
                                        DropdownMenuItem(
                                            text = { Text(period) },
                                            onClick = {
                                                selectedPeriod = period
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            FilterChip(
                                selected = selectedType == "✈️",
                                onClick = { selectedType = "✈️" },
                                label = { Text("✈️") }
                            )
                            FilterChip(
                                selected = selectedType == "🚢",
                                onClick = { selectedType = "🚢" },
                                label = { Text("🚢") }
                            )
                            FilterChip(
                                selected = selectedType == "All",
                                onClick = { selectedType = "All" },
                                label = { Text("All") }
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Recent Entries",
                        style = MaterialTheme.typography.titleLarge
                    )
                    TextButton(onClick = { navController.navigate("list") }) {
                        Text("View All →")
                    }
                }
            }

            items(allEntries.size) { index ->
                val entry = allEntries[index]
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        when(entry) {
                            is FlightEntry -> navController.navigate("flight_detail/${entry.id}")
                            is MarineEntry -> navController.navigate("marine_detail/${entry.id}")
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = when(entry) {
                                    is FlightEntry -> "✈️ ${entry.from} → ${entry.to}"
                                    is MarineEntry -> "🚢 ${entry.from} → ${entry.to}"
                                    else -> ""
                                },
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = when(entry) {
                                    is FlightEntry -> entry.date
                                    is MarineEntry -> entry.dateDepart
                                    else -> ""
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = when(entry) {
                                is FlightEntry -> calculateFlightDuration(entry)
                                is MarineEntry -> calculateMarineDuration(entry)
                                else -> ""
                            },
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { navController.navigate("vehicles") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("🛠️ My Vehicles")
                    }
                    OutlinedButton(
                        onClick = { navController.navigate("statistics") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("📊 Statistics")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    navController: NavController,
    flights: List<FlightEntry>,
    marineEntries: List<MarineEntry>
) {
    var filterDate by remember { mutableStateOf("") }
    var filterType by remember { mutableStateOf("All") }
    var filterCountry by remember { mutableStateOf("") }
    var filterVehicle by remember { mutableStateOf("") }

    val allEntries = remember(flights, marineEntries, filterType) {
        val entries = mutableListOf<Any>()
        when(filterType) {
            "Flight" -> entries.addAll(flights)
            "Marine" -> entries.addAll(marineEntries)
            else -> {
                entries.addAll(flights)
                entries.addAll(marineEntries)
            }
        }
        entries.sortedByDescending {
            when(it) {
                is FlightEntry -> it.date
                is MarineEntry -> it.dateDepart
                else -> ""
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Entries List") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Text("←")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Filters",
                            style = MaterialTheme.typography.titleMedium
                        )

                        OutlinedTextField(
                            value = filterDate,
                            onValueChange = { filterDate = it },
                            label = { Text("Filter by Date") },
                            placeholder = { Text("YYYY-MM-DD") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = filterType == "All",
                                onClick = { filterType = "All" },
                                label = { Text("All") }
                            )
                            FilterChip(
                                selected = filterType == "Flight",
                                onClick = { filterType = "Flight" },
                                label = { Text("✈️ Flights") }
                            )
                            FilterChip(
                                selected = filterType == "Marine",
                                onClick = { filterType = "Marine" },
                                label = { Text("🚢 Marine") }
                            )
                        }

                        OutlinedTextField(
                            value = filterCountry,
                            onValueChange = { filterCountry = it },
                            label = { Text("Filter by Country") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = filterVehicle,
                            onValueChange = { filterVehicle = it },
                            label = { Text("Filter by Vehicle") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                filterDate = ""
                                filterType = "All"
                                filterCountry = ""
                                filterVehicle = ""
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Clear Filters")
                        }
                    }
                }
            }

            items(allEntries.size) { index ->
                val entry = allEntries[index]
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        when(entry) {
                            is FlightEntry -> navController.navigate("flight_detail/${entry.id}")
                            is MarineEntry -> navController.navigate("marine_detail/${entry.id}")
                        }
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = when(entry) {
                                    is FlightEntry -> "✈️ ${entry.from} → ${entry.to}"
                                    is MarineEntry -> "🚢 ${entry.from} → ${entry.to}"
                                    else -> ""
                                },
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = when(entry) {
                                    is FlightEntry -> calculateFlightDuration(entry)
                                    is MarineEntry -> calculateMarineDuration(entry)
                                    else -> ""
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Text(
                            text = when(entry) {
                                is FlightEntry -> entry.date
                                is MarineEntry -> entry.dateDepart
                                else -> ""
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    when(entry) {
                                        is FlightEntry -> navController.navigate("flight_detail/${entry.id}")
                                        is MarineEntry -> navController.navigate("marine_detail/${entry.id}")
                                    }
                                }
                            ) {
                                Text("Details →")
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    navController: NavController,
    flights: List<FlightEntry>,
    marineEntries: List<MarineEntry>
) {
    var selectedPeriod by remember { mutableStateOf("Month") }
    val periods = listOf("Week", "Month", "Year", "All")

    val mostFrequentRoute = remember(flights, marineEntries) {
        val routeCounts = mutableMapOf<String, Int>()
        flights.forEach {
            val route = "${it.from} → ${it.to}"
            routeCounts[route] = (routeCounts[route] ?: 0) + 1
        }
        marineEntries.forEach {
            val route = "${it.from} → ${it.to}"
            routeCounts[route] = (routeCounts[route] ?: 0) + 1
        }
        routeCounts.maxByOrNull { it.value }?.key ?: "No routes yet"
    }

    val mostUsedVehicle = remember(flights, marineEntries) {
        val vehicleCounts = mutableMapOf<String, Int>()
        flights.forEach {
            val vehicle = it.aircraftType
            vehicleCounts[vehicle] = (vehicleCounts[vehicle] ?: 0) + 1
        }
        marineEntries.forEach {
            val vehicle = it.vesselType
            vehicleCounts[vehicle] = (vehicleCounts[vehicle] ?: 0) + 1
        }
        vehicleCounts.maxByOrNull { it.value }?.key ?: "No vehicles yet"
    }

    val avgFlightTime = remember(flights) {
        if (flights.isEmpty()) "0h" else {
            val totalMinutes = flights.sumOf {
                val parts = calculateFlightDuration(it).replace("h", "").split("m")
                val hours = parts[0].toIntOrNull() ?: 0
                val minutes = parts.getOrNull(1)?.replace("m", "")?.toIntOrNull() ?: 0
                hours * 60 + minutes
            }
            val avgMinutes = totalMinutes / flights.size
            "${avgMinutes / 60}h ${avgMinutes % 60}m"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistics") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Text("←")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "📊 Analytics Overview",
                            style = MaterialTheme.typography.headlineSmall
                        )

                        var expanded by remember { mutableStateOf(false) }

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedPeriod,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Period") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                periods.forEach { period ->
                                    DropdownMenuItem(
                                        text = { Text(period) },
                                        onClick = {
                                            selectedPeriod = period
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatisticRow("Total Flights", flights.size.toString())
                        StatisticRow("Total Marine Routes", marineEntries.size.toString())
                        Divider()
                        StatisticRow("Most Frequent Route", mostFrequentRoute)
                        StatisticRow("Most Used Vehicle", mostUsedVehicle)
                        StatisticRow("Average Flight Time", avgFlightTime)
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Monthly Activity",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("✈️", fontSize = MaterialTheme.typography.headlineLarge.fontSize)
                                Text(flights.size.toString())
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🚢", fontSize = MaterialTheme.typography.headlineLarge.fontSize)
                                Text(marineEntries.size.toString())
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("⏱️", fontSize = MaterialTheme.typography.headlineLarge.fontSize)
                                Text("${flights.size + marineEntries.size}")
                            }
                        }
                    }
                }
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatisticRow("Total Flights", flights.size.toString())
                        StatisticRow("Total Marine Routes", marineEntries.size.toString())
                        Divider()
                        StatisticRow("Most Frequent Route", mostFrequentRoute)
                        StatisticRow("Most Used Vehicle", mostUsedVehicle)
                        StatisticRow("Average Flight Time", avgFlightTime)
                    }
                }
            }
            /*val privacyPolicyUrl = "https://colddepth.com/privacy-policy.html"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl))
                        context.startActivity(intent)*/
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehiclesScreen(
    navController: NavController,
    vehicles: MutableList<Vehicle>
) {
    var selectedTab by remember { mutableStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }

    // Используем snapshotFlow для отслеживания изменений в списке
    val aircrafts by remember(vehicles) {
        derivedStateOf { vehicles.filter { it.vehicleType == "Aircraft" } }
    }
    val vessels by remember(vehicles) {
        derivedStateOf { vehicles.filter { it.vehicleType == "Vessel" } }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Vehicles") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Text("←")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Text("➕")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("✈️ Aircraft (${aircrafts.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("🚢 Vessels (${vessels.size})") }
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (selectedTab == 0) {
                    if (aircrafts.isEmpty()) {
                        item {
                            EmptyStateCard(
                                text = "No aircraft added yet",
                                onAddClick = { showAddDialog = true }
                            )
                        }
                    } else {
                        items(
                            items = aircrafts,
                            key = { it.id }
                        ) { aircraft ->
                            VehicleCard(
                                vehicle = aircraft,
                                onEdit = {
                                    // Handle edit
                                },
                                onDelete = {
                                    // Просто удаляем из исходного списка
                                    vehicles.remove(aircraft)
                                }
                            )
                        }
                    }
                } else {
                    if (vessels.isEmpty()) {
                        item {
                            EmptyStateCard(
                                text = "No vessels added yet",
                                onAddClick = { showAddDialog = true }
                            )
                        }
                    } else {
                        items(
                            items = vessels,
                            key = { it.id }
                        ) { vessel ->
                            VehicleCard(
                                vehicle = vessel,
                                onEdit = {
                                    // Handle edit
                                },
                                onDelete = {
                                    vehicles.remove(vessel)
                                }
                            )
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            AddVehicleDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { vehicle ->
                    // Добавляем в исходный список - это автоматически обновит UI
                    vehicles.add(vehicle)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun EmptyStateCard(text: String, onAddClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onAddClick
            ) {
                Text("➕ Add New")
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    navController: NavController,
    flights: List<FlightEntry>,
    marineEntries: List<MarineEntry>
) {
    var filterType by remember { mutableStateOf("All") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Route Map") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Text("←")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Filter Routes",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = filterType == "All",
                            onClick = { filterType = "All" },
                            label = { Text("All") }
                        )
                        FilterChip(
                            selected = filterType == "Flight",
                            onClick = { filterType = "Flight" },
                            label = { Text("✈️ Flights") }
                        )
                        FilterChip(
                            selected = filterType == "Marine",
                            onClick = { filterType = "Marine" },
                            label = { Text("🚢 Marine") }
                        )
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "🗺️",
                            fontSize = MaterialTheme.typography.displayLarge.fontSize
                        )
                        Text(
                            text = "Map View",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = "Route visualization would appear here",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Routes:",
                            style = MaterialTheme.typography.titleMedium
                        )

                        if (filterType != "Marine") {
                            flights.take(3).forEach { flight ->
                                Text("✈️ ${flight.from} → ${flight.to}")
                            }
                        }
                        if (filterType != "Flight") {
                            marineEntries.take(3).forEach { marine ->
                                Text("🚢 ${marine.from} → ${marine.to}")
                            }
                        }
                        if (flights.isEmpty() && marineEntries.isEmpty()) {
                            Text("No routes to display")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    navController: NavController,
    flights: List<FlightEntry>,
    marineEntries: List<MarineEntry>
) {
    var selectedPeriod by remember { mutableStateOf("Month") }
    var selectedType by remember { mutableStateOf("All") }
    var selectedFormat by remember { mutableStateOf("PDF") }

    val periods = listOf("Week", "Month", "Year", "All")
    val types = listOf("All", "✈️ Flights", "🚢 Marine")
    val formats = listOf("PDF", "CSV")

    val estimatedEntries = remember(selectedType, flights, marineEntries) {
        when(selectedType) {
            "✈️ Flights" -> flights.size
            "🚢 Marine" -> marineEntries.size
            else -> flights.size + marineEntries.size
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Export Data") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Text("←")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Export Options",
                            style = MaterialTheme.typography.titleLarge
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Period",
                                style = MaterialTheme.typography.titleMedium
                            )

                            var periodExpanded by remember { mutableStateOf(false) }

                            ExposedDropdownMenuBox(
                                expanded = periodExpanded,
                                onExpandedChange = { periodExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = selectedPeriod,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Select Period") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = periodExpanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                )

                                ExposedDropdownMenu(
                                    expanded = periodExpanded,
                                    onDismissRequest = { periodExpanded = false }
                                ) {
                                    periods.forEach { period ->
                                        DropdownMenuItem(
                                            text = { Text(period) },
                                            onClick = {
                                                selectedPeriod = period
                                                periodExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Type",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                types.forEach { type ->
                                    FilterChip(
                                        selected = selectedType == type,
                                        onClick = { selectedType = type },
                                        label = { Text(type) }
                                    )
                                }
                            }
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Format",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                formats.forEach { format ->
                                    FilterChip(
                                        selected = selectedFormat == format,
                                        onClick = { selectedFormat = format },
                                        label = { Text(format) }
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = { /* Generate report */ },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("📄 Create Report")
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Export Summary",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Period: $selectedPeriod",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Type: $selectedType",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Format: $selectedFormat",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Entries to export: $estimatedEntries",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightDetailScreen(
    navController: NavController,
    flightId: String,
    flights: List<FlightEntry>
) {
    val flight = flights.find { it.id == flightId } ?: FlightEntry(
        id = flightId,
        from = "AMS",
        to = "BCN",
        date = "2024-06-12",
        timeDepart = "10:30",
        timeArrive = "12:45",
        aircraftType = "Airbus",
        registration = "PH-BXA",
        airline = "KLM",
        flightClass = "Business",
        purpose = "Business",
        notes = "Smooth flight, good service"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Flight Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Text("←")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "✈️ ${flight.from} → ${flight.to}",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            text = calculateFlightDuration(flight),
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            item {
                DetailCard(
                    title = "📍 Route Information",
                    content = {
                        DetailRow("From", flight.from)
                        DetailRow("To", flight.to)
                        DetailRow("Date", flight.date)
                        DetailRow("Departure", flight.timeDepart)
                        DetailRow("Arrival", flight.timeArrive)
                    }
                )
            }

            item {
                DetailCard(
                    title = "✈️ Aircraft Information",
                    content = {
                        DetailRow("Aircraft Type", flight.aircraftType)
                        DetailRow("Registration", flight.registration)
                        DetailRow("Airline", flight.airline)
                    }
                )
            }

            item {
                DetailCard(
                    title = "📝 Additional Information",
                    content = {
                        DetailRow("Class", flight.flightClass)
                        DetailRow("Purpose", flight.purpose)
                        if (flight.notes.isNotEmpty()) {
                            DetailRow("Notes", flight.notes)
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarineDetailScreen(
    navController: NavController,
    marineId: String,
    marineEntries: List<MarineEntry>
) {
    val marine = marineEntries.find { it.id == marineId } ?: MarineEntry(
        id = marineId,
        from = "Rotterdam",
        to = "Oslo",
        dateDepart = "2024-06-10",
        dateArrive = "2024-06-13",
        vesselType = "Container",
        vesselName = "Maersk Line",
        flag = "Denmark",
        distance = "650",
        avgSpeed = "18",
        weather = "Calm"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Marine Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Text("←")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🚢 ${marine.from} → ${marine.to}",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            text = calculateMarineDuration(marine),
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            item {
                DetailCard(
                    title = "⚓ Route Information",
                    content = {
                        DetailRow("From", marine.from)
                        DetailRow("To", marine.to)
                        DetailRow("Departure Date", marine.dateDepart)
                        DetailRow("Arrival Date", marine.dateArrive)
                    }
                )
            }

            item {
                DetailCard(
                    title = "🚢 Vessel Information",
                    content = {
                        DetailRow("Vessel Type", marine.vesselType)
                        DetailRow("Vessel Name", marine.vesselName)
                        DetailRow("Flag", marine.flag)
                    }
                )
            }

            item {
                DetailCard(
                    title = "🌊 Route Parameters",
                    content = {
                        DetailRow("Distance", "${marine.distance} nm")
                        DetailRow("Avg Speed", "${marine.avgSpeed} knots")
                        DetailRow("Weather", marine.weather)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val settings = remember { mutableStateOf(Settings()) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Text("←")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Units",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = settings.value.distanceUnit == "km",
                                onClick = { settings.value = settings.value.copy(distanceUnit = "km") },
                                label = { Text("km") }
                            )
                            FilterChip(
                                selected = settings.value.distanceUnit == "miles",
                                onClick = { settings.value = settings.value.copy(distanceUnit = "miles") },
                                label = { Text("miles") }
                            )
                            FilterChip(
                                selected = settings.value.distanceUnit == "nm",
                                onClick = { settings.value = settings.value.copy(distanceUnit = "nm") },
                                label = { Text("nautical miles") }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = settings.value.speedUnit == "km/h",
                                onClick = { settings.value = settings.value.copy(speedUnit = "km/h") },
                                label = { Text("km/h") }
                            )
                            FilterChip(
                                selected = settings.value.speedUnit == "knots",
                                onClick = { settings.value = settings.value.copy(speedUnit = "knots") },
                                label = { Text("knots") }
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Time Format",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = settings.value.timeFormat == "24h",
                                onClick = { settings.value = settings.value.copy(timeFormat = "24h") },
                                label = { Text("24h") }
                            )
                            FilterChip(
                                selected = settings.value.timeFormat == "12h",
                                onClick = { settings.value = settings.value.copy(timeFormat = "12h") },
                                label = { Text("12h") }
                            )
                        }

                        Divider()
                    }
                }
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val context = LocalContext.current

                        Button(
                            onClick = {
                                val privacyPolicyUrl = "https://aeromarinelogbook.com/privacy-policy.html"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth(), // Кнопка на всю ширину
                            shape = RoundedCornerShape(8.dp) // Скругленные углы
                        ) {
                            Text(
                                text = "Privacy Policy",
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete All Data") },
                text = { Text("Are you sure you want to delete all your flight and marine records? This action cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            // Clear all data here
                            showDeleteDialog = false
                        }
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}