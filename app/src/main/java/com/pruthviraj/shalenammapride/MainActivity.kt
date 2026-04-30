package com.pruthviraj.shalenammapride

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.pruthviraj.shalenammapride.screens.*
import com.pruthviraj.shalenammapride.ui.theme.ShalenammaPrideTheme

// Define the 4 tabs
sealed class BottomNavItem(val label: String, val icon: ImageVector) {
    object Meal     : BottomNavItem("Daily Meal", Icons.Filled.Home)
    object Facility : BottomNavItem("Facility",   Icons.Filled.Place)
    object Stars    : BottomNavItem("Stars",       Icons.Filled.Star)
    object Feedback : BottomNavItem("Feedback",    Icons.Filled.Email)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShalenammaPrideTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val tabs = listOf(
        BottomNavItem.Meal,
        BottomNavItem.Facility,
        BottomNavItem.Stars,
        BottomNavItem.Feedback
    )
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick  = { selectedTab = index },
                        icon     = { Icon(tab.icon, contentDescription = tab.label) },
                        label    = { Text(tab.label) }
                    )
                }
            }
        }
    ) { paddingValues ->
        // Show the right screen based on selected tab
        when (selectedTab) {
            0 -> MealScreen()
            1 -> FacilityScreen()
            2 -> StarsScreen()
            3 -> FeedbackScreen()
        }
    }
}