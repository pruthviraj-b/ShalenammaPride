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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

private const val CLOUD_NAME    = "ddwxml58b"
private const val UPLOAD_PRESET = "shalenamma_preset"

// Dark theme colors
private val BgDark    = Color(0xFF0F172A)
private val BgMid     = Color(0xFF1E293B)
private val BgCard    = Color(0xFF0F172A)
private val Green600  = Color(0xFF16A34A)
private val Green700  = Color(0xFF15803D)
private val Green100  = Color(0xFFBBF7D0)
private val TextPrim  = Color(0xFFF1F5F9)
private val TextMuted = Color(0xFF94A3B8)
private val Border    = Color(0xFF334155)

data class MealEntry(
    val imageUrl : String = "",
    val menuText : String = "",
    val date     : String = "",
    val timestamp: Long   = 0L
)

@Composable
fun MealScreen() {
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

    val database = FirebaseDatabase.getInstance().reference

    // Load all meals history
    LaunchedEffect(Unit) {
        database.child("meals").get().addOnSuccessListener { snapshot ->
            val list = mutableListOf<MealEntry>()
            snapshot.children.forEach { child ->
                val meal = MealEntry(
                    imageUrl  = child.child("imageUrl").value.toString(),
                    menuText  = child.child("menuText").value.toString(),
                    date      = child.child("date").value.toString(),
                    timestamp = child.child("timestamp").value as? Long ?: 0L
                )
                list.add(meal)
                if (meal.date == today) todayMeal = meal
            }
            mealHistory = list.sortedByDescending { it.timestamp }
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
        // ── Hero Header ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(listOf(Green600, Green700))
                )
                .padding(horizontal = 20.dp, vertical = 28.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            dayLabel,
                            color = Green100,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            "Mid-Day Meal",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (todayMeal != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Text(
                                "  LIVE  ",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {

            // ── Today's Meal Card ──
            if (todayMeal != null) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = BgMid,
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
                                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Today's Meal",
                                color = TextPrim,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                todayMeal!!.menuText,
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(Green600, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Posted today",
                                    color = Green600,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // ── Stats Row ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard("TOTAL POSTS", "${mealHistory.size}", modifier = Modifier.weight(1f))
                StatCard("THIS WEEK", "${mealHistory.take(7).size}", modifier = Modifier.weight(1f))
                StatCard("TODAY", if (todayMeal != null) "✓" else "—", modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Add Meal Button or Form ──
            if (!showUploadForm) {
                Button(
                    onClick  = { showUploadForm = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape  = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Green600)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Add Today's Meal",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            } else {

                // ── Upload Form ──
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = BgMid,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        Text(
                            "Post New Meal",
                            color = TextPrim,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Image preview / placeholder
                        if (selectedImageUri != null) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Selected photo",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(14.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(BgCard)
                                    .border(
                                        width = 1.5.dp,
                                        color = Border,
                                        shape = RoundedCornerShape(14.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("📷", fontSize = 32.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        "Tap below to add photo",
                                        color = TextMuted,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Camera + Gallery buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick  = { cameraLauncher.launch(null) },
                                modifier = Modifier.weight(1f),
                                shape    = RoundedCornerShape(12.dp),
                                colors   = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1E3A5F)
                                )
                            ) {
                                Text("📸 Camera", color = Color(0xFF93C5FD), fontSize = 13.sp)
                            }
                            Button(
                                onClick  = { galleryLauncher.launch("image/*") },
                                modifier = Modifier.weight(1f),
                                shape    = RoundedCornerShape(12.dp),
                                colors   = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2D1B69)
                                )
                            ) {
                                Text("🖼️ Gallery", color = Color(0xFFC4B5FD), fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

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
                                focusedBorderColor   = Green600,
                                unfocusedBorderColor = Border,
                                cursorColor          = Green600
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Post + Cancel
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick  = {
                                    showUploadForm   = false
                                    selectedImageUri = null
                                    menuText         = ""
                                    statusMessage    = ""
                                },
                                modifier = Modifier.weight(1f),
                                shape    = RoundedCornerShape(12.dp),
                                colors   = ButtonDefaults.outlinedButtonColors(
                                    contentColor = TextMuted
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp, Border
                                )
                            ) {
                                Text("Cancel")
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
                                                            val newEntry = MealEntry(imageUrl, menuText, today, System.currentTimeMillis())
                                                            todayMeal      = newEntry
                                                            mealHistory    = listOf(newEntry) + mealHistory
                                                            isUploading    = false
                                                            showUploadForm = false
                                                            selectedImageUri = null
                                                            menuText       = ""
                                                            statusMessage  = "✅ Posted!"
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
                                modifier = Modifier.weight(2f),
                                enabled  = !isUploading,
                                shape    = RoundedCornerShape(12.dp),
                                colors   = ButtonDefaults.buttonColors(containerColor = Green600)
                            ) {
                                Text(
                                    if (isUploading) "Posting..." else "✅ Post Meal",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (statusMessage.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                statusMessage,
                                color = if (statusMessage.startsWith("❌"))
                                    Color(0xFFFC8181) else Green600,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                modifier  = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // ── Meal History ──
            if (mealHistory.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Previous Meals",
                    color = TextPrim,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(mealHistory) { meal ->
                        MealHistoryCard(meal)
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
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF1E293B),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, color = Color(0xFF94A3B8), fontSize = 9.sp, letterSpacing = 0.5.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = Color(0xFFF1F5F9), fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MealHistoryCard(meal: MealEntry) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF1E293B),
        modifier = Modifier.width(140.dp)
    ) {
        Column {
            if (meal.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = meal.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .background(Color(0xFF166534)),
                    contentAlignment = Alignment.Center
                ) { Text("🍛", fontSize = 28.sp) }
            }
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    meal.date,
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp
                )
                Text(
                    meal.menuText,
                    color = Color(0xFFF1F5F9),
                    fontSize = 11.sp,
                    maxLines = 2
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