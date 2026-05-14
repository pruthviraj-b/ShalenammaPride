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
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavGraph.Companion.findStartDestination
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.draw.clip

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
        
        // Truly transparent edge-to-edge UI
        window.setFlags(android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or 
                                                   android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }
        
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

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                if (currentRoute != "login") {
                    // ── Premium Floating Bottom Navigation ──
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 20.dp)
                            .navigationBarsPadding(),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Surface(
                            color = Color.White.copy(alpha = 0.95f),
                            shadowElevation = 12.dp,
                            shape = RoundedCornerShape(32.dp),
                            modifier = Modifier.fillMaxWidth().height(72.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                items.forEach { item ->
                                    key(item.route) {
                                        val isSelected = currentRoute == item.route
                                        val activeColor = Color(0xFFFF7A3D)
                                        val inactiveColor = Color(0xFF9CA3AF)
                                        
                                        val labelText = when (item.route) {
                                            "dashboard" -> Lang.get("Home", "ಮುಖಪುಟ")
                                            "meal"      -> Lang.get("Meals", "ಊಟ")
                                            "facility"  -> Lang.get("Facility", "ಸೌಲಭ್ಯ")
                                            "stars"     -> Lang.get("Stars", "ತಾರೆಗಳು")
                                            "feedback"  -> Lang.get("Feedback", "ಪ್ರತಿಕ್ರಿಯೆ")
                                            else        -> item.label
                                        }
    
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable(
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = null 
                                                ) {
                                                    navController.navigate(item.route) {
                                                        popUpTo(navController.graph.findStartDestination().id) {
                                                            saveState = true
                                                        }
                                                        launchSingleTop = true
                                                        restoreState = true
                                                    }
                                                },
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(width = 54.dp, height = 34.dp)
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(if (isSelected) activeColor else Color.Transparent),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = item.icon,
                                                    contentDescription = item.label,
                                                    tint = if (isSelected) Color.White else inactiveColor,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                text = labelText,
                                                color = if (isSelected) activeColor else inactiveColor,
                                                fontSize = 10.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            )
                                        }
                                    }
                                }
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
                composable("announcements") { AnnouncementsScreen(onBackClick = { navController.popBackStack() }) }
                composable("profile") { ProfileScreen(navController = navController) }
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
                    color = MaterialTheme.colorScheme.primary,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)),
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
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                notif.message,
                                color = MaterialTheme.colorScheme.onPrimary,
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