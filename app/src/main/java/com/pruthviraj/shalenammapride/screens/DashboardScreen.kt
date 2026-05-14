package com.pruthviraj.shalenammapride.screens

import com.pruthviraj.shalenammapride.Lang
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.database.FirebaseDatabase
import com.pruthviraj.shalenammapride.util.PdfGenerator
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun DashboardScreen(navController: NavController, innerPadding: PaddingValues) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    var mealsCount by remember { mutableStateOf(0) }
    var starsCount by remember { mutableStateOf(0) }
    var feedbackCount by remember { mutableStateOf(0) }
    var circularsCount by remember { mutableStateOf(0) }
    var isTodayMealPosted by remember { mutableStateOf(false) }
    var langState by remember { mutableStateOf(0) }

    // Portal Link Logic
    val prefs = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
    var portalUrl by remember { mutableStateOf(prefs.getString("portal_url", "https://shalenammapride-b4fc8.web.app/") ?: "https://shalenammapride-b4fc8.web.app/") }
    var showPortalDialog by remember { mutableStateOf(false) }
    var showUrlEditDialog by remember { mutableStateOf(false) }
    var tempUrl by remember { mutableStateOf(portalUrl) }

    LaunchedEffect(langState) {
        val db = FirebaseDatabase.getInstance().reference
        db.child("meals").get().addOnSuccessListener { snapshot ->
            mealsCount = snapshot.childrenCount.toInt()
            isTodayMealPosted = snapshot.children.any { it.child("date").value?.toString() == today }
        }
        db.child("stars").get().addOnSuccessListener { starsCount = it.childrenCount.toInt() }
        db.child("feedback").get().addOnSuccessListener { feedbackCount = it.childrenCount.toInt() }
        db.child("announcements").get().addOnSuccessListener { circularsCount = it.childrenCount.toInt() }
    }

    val greeting = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 0..11 -> Lang.get("Good Morning,", "ಶುಭೋದಯ,")
        in 12..16 -> Lang.get("Good Afternoon,", "ಶುಭ ಮಧ್ಯಾಹ್ನ,")
        else -> Lang.get("Good Evening,", "ಶುಭ ಸಂಜೆ,")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(innerPadding)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Header Area ──
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(greeting, color = Color(0xFF9CA3AF), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(Lang.get("Principal Admin", "ಮುಖ್ಯ ಅಡ್ಮಿನ್"), color = Color(0xFF1A1A2E), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("✨", fontSize = 20.sp)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                // User Request: Language Translation Toggle
                Surface(
                    onClick = { Lang.toggle(); langState++ },
                    shape = CircleShape,
                    color = Color(0xFFFF7A3D).copy(0.1f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(if (Lang.isKannada) "EN" else "ಕನ್ನಡ", color = Color(0xFFFF7A3D), fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Surface(
                    onClick = { navController.navigate("profile") },
                    shape = CircleShape,
                    color = Color(0xFFFF7A3D),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        // ── Hero Card (Real Stats Sync) ──
        Surface(
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 0.dp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(Color(0xFFFF7A3D), Color(0xFFFF9A62))), RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Column {
                            Text(Lang.get("SCHOOL OVERVIEW", "ಶಾಲೆಯ ಅವಲೋಕನ"), color = Color.White.copy(0.7f), fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                            Text(Lang.get("Active Management", "ಸಕ್ರಿಯ ನಿರ್ವಹಣೆ"), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        DashboardStatItem(Lang.get("Total Actions", "ಒಟ್ಟು"), "${mealsCount + starsCount + feedbackCount}", Icons.Outlined.Speed)
                        DashboardStatItem(Lang.get("Campus", "ಕ್ಯಾಂಪಸ್"), "Main", Icons.Outlined.LocationOn)
                        DashboardStatItem(Lang.get("Status", "ಸ್ಥಿತಿ"), if (isTodayMealPosted) "Synced" else "Pending", Icons.Outlined.Sync)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Quick Operations ──
        Text(Lang.get("Quick Operations", "ತ್ವರಿತ ಕಾರ್ಯಾಚರಣೆಗಳು"), color = Color(0xFF1A1A2E), fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 24.dp))
        Spacer(modifier = Modifier.height(14.dp))
        
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ActionCard("🍛", Lang.get("Meals", "ಊಟ"), Color(0xFFFF7A3D), Modifier.weight(1f)) { navController.navigate("meal") }
                ActionCard("🏫", Lang.get("Facilities", "ಸೌಲಭ್ಯ"), Color(0xFFF59E0B), Modifier.weight(1f)) { navController.navigate("facility") }
                ActionCard("⭐", Lang.get("Stars", "ತಾರೆ"), Color(0xFFFF9A62), Modifier.weight(1f)) { navController.navigate("stars") }
                ActionCard("💬", Lang.get("Feedback", "ಪ್ರತಿಕ್ರಿಯೆ"), Color(0xFF10B981), Modifier.weight(1f)) { navController.navigate("feedback") }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ActionCard(
                    emoji = "📢", 
                    title = Lang.get("Circulars", "ಸುತ್ತೋಲೆ"), 
                    accent = Color(0xFF3B82F6), 
                    modifier = Modifier.weight(1f),
                    isNeon = true
                ) { 
                    navController.navigate("announcements") 
                }
                
                // Premium Neon Portal Button
                ActionCard(
                    emoji = "🌐",
                    title = Lang.get("Portal", "ಪೋರ್ಟಲ್"),
                    accent = Color(0xFF6366F1),
                    modifier = Modifier.weight(1f),
                    isNeon = true
                ) {
                    showPortalDialog = true
                }

                ActionCard("👤", Lang.get("Profile", "ಪ್ರೊಫೈಲ್"), Color(0xFF8B5CF6), Modifier.weight(1f)) { navController.navigate("profile") }
                ActionCard("📄", Lang.get("Reports", "ವರದಿ"), Color(0xFFEC4899), Modifier.weight(1f)) { 
                    scope.launch { PdfGenerator.generateSchoolReport(context, mealsCount, starsCount, feedbackCount) }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Engagement Analytics (Smaller 4 Cards - User Request) ──
        Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text(Lang.get("Engagement Analytics", "ವಿಶ್ಲೇಷಣೆ"), color = Color(0xFF1A1A2E), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
            Text(Lang.get("Real-time", "ನೈಜ ಸಮಯ"), color = Color(0xFF9CA3AF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp), Arrangement.spacedBy(8.dp)) {
            ActivityStatCard("🍛", mealsCount, Lang.get("Meals", "ಊಟ"), Color(0xFFFF7A3D).copy(0.06f), Color(0xFFFF7A3D), Modifier.weight(1f))
            ActivityStatCard("⭐", starsCount, Lang.get("Stars", "ತಾರೆ"), Color(0xFFF59E0B).copy(0.06f), Color(0xFFF59E0B), Modifier.weight(1f))
            ActivityStatCard("💬", feedbackCount, Lang.get("Ideas", "ವಿಚಾರ"), Color(0xFF10B981).copy(0.06f), Color(0xFF10B981), Modifier.weight(1f))
            ActivityStatCard("📢", circularsCount, Lang.get("Notices", "ನೋಟಿಸ್"), Color(0xFF3B82F6).copy(0.06f), Color(0xFF3B82F6), Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Smart Insights (Working / Warning Theme - User Request) ──
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (!isTodayMealPosted) Color(0xFFFFF1F1) else Color(0xFFF0FDF4),
            border = BorderStroke(1.dp, if (!isTodayMealPosted) Color(0xFFFECACA) else Color(0xFFBBF7D0)),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (!isTodayMealPosted) Icons.Outlined.ErrorOutline else Icons.Outlined.OfflinePin, 
                        null, 
                        tint = if (!isTodayMealPosted) Color(0xFFDC2626) else Color(0xFF16A34A), 
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (!isTodayMealPosted) Lang.get("Action Required", "ಕ್ರಮ ಅಗತ್ಯವಿದೆ") else Lang.get("System Synced", "ಸಿಸ್ಟಮ್ ಸಿಂಕ್ ಆಗಿದೆ"), 
                        color = if (!isTodayMealPosted) Color(0xFFDC2626) else Color(0xFF16A34A), 
                        fontSize = 14.sp, 
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    if (!isTodayMealPosted) 
                        Lang.get("Today's mid-day meal photo is missing. Please post it immediately.", "ಇಂದಿನ ಬಿಸಿಯೂಟದ ಫೋಟೋ ಇಲ್ಲ. ದಯವಿಟ್ಟು ಕೂಡಲೇ ಅಪ್‌ಲೋಡ್ ಮಾಡಿ.")
                    else 
                        Lang.get("Excellent! All daily operational tasks are completed and synced.", "ಅತ್ಯುತ್ತಮ! ಎಲ್ಲಾ ದೈನಂದಿನ ಕೆಲಸಗಳು ಪೂರ್ಣಗೊಂಡಿವೆ."),
                    color = if (!isTodayMealPosted) Color(0xFF7F1D1D) else Color(0xFF064E3B), 
                    fontSize = 12.sp, 
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Download Section (Simplified) ──
        Surface(
            onClick = { scope.launch { PdfGenerator.generateSchoolReport(context, mealsCount, starsCount, feedbackCount) } },
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFF9FAFB),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
        ) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.FileDownload, null, tint = Color(0xFF1A1A2E), modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Text(Lang.get("Generate Engagement Report", "ವರದಿ ತಯಾರಿಸಿ"), color = Color(0xFF1A1A2E), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(120.dp))
    }

    // --- Portal Link Dialogs ---
    if (showPortalDialog) {
        AlertDialog(
            onDismissRequest = { showPortalDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(28.dp),
            title = { Text(Lang.get("Parents Portal", "ಪೋಷಕರ ಪೋರ್ಟಲ್"), fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1A2E)) },
            text = { Text(Lang.get("Choose an action for the Parents Portal link.", "ಪೋಷಕರ ಪೋರ್ಟಲ್ ಲಿಂಕ್‌ಗಾಗಿ ಕ್ರಮವನ್ನು ಆರಿಸಿ."), color = Color(0xFF374151)) },
            confirmButton = {
                Button(
                    onClick = {
                        showPortalDialog = false
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(portalUrl))
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(Lang.get("Open Portal", "ಪೋರ್ಟಲ್ ತೆರೆಯಿರಿ"), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPortalDialog = false; showUrlEditDialog = true }) {
                    Text(Lang.get("Change Link", "ಲಿಂಕ್ ಬದಲಿಸಿ"), color = Color(0xFF6366F1))
                }
            }
        )
    }

    if (showUrlEditDialog) {
        AlertDialog(
            onDismissRequest = { showUrlEditDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(28.dp),
            title = { Text(Lang.get("Update Link", "ಲಿಂಕ್ ಅಪ್‌ಡೇಟ್ ಮಾಡಿ"), fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1A2E)) },
            text = {
                Column {
                    Text(Lang.get("Enter the new URL for the portal:", "ಪೋರ್ಟಲ್‌ಗಾಗಿ ಹೊಸ URL ಅನ್ನು ನಮೂದಿಸಿ:"), fontSize = 12.sp, color = Color.Gray)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tempUrl,
                        onValueChange = { tempUrl = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF1A1A2E),
                            unfocusedTextColor = Color(0xFF1A1A2E),
                            focusedBorderColor = Color(0xFF6366F1)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempUrl.isNotBlank()) {
                            portalUrl = tempUrl
                            prefs.edit().putString("portal_url", portalUrl).apply()
                            showUrlEditDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(Lang.get("Save", "ಉಳಿಸಿ"), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUrlEditDialog = false }) {
                    Text(Lang.get("Cancel", "ರದ್ದುಮಾಡಿ"))
                }
            }
        )
    }
}

@Composable
fun DashboardStatItem(label: String, value: String, icon: ImageVector) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Color.White.copy(0.8f), modifier = Modifier.size(10.dp))
            Spacer(Modifier.width(4.dp))
            Text(label, color = Color.White.copy(0.7f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
        }
        Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun ActionCard(emoji: String, title: String, accent: Color, modifier: Modifier = Modifier, isNeon: Boolean = false, onClick: () -> Unit) {
    val glowColor = if (isNeon) accent else Color.Transparent
    
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = if (isNeon) 8.dp else 0.dp,
        border = BorderStroke(if (isNeon) 2.dp else 1.dp, if (isNeon) accent.copy(alpha = 0.5f) else Color(0xFFF3F4F6)),
        modifier = modifier.then(
            if (isNeon) {
                Modifier.drawBehind {
                    val shadowRadius = 8.dp.toPx()
                    drawRoundRect(
                        color = accent.copy(alpha = 0.2f),
                        size = size.copy(width = size.width + shadowRadius, height = size.height + shadowRadius),
                        topLeft = Offset(-shadowRadius/2, -shadowRadius/2),
                        cornerRadius = CornerRadius(20.dp.toPx(), 20.dp.toPx())
                    )
                }
            } else Modifier
        )
    ) {
        Column(Modifier.padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                shape = CircleShape, 
                color = if (isNeon) accent.copy(alpha = 0.15f) else accent.copy(alpha = 0.06f), 
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) { 
                    Text(emoji, fontSize = 18.sp, modifier = if (isNeon) Modifier.shadow(4.dp, CircleShape) else Modifier) 
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                title, 
                color = if (isNeon) accent else Color(0xFF1A1A2E), 
                fontSize = 10.sp, 
                fontWeight = if (isNeon) FontWeight.ExtraBold else FontWeight.Bold
            )
        }
    }
}

@Composable
fun ActivityStatCard(emoji: String, count: Int, label: String, bg: Color, accent: Color, modifier: Modifier = Modifier) {
    Surface(shape = RoundedCornerShape(16.dp), color = bg, modifier = modifier) {
        Column(Modifier.padding(10.dp)) {
            Text(emoji, fontSize = 16.sp)
            Spacer(Modifier.height(6.dp))
            Text(count.toString(), color = Color(0xFF1A1A2E), fontSize = 16.sp, fontWeight = FontWeight.Black)
            Text(label, color = accent.copy(alpha = 0.7f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
    }
}
