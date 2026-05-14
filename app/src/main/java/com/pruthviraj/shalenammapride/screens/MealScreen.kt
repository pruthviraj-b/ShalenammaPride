package com.pruthviraj.shalenammapride.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import com.pruthviraj.shalenammapride.AppNotificationManager
import com.pruthviraj.shalenammapride.Lang
import com.pruthviraj.shalenammapride.util.shimmerEffect

private const val CLOUD_NAME    = "ddwxml58b"
private const val UPLOAD_PRESET = "shalenamma_preset"

data class MealEntry(
    val id       : String = "",
    val imageUrl : String = "",
    val menuText : String = "",
    val date     : String = "",
    val timestamp: Long   = 0L
)

data class LibraryMeal(val day: String, val menu: String, val type: String = "Regular")

val MEAL_LIBRARY = listOf(
    LibraryMeal("Monday", "Steamed Rice, Mixed Veg Sambar, Beans Palya"),
    LibraryMeal("Tuesday", "Bisibele Bath, Coconut Chutney"),
    LibraryMeal("Wednesday", "Rice, Tomato Rasam, Vegetable Poriyal"),
    LibraryMeal("Thursday", "Lemon Rice, Channa Sundal"),
    LibraryMeal("Friday", "Tomato Rice, Vegetable Kurma"),
    LibraryMeal("Saturday", "Khichdi, Vegetable Curry"),
    LibraryMeal("Festival", "Pulav, Sweet Kesari, Mysore Pak", "Special"),
    LibraryMeal("Festival", "Children's Day Special: Veg Fried Rice, Paneer Curry", "Special")
)

@Composable
fun MealScreen(onBackClick: () -> Unit = {}) {
    val context = LocalContext.current
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val dateLabel = SimpleDateFormat("EEEE • MMM dd", Locale.getDefault()).format(Date()).uppercase()

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var menuText by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }
    var showUploadForm by remember { mutableStateOf(false) }
    var mealHistory by remember { mutableStateOf<List<MealEntry>>(emptyList()) }
    var todayMeal by remember { mutableStateOf<MealEntry?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Advanced Features State
    var isEggAvailable by remember { mutableStateOf(true) }
    var isMilkAvailable by remember { mutableStateOf(true) }
    var isFruitAvailable by remember { mutableStateOf(true) }
    var showSwapDialog by remember { mutableStateOf(false) }
    var showHistoryView by remember { mutableStateOf(false) }

    val database = FirebaseDatabase.getInstance().reference

    LaunchedEffect(Unit) {
        database.child("meals").get().addOnSuccessListener { snapshot ->
            val list = mutableListOf<MealEntry>()
            snapshot.children.forEach { child ->
                val meal = MealEntry(
                    id        = child.key ?: "",
                    imageUrl  = child.child("imageUrl").value.toString(),
                    menuText  = child.child("menuText").value.toString(),
                    date      = child.child("date").value.toString(),
                    timestamp = child.child("timestamp").value as? Long ?: 0L
                )
                list.add(meal)
                if (meal.date == today) todayMeal = meal
            }
            mealHistory = list.sortedByDescending { it.timestamp }
            isLoading = false
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            val file = java.io.File(context.cacheDir, "meal_temp.jpg")
            file.outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, it) }
            selectedImageUri = Uri.fromFile(file)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent) // Let MainActivity white show through
            .verticalScroll(rememberScrollState())
    ) {
        // ── Header Area ──
        Box(
            modifier = Modifier.fillMaxWidth().height(180.dp).background(
                brush = Brush.verticalGradient(listOf(Color(0xFFFF7A3D), Color(0xFFFF9A62))),
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
            )
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(44.dp).clickable { onBackClick() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(dateLabel, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(Lang.get("Meal Management", "ಊಟದ ನಿರ್ವಹಣೆ"), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    HeaderStatPill(Icons.Outlined.EventAvailable, if (todayMeal != null) "Posted" else "Pending", if (todayMeal != null) Color(0xFF10B981) else Color.White.copy(0.4f))
                    HeaderStatPill(
                        Icons.Outlined.History, 
                        "History: ${mealHistory.size}", 
                        if (showHistoryView) Color.White.copy(0.4f) else Color.White.copy(0.2f),
                        onClick = { showHistoryView = !showHistoryView }
                    )
                }
            }
        }

        // ── Quick Info Section ──
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).offset(y = (-30).dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            QuickInfoCard(Lang.get("Total Records", "ಒಟ್ಟು ದಾಖಲೆ"), "${mealHistory.size}", Color(0xFF6366F1), Modifier.weight(1f))
            QuickInfoCard(Lang.get("Operational", "ಕಾರ್ಯಾಚರಣೆ"), "Ready", Color(0xFFFF7A3D), Modifier.weight(1f))
        }

        // ── Flexible Action Pills (User Request: Fixed and Working) ──
        Text(Lang.get("Flexible Actions", "ಫ್ಲೆಕ್ಸಿಬಲ್ ಕ್ರಮಗಳು"), color = Color(0xFF1A1A2E), fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 24.dp))
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp), Arrangement.spacedBy(10.dp)) {
            FlexibleActionPill("🔁", Lang.get("Repeat Last", "ಹಿಂದಿನದನ್ನು ಬಳಸಿ"), Modifier.weight(1f)) {
                if (mealHistory.isNotEmpty()) {
                    menuText = mealHistory.first().menuText
                    showUploadForm = true
                    AppNotificationManager.trigger("🔁", "Menu copied from history!")
                } else {
                    AppNotificationManager.trigger("⚠️", "No history found!")
                }
            }
            FlexibleActionPill("🔀", Lang.get("Swap Day", "ದಿನ ಬದಲಿಸಿ"), Modifier.weight(1f)) {
                showSwapDialog = true
            }
            FlexibleActionPill("✨", Lang.get("Special", "ವಿಶೇಷ"), Modifier.weight(1f)) {
                menuText = "Festival Special: "
                showUploadForm = true
            }
        }

        Spacer(Modifier.height(28.dp))

        // ── Item Availability Toggles ──
        Text(Lang.get("Item Inventory", "ವಸ್ತುಗಳ ಲಭ್ಯತೆ"), color = Color(0xFF1A1A2E), fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 24.dp))
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp), Arrangement.spacedBy(10.dp)) {
            ItemToggleCard("🥚", Lang.get("Egg", "ಮೊಟ್ಟೆ"), isEggAvailable, Modifier.weight(1f)) { isEggAvailable = !isEggAvailable }
            ItemToggleCard("🥛", Lang.get("Milk", "ಹಾಲು"), isMilkAvailable, Modifier.weight(1f)) { isMilkAvailable = !isMilkAvailable }
            ItemToggleCard("🍎", Lang.get("Fruit", "ಹಣ್ಣು"), isFruitAvailable, Modifier.weight(1f)) { isFruitAvailable = !isFruitAvailable }
        }

        // ── Smart Substitute Engine ──
        if (!isEggAvailable || !isMilkAvailable || !isFruitAvailable) {
            Spacer(Modifier.height(16.dp))
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFFFF7ED),
                border = BorderStroke(1.dp, Color(0xFFFFEDD5)),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Lightbulb, null, tint = Color(0xFFEA580C), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(Lang.get("Substitute Suggested", "ಬದಲಿ ಸಲಹೆ"), color = Color(0xFFEA580C), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Spacer(Modifier.height(8.dp))
                    val subs = mutableListOf<String>()
                    if (!isEggAvailable) subs.add("Sprouts / Chikki")
                    if (!isMilkAvailable) subs.add("Ragi Malt / Extra Veg")
                    if (!isFruitAvailable) subs.add("Banana / Dates")
                    Text("Recommendation: " + subs.joinToString(", "), color = Color(0xFF9A3412), fontSize = 12.sp, lineHeight = 18.sp)
                }
            }
        }

        Spacer(Modifier.height(28.dp))

        // ── Today's Active Entry ──
        if (todayMeal != null) {
            Text(Lang.get("Active Menu", "ಸಕ್ರಿಯ ಮೆನು"), color = Color(0xFF1A1A2E), fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(Modifier.height(16.dp))
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFF3F4F6)),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
            ) {
                Column {
                    if (todayMeal!!.imageUrl.isNotEmpty()) {
                        AsyncImage(model = todayMeal!!.imageUrl, contentDescription = null, modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)), contentScale = ContentScale.Crop)
                    }
                    Column(Modifier.padding(20.dp)) {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Column(Modifier.weight(1f)) {
                                Text(Lang.get("Served Today", "ಇಂದು ಬಡಿಸಲಾಗಿದೆ"), color = Color(0xFF9CA3AF), fontSize = 10.sp, fontWeight = FontWeight.Black)
                                Spacer(Modifier.height(4.dp))
                                Text(todayMeal!!.menuText, color = Color(0xFF1A1A2E), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            IconButton(onClick = {
                                database.child("meals").child(todayMeal!!.id).removeValue().addOnSuccessListener { todayMeal = null }
                            }) { Icon(Icons.Outlined.Delete, null, tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp)) }
                        }
                    }
                }
            }
            Spacer(Modifier.height(28.dp))
        }

        // ── History View Toggled ──
        if (showHistoryView) {
            Text(Lang.get("Meal History", "ಊಟದ ಇತಿಹಾಸ"), color = Color(0xFF1A1A2E), fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(Modifier.height(16.dp))
            Column(Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                mealHistory.forEach { meal ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFF3F4F6)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (meal.imageUrl.isNotEmpty()) {
                                AsyncImage(model = meal.imageUrl, contentDescription = null, modifier = Modifier.size(60.dp).clip(RoundedCornerShape(10.dp)), contentScale = ContentScale.Crop)
                                Spacer(Modifier.width(12.dp))
                            }
                            Column(Modifier.weight(1f)) {
                                Text(meal.date, color = Color(0xFF9CA3AF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(meal.menuText, color = Color(0xFF1A1A2E), fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            }
                            IconButton(onClick = {
                                database.child("meals").child(meal.id).removeValue().addOnSuccessListener {
                                    mealHistory = mealHistory.filter { it.id != meal.id }
                                }
                            }) { Icon(Icons.Outlined.Delete, null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp)) }
                        }
                    }
                }
                if (mealHistory.isEmpty()) {
                    Text("No history available", color = Color(0xFF9CA3AF), fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(32.dp))
                }
            }
            Spacer(Modifier.height(28.dp))
        }

        // ── Main Update Form ──
        if (!showUploadForm) {
            Surface(
                onClick = { showUploadForm = true },
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFFF9FAFB),
                border = BorderStroke(1.dp, Color(0xFFF3F4F6)),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
            ) {
                Row(Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = Color(0xFFFF7A3D), modifier = Modifier.size(52.dp)) {
                        Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.Add, null, tint = Color.White, modifier = Modifier.size(28.dp)) }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(Lang.get("Update Today's Meal", "ಇಂದಿನ ಊಟದ ಅಪ್‌ಡೇಟ್"), color = Color(0xFF1A1A2E), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                        Text(Lang.get("Snap photo and set menu", "ಫೋಟೋ ತೆಗೆದು ಮೆನು ಸೆಟ್ ಮಾಡಿ"), color = Color(0xFF9CA3AF), fontSize = 12.sp)
                    }
                }
            }
        } else {
            // ── The Form ──
            Surface(
                shape = RoundedCornerShape(24.dp), color = Color.White, shadowElevation = 12.dp,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
            ) {
                Column(Modifier.padding(24.dp)) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Text(Lang.get("Meal Update", "ಊಟದ ಅಪ್‌ಡೇಟ್"), color = Color(0xFF1A1A2E), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                        IconButton(onClick = { showUploadForm = false; selectedImageUri = null; menuText = "" }) { Icon(Icons.Outlined.Close, null) }
                    }
                    Spacer(Modifier.height(16.dp))
                    Box(
                        Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(20.dp)).background(Color(0xFFF9FAFB))
                            .clickable { cameraLauncher.launch(null) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImageUri != null) {
                            AsyncImage(model = selectedImageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Outlined.PhotoCamera, null, tint = Color(0xFFFF7A3D), modifier = Modifier.size(32.dp))
                                Spacer(Modifier.height(8.dp))
                                Text(Lang.get("Capture Meal Photo", "ಊಟದ ಫೋಟೋ ತೆಗೆಯಿರಿ"), color = Color(0xFFFF7A3D), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = menuText, onValueChange = { menuText = it },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        placeholder = { Text(Lang.get("Menu items...", "ಮೆನು ಐಟಂಗಳು..."), fontSize = 14.sp) },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF1A1A2E),
                            unfocusedTextColor = Color(0xFF1A1A2E),
                            focusedBorderColor = Color(0xFFFF7A3D),
                            unfocusedBorderColor = Color(0xFFF3F4F6),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color(0xFFF9FAFB)
                        )
                    )
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = {
                            if (menuText.isBlank()) { statusMessage = "Please enter menu!"; return@Button }
                            isUploading = true
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val imageUrl = if (selectedImageUri != null) uploadToCloudinary(context, selectedImageUri!!) ?: "" else ""
                                    val key = database.child("meals").push().key ?: today
                                    val data = mapOf("imageUrl" to imageUrl, "menuText" to menuText, "date" to today, "timestamp" to System.currentTimeMillis())
                                    database.child("meals").child(key).setValue(data).await()
                                    withContext(Dispatchers.Main) {
                                        val entry = MealEntry(key, imageUrl, menuText, today, System.currentTimeMillis())
                                        todayMeal = entry; mealHistory = listOf(entry) + mealHistory
                                        isUploading = false; showUploadForm = false; selectedImageUri = null; menuText = ""
                                        AppNotificationManager.trigger("🍱", "Meal Record Published!")
                                    }
                                } catch (e: Exception) { withContext(Dispatchers.Main) { isUploading = false } }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7A3D)),
                        enabled = !isUploading
                    ) {
                        if (isUploading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        else Text(Lang.get("Confirm & Post", "ಖಚಿತಪಡಿಸಿ ಮತ್ತು ಅಪ್‌ಲೋಡ್ ಮಾಡಿ"), color = Color.White, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }

        Spacer(Modifier.height(140.dp))
    }

    // ── ADVANCED SWAP DIALOG (User Request: Fixed and Flexible) ──
    if (showSwapDialog) {
        AlertDialog(
            onDismissRequest = { showSwapDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(28.dp),
            title = { Text(Lang.get("Select Menu to Use", "ಮೆನು ಆಯ್ಕೆಮಾಡಿ"), fontWeight = FontWeight.ExtraBold) },
            text = {
                Column(Modifier.fillMaxWidth()) {
                    Text(Lang.get("Pick from weekly standards or recent history", "ವಾರದ ಮೆನು ಅಥವಾ ಇತಿಹಾಸದಿಂದ ಆಯ್ಕೆಮಾಡಿ"), fontSize = 12.sp, color = Color(0xFF64748B))
                    Spacer(Modifier.height(16.dp))
                    
                    // Library Section
                    Text(Lang.get("Weekly Library", "ವಾರದ ಮೆನು"), fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFFFF7A3D))
                    Spacer(Modifier.height(8.dp))
                    Box(Modifier.height(100.dp)) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(MEAL_LIBRARY) { lib ->
                                Surface(
                                    onClick = { menuText = lib.menu; showSwapDialog = false; showUploadForm = true },
                                    shape = RoundedCornerShape(16.dp), color = Color(0xFFF8FAFC), border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    modifier = Modifier.width(140.dp)
                                ) {
                                    Column(Modifier.padding(12.dp)) {
                                        Text(lib.day, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                                        Spacer(Modifier.height(4.dp))
                                        Text(lib.menu, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, color = Color(0xFF1E293B))
                                    }
                                }
                            }
                        }
                    }
                    
                    if (mealHistory.isNotEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        Text(Lang.get("Recent History", "ಇತ್ತೀಚಿನ ಇತಿಹಾಸ"), fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFFFF7A3D))
                        Spacer(Modifier.height(8.dp))
                        Box(Modifier.height(100.dp)) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(mealHistory) { hist ->
                                    Surface(
                                        onClick = { menuText = hist.menuText; showSwapDialog = false; showUploadForm = true },
                                        shape = RoundedCornerShape(16.dp), color = Color(0xFFF8FAFC), border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                        modifier = Modifier.width(140.dp)
                                    ) {
                                        Column(Modifier.padding(12.dp)) {
                                            Text(hist.date, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                                            Spacer(Modifier.height(4.dp))
                                            Text(hist.menuText, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, color = Color(0xFF1E293B))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showSwapDialog = false }) { Text("Cancel", color = Color(0xFF94A3B8)) } }
        )
    }
}

@Composable
fun FlexibleActionPill(emoji: String, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier
    ) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 20.sp)
            Spacer(Modifier.height(4.dp))
            Text(label, color = Color(0xFF475569), fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun ItemToggleCard(emoji: String, label: String, isAvailable: Boolean, modifier: Modifier = Modifier, onToggle: () -> Unit) {
    Surface(
        onClick = onToggle,
        shape = RoundedCornerShape(16.dp),
        color = if (isAvailable) Color.White else Color(0xFFFEF2F2),
        border = BorderStroke(1.dp, if (isAvailable) Color(0xFFE5E7EB) else Color(0xFFFCA5A5)),
        modifier = modifier
    ) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 22.sp)
            Spacer(Modifier.height(6.dp))
            Text(label, color = if (isAvailable) Color(0xFF1A1A2E) else Color(0xFFB91C1C), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(if (isAvailable) "Available" else "OUT", color = if (isAvailable) Color(0xFF10B981) else Color(0xFFEF4444), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun HeaderStatPill(icon: ImageVector, label: String, color: Color, onClick: () -> Unit = {}) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp), 
        color = color
    ) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(6.dp))
            Text(label, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun QuickInfoCard(label: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Surface(shape = RoundedCornerShape(20.dp), color = Color.White, shadowElevation = 2.dp, modifier = modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(label, color = Color(0xFF9CA3AF), fontSize = 9.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(value, color = Color(0xFF1A1A2E), fontSize = 20.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.width(6.dp))
                Icon(Icons.Outlined.Analytics, null, tint = accent, modifier = Modifier.size(16.dp))
            }
        }
    }
}

suspend fun uploadToCloudinary(context: Context, imageUri: Uri): String? {
    return withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val imageBytes  = inputStream?.readBytes() ?: return@withContext null
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "meal.jpg", imageBytes.toRequestBody("image/*".toMediaType()))
                .addFormDataPart("upload_preset", UPLOAD_PRESET)
                .build()
            val request = Request.Builder()
                .url("https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload")
                .post(requestBody)
                .build()
            val response = OkHttpClient().newCall(request).execute()
            val json = JSONObject(response.body?.string() ?: "")
            json.getString("secure_url")
        } catch (e: Exception) { null }
    }
}