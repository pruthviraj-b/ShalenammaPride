package com.pruthviraj.shalenammapride

import android.os.Bundle
import android.os.Build
import android.Manifest
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.messaging.FirebaseMessaging
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pruthviraj.shalenammapride.screens.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ChildEventListener
import com.pruthviraj.shalenammapride.ui.theme.ShalenammaPrideTheme
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment
import androidx.compose.animation.*

data class NotificationEvent(val icon: String, val message: String)

object AppNotificationManager {
    val events = MutableSharedFlow<NotificationEvent>(extraBufferCapacity = 1)
    fun trigger(icon: String, message: String) {
        events.tryEmit(NotificationEvent(icon, message))
    }
}

sealed class BottomNavItem(val route: String, val label: String, val icon: ImageVector) {
    object Dashboard : BottomNavItem("dashboard", "Home",      Icons.Filled.Home)
    object Meal      : BottomNavItem("meal",      "Meals",     Icons.Filled.Restaurant)
    object Facility  : BottomNavItem("facility",  "Facility",  Icons.Filled.Place)
    object Stars     : BottomNavItem("stars",     "Stars",     Icons.Filled.Star)
    object Feedback  : BottomNavItem("feedback",  "Feedback",  Icons.Filled.Email)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Start Foreground Service for background database listening
        val startServiceIntent = android.content.Intent(this, DatabaseListenerService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val requestPermissionLauncher = registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(startServiceIntent)
                    } else {
                        startService(startServiceIntent)
                    }
                }
            }
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(startServiceIntent)
            } else {
                startService(startServiceIntent)
            }
        }

        setContent {
            ShalenammaPrideTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    
    val items = listOf(
        BottomNavItem.Dashboard,
        BottomNavItem.Meal,
        BottomNavItem.Facility,
        BottomNavItem.Stars,
        BottomNavItem.Feedback
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var currentNotification by remember { mutableStateOf<NotificationEvent?>(null) }

    LaunchedEffect(Unit) {
        AppNotificationManager.events.collect { event ->
            currentNotification = event
            delay(3000)
            currentNotification = null
        }
    }

    // Listen to Firebase Feedback
    val database = FirebaseDatabase.getInstance().reference
    LaunchedEffect(Unit) {
        val startupTime = System.currentTimeMillis()
        database.child("feedback").addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val timestamp = snapshot.child("timestamp").value as? Long ?: 0L
                if (timestamp > startupTime) {
                    val name = snapshot.child("name").value?.toString() ?: "Someone"
                    AppNotificationManager.trigger("📩", "New feedback from $name!")
                }
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color(0xFFF9FAFB),
            bottomBar = {
                if (currentRoute != "login") {
                    Surface(
                        color = Color.White,
                        shadowElevation = 16.dp,
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        NavigationBar(
                            containerColor = Color.Transparent,
                            contentColor = Color(0xFF111827),
                            tonalElevation = 0.dp
                        ) {
                            items.forEach { item ->
                                NavigationBarItem(
                                    icon = { Icon(item.icon, contentDescription = item.label) },
                                    label = { 
                                        val text = when (item.route) {
                                            "dashboard" -> Lang.get("Home", "ಮುಖಪುಟ")
                                            "meal"      -> Lang.get("Meals", "ಊಟ")
                                            "facility"  -> Lang.get("Facility", "ಸೌಲಭ್ಯ")
                                            "stars"     -> Lang.get("Stars", "ತಾರೆಗಳು")
                                            "feedback"  -> Lang.get("Feedback", "ಪ್ರತಿಕ್ರಿಯೆ")
                                            else        -> item.label
                                        }
                                        Text(text) 
                                    },
                                    selected = currentRoute == item.route,
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.White,
                                        selectedTextColor = Color(0xFF111827),
                                        indicatorColor = Color(0xFF111827),
                                        unselectedIconColor = Color(0xFF6B7280),
                                        unselectedTextColor = Color(0xFF6B7280)
                                    ),
                                    onClick = {
                                        if (currentRoute != item.route) {
                                            navController.navigate(item.route) {
                                                popUpTo("dashboard") {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            val auth = FirebaseAuth.getInstance()
            val startDest = if (auth.currentUser != null) BottomNavItem.Dashboard.route else "login"

            NavHost(
                navController = navController, 
                startDestination = startDest,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("login") { LoginScreen(navController) }
                composable(BottomNavItem.Dashboard.route) { DashboardScreen(navController, innerPadding = PaddingValues(0.dp)) }
                composable(BottomNavItem.Meal.route)      { MealScreen(onBackClick = { navController.popBackStack() }) }
                composable(BottomNavItem.Facility.route)  { FacilityScreen(onBackClick = { navController.popBackStack() }) }
                composable(BottomNavItem.Stars.route)     { StarsScreen(onBackClick = { navController.popBackStack() }) }
                composable(BottomNavItem.Feedback.route)  { FeedbackScreen(onBackClick = { navController.popBackStack() }) }
            }
        }

        // Advanced Notification Dropdown Overlay
        AnimatedVisibility(
            visible = currentNotification != null,
            enter = slideInVertically(initialOffsetY = { -it - 150 }) + fadeIn(initialAlpha = 0.3f),
            exit = slideOutVertically(targetOffsetY = { -it - 150 }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp, start = 20.dp, end = 20.dp)
        ) {
            currentNotification?.let { notif ->
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF111827).copy(alpha = 0.95f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                    shadowElevation = 24.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.1f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(notif.icon, fontSize = 22.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                "Notification",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                notif.message,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}