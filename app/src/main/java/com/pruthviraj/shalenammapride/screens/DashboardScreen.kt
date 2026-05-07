package com.pruthviraj.shalenammapride.screens

import com.pruthviraj.shalenammapride.Lang
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import com.pruthviraj.shalenammapride.util.PdfGenerator
import com.google.firebase.database.FirebaseDatabase
import androidx.compose.runtime.*
import androidx.compose.foundation.Canvas
import androidx.compose.animation.core.*
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

private val BgDark    = Color(0xFFF9FAFB)
private val BgMid     = Color(0xFFFFFFFF)
private val PrimaryDark = Color(0xFF111827)
private val TextPrim  = Color(0xFF111827)
private val TextMuted = Color(0xFF6B7280)
private val Border    = Color(0xFFE5E7EB)
private val TagBg     = Color(0xFFF3F4F6)

@Composable
fun DashboardScreen(navController: NavController, innerPadding: PaddingValues) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var mealsCount by remember { mutableStateOf(0f) }
    var starsCount by remember { mutableStateOf(0f) }
    var feedbackCount by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        val db = FirebaseDatabase.getInstance().reference
        db.child("meals").get().addOnSuccessListener { snapshot ->
            mealsCount = snapshot.childrenCount.toFloat()
        }
        db.child("stars").get().addOnSuccessListener { snapshot ->
            starsCount = snapshot.childrenCount.toFloat()
        }
        db.child("feedback").get().addOnSuccessListener { snapshot ->
            feedbackCount = snapshot.childrenCount.toFloat()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(innerPadding)
            .verticalScroll(rememberScrollState())
    ) {
        // Premium Header
        Surface(
            color = BgMid,
            shadowElevation = 4.dp,
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        Lang.get("ADMIN PORTAL", "ಆಡಳಿತ ಪೋರ್ಟಲ್"), 
                        color = TextMuted, 
                        fontSize = 12.sp, 
                        fontWeight = FontWeight.Bold, 
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        Lang.get("Dashboard", "ಡ್ಯಾಶ್‌ಬೋರ್ಡ್"), 
                        color = PrimaryDark, 
                        fontSize = 32.sp, 
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Box(
                    modifier = Modifier
                        .background(TagBg, RoundedCornerShape(20.dp))
                        .border(1.dp, Border, RoundedCornerShape(20.dp))
                        .clickable { Lang.toggle() }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        Lang.get("A / ಅ", "ಅ / A"), 
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryDark
                    )
                }
            }
        }

        Column(modifier = Modifier.padding(20.dp)) {
            // Stats Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(Lang.get("SYSTEM STATUS", "ಸಿಸ್ಟಮ್ ಸ್ಥಿತಿ"), Lang.get("Online", "ಆನ್‌ಲೈನ್"), modifier = Modifier.weight(1f))
                StatCard(Lang.get("STAFF ACCESS", "ಸಿಬ್ಬಂದಿ ಪ್ರವೇಶ"), Lang.get("Active", "ಸಕ್ರಿಯ"), modifier = Modifier.weight(1f))
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            Text(Lang.get("Activity Overview", "ಚಟುವಟಿಕೆ ಅವಲೋಕನ"), color = TextPrim, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(16.dp))

            AnalyticsChartCard(mealsCount, starsCount, feedbackCount)

            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { 
                    scope.launch { 
                        PdfGenerator.generateSchoolReport(context, mealsCount.toInt(), starsCount.toInt(), feedbackCount.toInt()) 
                    } 
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark)
            ) {
                Icon(Icons.Filled.PictureAsPdf, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(Lang.get("Download PDF Report", "ಪಿಡಿಎಫ್ ವರದಿ ಡೌನ್‌ಲೋಡ್ ಮಾಡಿ"), color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text(Lang.get("Manage School", "ಶಾಲೆಯನ್ನು ನಿರ್ವಹಿಸಿ"), color = TextPrim, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(16.dp))

            // Grid for Navigation
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DashboardCard(
                    title = Lang.get("Mid-Day Meals", "ಬಿಸಿಯೂಟ"), 
                    icon = "🍛", 
                    subtitle = Lang.get("Post daily meals", "ದೈನಂದಿನ ಊಟವನ್ನು ಸೇರಿಸಿ"), 
                    onClick = { navController.navigate("meal") }, 
                    modifier = Modifier.weight(1f)
                )
                DashboardCard(
                    title = Lang.get("Facilities", "ಸೌಲಭ್ಯಗಳು"), 
                    icon = "🏫", 
                    subtitle = Lang.get("Upload gallery", "ಗ್ಯಾಲರಿ ಅಪ್‌ಲೋಡ್ ಮಾಡಿ"), 
                    onClick = { navController.navigate("facility") }, 
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DashboardCard(
                    title = Lang.get("Student Stars", "ವಿದ್ಯಾರ್ಥಿ ತಾರೆಗಳು"), 
                    icon = "⭐", 
                    subtitle = Lang.get("Add achievements", "ಸಾಧನೆಗಳನ್ನು ಸೇರಿಸಿ"), 
                    onClick = { navController.navigate("stars") }, 
                    modifier = Modifier.weight(1f)
                )
                DashboardCard(
                    title = Lang.get("Feedback", "ಪ್ರತಿಕ್ರಿಯೆ"), 
                    icon = "💬", 
                    subtitle = Lang.get("Read parent ideas", "ಪೋಷಕರ ವಿಚಾರಗಳನ್ನು ಓದಿ"), 
                    onClick = { navController.navigate("feedback") }, 
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun DashboardCard(title: String, icon: String, subtitle: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = BgMid,
        border = androidx.compose.foundation.BorderStroke(1.dp, Border),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(icon, fontSize = 32.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, color = TextPrim, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, color = TextMuted, fontSize = 12.sp)
        }
    }
}

@Composable
fun AnalyticsChartCard(meals: Float, stars: Float, feedback: Float) {
    val maxVal = maxOf(meals, stars, feedback, 1f) // Avoid division by zero
    val animMeals by animateFloatAsState(targetValue = meals / maxVal, animationSpec = tween(1000, easing = FastOutSlowInEasing))
    val animStars by animateFloatAsState(targetValue = stars / maxVal, animationSpec = tween(1200, easing = FastOutSlowInEasing))
    val animFeedback by animateFloatAsState(targetValue = feedback / maxVal, animationSpec = tween(1400, easing = FastOutSlowInEasing))

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = BgMid,
        border = androidx.compose.foundation.BorderStroke(1.dp, Border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(Lang.get("Engagement", "ತೊಡಗಿಸಿಕೊಳ್ಳುವಿಕೆ"), color = TextMuted, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text(Lang.get("All Time", "ಎಲ್ಲಾ ಸಮಯ"), color = PrimaryDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(modifier = Modifier.fillMaxWidth().height(160.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
                ChartBar("🍛", Lang.get("Meals", "ಊಟ"), animMeals, meals.toInt(), Color(0xFFF59E0B))
                ChartBar("⭐", Lang.get("Stars", "ತಾರೆಗಳು"), animStars, stars.toInt(), Color(0xFF3B82F6))
                ChartBar("💬", Lang.get("Feedback", "ಪ್ರತಿಕ್ರಿಯೆ"), animFeedback, feedback.toInt(), Color(0xFF10B981))
            }
        }
    }
}

@Composable
fun ChartBar(icon: String, label: String, progress: Float, rawValue: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom, modifier = Modifier.fillMaxHeight()) {
        Text(rawValue.toString(), color = PrimaryDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Box(modifier = Modifier.height(90.dp).width(30.dp), contentAlignment = Alignment.BottomCenter) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRoundRect(
                    color = TagBg,
                    size = Size(size.width, size.height),
                    cornerRadius = CornerRadius(15.dp.toPx(), 15.dp.toPx())
                )
                val barHeight = size.height * progress
                drawRoundRect(
                    color = color,
                    topLeft = Offset(0f, size.height - barHeight),
                    size = Size(size.width, barHeight),
                    cornerRadius = CornerRadius(15.dp.toPx(), 15.dp.toPx())
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(icon, fontSize = 18.sp)
        Text(label, color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Medium)
    }
}
