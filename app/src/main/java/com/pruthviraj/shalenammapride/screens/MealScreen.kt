package com.pruthviraj.shalenammapride.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

// Premium White Theme Colors
private val BgDark    = Color(0xFFF9FAFB) // Light gray background
private val BgMid     = Color(0xFFFFFFFF) // White cards/surfaces
private val BgCard    = Color(0xFFF3F4F6) // Subtle off-white for placeholders
private val PrimaryDark = Color(0xFF111827) // Dark text & primary buttons
private val TagBg     = Color(0xFFF3F4F6) // Light gray for tags/badges
private val TextPrim  = Color(0xFF111827)
private val TextMuted = Color(0xFF6B7280)
private val Border    = Color(0xFFE5E7EB) // Soft gray borders

data class MealEntry(
    val id       : String = "",
    val imageUrl : String = "",
    val menuText : String = "",
    val date     : String = "",
    val timestamp: Long   = 0L
)

@Composable
fun MealScreen(onBackClick: () -> Unit = {}) {
    val context  = LocalContext.current
    val today    = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val dayLabel = SimpleDateFormat("EEEE • MMM dd", Locale.getDefault()).format(Date()).uppercase()

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var menuText         by remember { mutableStateOf("") }
    var isUploading      by remember { mutableStateOf(false) }
    var statusMessage    by remember { mutableStateOf("") }
    var showUploadForm   by remember { mutableStateOf(false) }
    var mealHistory      by remember { mutableStateOf<List<MealEntry>>(emptyList()) }
    var todayMeal        by remember { mutableStateOf<MealEntry?>(null) }
    var isLoading        by remember { mutableStateOf(true) }

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

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> selectedImageUri = uri }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val file = java.io.File(context.cacheDir, "meal_temp.jpg")
            file.outputStream().use {
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, it)
            }
            selectedImageUri = Uri.fromFile(file)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Premium Header ──
        Surface(
            color = BgMid,
            shadowElevation = 4.dp,
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { onBackClick() },
                        tint = PrimaryDark
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            dayLabel,
                            color = TextMuted,
                            fontSize = 12.sp,
                            letterSpacing = 1.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            Lang.get("Mid-Day Meal", "ಬಿಸಿಯೂಟ"),
                            color = PrimaryDark,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = TagBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Border)
                ) {
                    val isPosted = todayMeal != null
                    Text(
                        if (isPosted) Lang.get("Posted", "ಸೇರಿಸಲಾಗಿದೆ") else Lang.get("Pending", "ಬಾಕಿ ಇದೆ"),
                        color = if (isPosted) Color(0xFF065F46) else Color(0xFF991B1B),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
        


        Column(modifier = Modifier.padding(20.dp)) {

            // ── Today's Meal Card ──
            if (todayMeal != null) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = BgMid,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        if (todayMeal!!.imageUrl.isNotEmpty()) {
                            AsyncImage(
                                model = todayMeal!!.imageUrl,
                                contentDescription = "Today's meal",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                Lang.get("Today's Meal", "ಇಂದಿನ ಊಟ"),
                                color = TextPrim,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                todayMeal!!.menuText,
                                color = TextMuted,
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(PrimaryDark, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Posted today",
                                    color = PrimaryDark,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    "Delete", 
                                    color = Color(0xFFDC2626), 
                                    fontSize = 12.sp, 
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable {
                                        val idToDelete = todayMeal!!.id
                                        database.child("meals").child(idToDelete).removeValue().addOnSuccessListener {
                                            todayMeal = null
                                            mealHistory = mealHistory.filter { it.id != idToDelete }
                                        }
                                    }.padding(8.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // ── Stats Row ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(Lang.get("TOTAL POSTS", "ಒಟ್ಟು ಪೋಸ್ಟ್‌ಗಳು"), "${mealHistory.size}", modifier = Modifier.weight(1f))
                StatCard(Lang.get("THIS WEEK", "ಈ ವಾರ"), "${mealHistory.take(7).size}", modifier = Modifier.weight(1f))
                StatCard(Lang.get("TODAY", "ಇಂದು"), if (todayMeal != null) "✓" else "—", modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Add Meal Button or Form ──
            if (!showUploadForm) {
                Button(
                    onClick  = { showUploadForm = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape  = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        Lang.get("Add Today's Meal", "ಇಂದಿನ ಊಟ ಸೇರಿಸಿ"),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            } else {

                // ── Upload Form ──
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = BgMid,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {

                        Text(
                            Lang.get("Post New Meal", "ಹೊಸ ಊಟ ಸೇರಿಸಿ"),
                            color = TextPrim,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Image preview / placeholder
                        if (selectedImageUri != null) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Selected photo",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(BgCard)
                                    .border(
                                        width = 1.dp,
                                        color = Border,
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("📷", fontSize = 28.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Tap below to add photo",
                                        color = TextMuted,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Camera + Gallery buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick  = { cameraLauncher.launch(null) },
                                modifier = Modifier.weight(1f),
                                shape    = RoundedCornerShape(10.dp),
                                colors   = ButtonDefaults.buttonColors(containerColor = TagBg),
                                border   = androidx.compose.foundation.BorderStroke(1.dp, Border)
                            ) {
                                Text("📸 Camera", color = PrimaryDark, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            }
                            Button(
                                onClick  = { galleryLauncher.launch("image/*") },
                                modifier = Modifier.weight(1f),
                                shape    = RoundedCornerShape(10.dp),
                                colors   = ButtonDefaults.buttonColors(containerColor = TagBg),
                                border   = androidx.compose.foundation.BorderStroke(1.dp, Border)
                            ) {
                                Text("🖼️ Gallery", color = PrimaryDark, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Menu text field
                        OutlinedTextField(
                            value  = menuText,
                            onValueChange = { menuText = it },
                            label  = { Text("Menu items (e.g. Rice, Dal, Sabzi)", color = TextMuted) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor     = TextPrim,
                                unfocusedTextColor   = TextPrim,
                                focusedBorderColor   = PrimaryDark,
                                unfocusedBorderColor = Border,
                                cursorColor          = PrimaryDark,
                                unfocusedContainerColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Post + Cancel
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick  = {
                                    showUploadForm   = false
                                    selectedImageUri = null
                                    menuText         = ""
                                    statusMessage    = ""
                                },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape    = RoundedCornerShape(10.dp),
                                colors   = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted),
                                border   = androidx.compose.foundation.BorderStroke(1.dp, Border)
                            ) {
                                Text("Cancel", fontWeight = FontWeight.Medium)
                            }

                            Button(
                                onClick = {
                                    if (selectedImageUri == null) {
                                        statusMessage = "⚠️ Please select a photo!"
                                        return@Button
                                    }
                                    if (menuText.isBlank()) {
                                        statusMessage = "⚠️ Please enter menu!"
                                        return@Button
                                    }
                                    isUploading   = true
                                    statusMessage = "Uploading..."

                                    CoroutineScope(Dispatchers.IO).launch {
                                        try {
                                            val imageUrl = uploadToCloudinary(context, selectedImageUri!!)
                                            if (imageUrl != null) {
                                                val key      = database.child("meals").push().key ?: today
                                                val mealData = mapOf(
                                                    "imageUrl"  to imageUrl,
                                                    "menuText"  to menuText,
                                                    "date"      to today,
                                                    "timestamp" to System.currentTimeMillis()
                                                )
                                                database.child("meals").child(key)
                                                    .setValue(mealData)
                                                    .addOnSuccessListener {
                                                        CoroutineScope(Dispatchers.Main).launch {
                                                            val newEntry = MealEntry(key, imageUrl, menuText, today, System.currentTimeMillis())
                                                            todayMeal      = newEntry
                                                            mealHistory    = listOf(newEntry) + mealHistory
                                                            isUploading    = false
                                                            showUploadForm = false
                                                            selectedImageUri = null
                                                            menuText       = ""
                                                            statusMessage  = ""
                                                            AppNotificationManager.trigger("✅", "Mid-Day Meal Posted!")
                                                        }
                                                    }
                                                    .addOnFailureListener {
                                                        CoroutineScope(Dispatchers.Main).launch {
                                                            isUploading   = false
                                                            statusMessage = "❌ ${it.message}"
                                                        }
                                                    }
                                            } else {
                                                withContext(Dispatchers.Main) {
                                                    isUploading   = false
                                                    statusMessage = "❌ Upload failed!"
                                                }
                                            }
                                        } catch (e: Exception) {
                                            withContext(Dispatchers.Main) {
                                                isUploading   = false
                                                statusMessage = "❌ ${e.message}"
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.weight(2f).height(48.dp),
                                enabled  = !isUploading,
                                shape    = RoundedCornerShape(10.dp),
                                colors   = ButtonDefaults.buttonColors(containerColor = PrimaryDark)
                            ) {
                                Text(
                                    if (isUploading) "Posting..." else "Post Meal",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        if (statusMessage.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                statusMessage,
                                color = if (statusMessage.startsWith("❌")) Color(0xFFDC2626) else PrimaryDark,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                modifier  = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // ── Meal History ──
            if (isLoading) {
                Spacer(modifier = Modifier.height(32.dp))
                Text(Lang.get("Previous Meals", "ಹಿಂದಿನ ಊಟಗಳು"), color = TextPrim, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(modifier = Modifier.width(160.dp).height(180.dp).clip(RoundedCornerShape(12.dp)).shimmerEffect())
                    Box(modifier = Modifier.width(160.dp).height(180.dp).clip(RoundedCornerShape(12.dp)).shimmerEffect())
                }
            } else if (mealHistory.isNotEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    Lang.get("Previous Meals", "ಹಿಂದಿನ ಊಟಗಳು"),
                    color = TextPrim,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(16.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(mealHistory) { meal ->
                        MealHistoryCard(meal, onDelete = {
                            database.child("meals").child(meal.id).removeValue().addOnSuccessListener {
                                mealHistory = mealHistory.filter { it.id != meal.id }
                                if (meal.id == todayMeal?.id) todayMeal = null
                            }
                        })
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = BgMid,
        border = androidx.compose.foundation.BorderStroke(1.dp, Border),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, color = TextMuted, fontSize = 10.sp, letterSpacing = 0.5.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, color = TextPrim, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MealHistoryCard(meal: MealEntry, onDelete: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = BgMid,
        border = androidx.compose.foundation.BorderStroke(1.dp, Border),
        modifier = Modifier.width(160.dp)
    ) {
        Column {
            if (meal.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = meal.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(BgCard),
                    contentAlignment = Alignment.Center
                ) { Text("🍛", fontSize = 28.sp) }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        meal.date,
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        "✕", 
                        color = Color(0xFFDC2626), 
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onDelete() }.padding(4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    meal.menuText,
                    color = TextPrim,
                    fontSize = 13.sp,
                    maxLines = 2,
                    lineHeight = 18.sp
                )
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
                .addFormDataPart("file", "meal.jpg",
                    imageBytes.toRequestBody("image/*".toMediaType()))
                .addFormDataPart("upload_preset", UPLOAD_PRESET)
                .build()
            val request = Request.Builder()
                .url("https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload")
                .post(requestBody)
                .build()
            val response = OkHttpClient().newCall(request).execute()
            val json     = JSONObject(response.body?.string() ?: "")
            json.getString("secure_url")
        } catch (e: Exception) { null }
    }
}