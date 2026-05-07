package com.pruthviraj.shalenammapride.screens

import com.pruthviraj.shalenammapride.Lang
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.pruthviraj.shalenammapride.AppNotificationManager

// Premium White Theme Colors
private val BgDark    = Color(0xFFF9FAFB) // Light gray background
private val BgMid     = Color(0xFFFFFFFF) // White cards
private val BgCard    = Color(0xFFF3F4F6) // Placeholder background
private val PrimaryDark = Color(0xFF111827) // Dark text & primary button
private val TagBg     = Color(0xFFF3F4F6) // Light gray tags
private val TextPrim  = Color(0xFF111827)
private val TextMuted = Color(0xFF6B7280)
private val Border    = Color(0xFFE5E7EB) // Soft gray borders

data class StarEntry(
    val id          : String = "",
    val studentName : String = "",
    val achievement : String = "",
    val imageUrl    : String = "",
    val category    : String = "",
    val timestamp   : Long   = 0L
)

@Composable
fun StarsScreen(onBackClick: () -> Unit = {}) {
    val context  = LocalContext.current
    var stars    by remember { mutableStateOf<List<StarEntry>>(emptyList()) }
    var showForm by remember { mutableStateOf(false) }
    var studentName  by remember { mutableStateOf("") }
    var achievement  by remember { mutableStateOf("") }
    var category     by remember { mutableStateOf("Student of the Week") }
    var selectedUri  by remember { mutableStateOf<Uri?>(null) }
    var isUploading  by remember { mutableStateOf(false) }
    var statusMsg    by remember { mutableStateOf("") }

    val database   = FirebaseDatabase.getInstance().reference
    val categories = listOf("Student of the Week","Sports Winner","Academic Excellence","Best Behaviour")

    LaunchedEffect(Unit) {
        database.child("stars").get().addOnSuccessListener { snapshot ->
            val list = mutableListOf<StarEntry>()
            snapshot.children.forEach { child ->
                list.add(StarEntry(
                    id          = child.key ?: "",
                    studentName = child.child("studentName").value.toString(),
                    achievement = child.child("achievement").value.toString(),
                    imageUrl    = child.child("imageUrl").value.toString(),
                    category    = child.child("category").value.toString(),
                    timestamp   = child.child("timestamp").value as? Long ?: 0L
                ))
            }
            stars = list.sortedByDescending { it.timestamp }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> selectedUri = uri }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val file = java.io.File(context.cacheDir, "star_temp.jpg")
            file.outputStream().use {
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, it)
            }
            selectedUri = Uri.fromFile(file)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
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
                            "SCHOOL PRIDE",
                            color = TextMuted,
                            fontSize = 12.sp,
                            letterSpacing = 1.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            Lang.get("Student Stars", "ವಿದ್ಯಾರ್ಥಿ ತಾರೆಗಳು"),
                            color = PrimaryDark,
                            fontSize = 26.sp,
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
                    Text(
                        "${stars.size} stars",
                        color = TextPrim,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
        


        LazyColumn(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item { Spacer(modifier = Modifier.height(12.dp)) }

            // Add button
            if (!showForm) {
                item {
                    Button(
                        onClick = { showForm = true },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape  = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            Lang.get("Add Star Student", "ಸ್ಟಾರ್ ವಿದ್ಯಾರ್ಥಿಯನ್ನು ಸೇರಿಸಿ"),
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                }
            } else {
                // Upload form
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = BgMid,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {

                            Text(
                                "New Star Student",
                                color = TextPrim,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(20.dp))

                            // Photo circle
                            Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                                if (selectedUri != null) {
                                    AsyncImage(
                                        model = selectedUri,
                                        contentDescription = null,
                                        modifier = Modifier.size(100.dp).clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(100.dp)
                                            .clip(CircleShape)
                                            .background(BgCard)
                                            .border(1.dp, Border, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) { Text("👤", fontSize = 36.sp) }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Camera + Gallery
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(
                                    onClick = { cameraLauncher.launch(null) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = TagBg),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Border)
                                ) { Text("📸 Camera", color = PrimaryDark, fontSize = 13.sp, fontWeight = FontWeight.Medium) }

                                Button(
                                    onClick = { galleryLauncher.launch("image/*") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = TagBg),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Border)
                                ) { Text("🖼️ Gallery", color = PrimaryDark, fontSize = 13.sp, fontWeight = FontWeight.Medium) }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Name field
                            OutlinedTextField(
                                value = studentName,
                                onValueChange = { studentName = it },
                                label = { Text("Student name", color = TextMuted) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor     = TextPrim,
                                    unfocusedTextColor   = TextPrim,
                                    focusedBorderColor   = PrimaryDark,
                                    unfocusedBorderColor = Border,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Achievement field
                            OutlinedTextField(
                                value = achievement,
                                onValueChange = { achievement = it },
                                label = { Text("Achievement (e.g. Won district football)", color = TextMuted) },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor     = TextPrim,
                                    unfocusedTextColor   = TextPrim,
                                    focusedBorderColor   = PrimaryDark,
                                    unfocusedBorderColor = Border,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Category chips
                            Text("Category", color = TextPrim, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            CustomFlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                categories.forEach { cat ->
                                    FilterChip(
                                        selected = category == cat,
                                        onClick  = { category = cat },
                                        label    = { Text(cat, fontSize = 12.sp) },
                                        colors   = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = PrimaryDark,
                                            selectedLabelColor     = Color.White,
                                            containerColor         = TagBg,
                                            labelColor             = TextPrim
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            borderColor = if(category == cat) PrimaryDark else Border,
                                            enabled = true,
                                            selected = category == cat
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Cancel + Submit
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        showForm    = false
                                        selectedUri = null
                                        studentName = ""
                                        achievement = ""
                                        statusMsg   = ""
                                    },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Border)
                                ) { Text("Cancel", color = TextMuted, fontWeight = FontWeight.Medium) }

                                Button(
                                    onClick = {
                                        if (studentName.isBlank() || achievement.isBlank()) {
                                            statusMsg = "⚠️ Fill name and achievement!"
                                            return@Button
                                        }
                                        isUploading = true
                                        statusMsg   = "Saving..."

                                        CoroutineScope(Dispatchers.IO).launch {
                                            val imageUrl = if (selectedUri != null)
                                                uploadStarImage(context, selectedUri!!) else ""

                                            val key  = database.child("stars").push().key ?: return@launch
                                            val data = mapOf(
                                                "studentName" to studentName,
                                                "achievement" to achievement,
                                                "imageUrl"    to (imageUrl ?: ""),
                                                "category"    to category,
                                                "timestamp"   to System.currentTimeMillis()
                                            )
                                            database.child("stars").child(key).setValue(data)
                                                .addOnSuccessListener {
                                                    CoroutineScope(Dispatchers.Main).launch {
                                                        stars = listOf(StarEntry(
                                                            key, studentName, achievement,
                                                            imageUrl ?: "", category,
                                                            System.currentTimeMillis()
                                                        )) + stars
                                                        isUploading = false
                                                        showForm    = false
                                                        studentName = ""
                                                        achievement = ""
                                                        selectedUri = null
                                                        statusMsg   = ""
                                                        AppNotificationManager.trigger("⭐", "Student Star Added!")
                                                    }
                                                }
                                                .addOnFailureListener {
                                                    CoroutineScope(Dispatchers.Main).launch {
                                                        isUploading = false
                                                        statusMsg   = "❌ ${it.message}"
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
                                        if (isUploading) "Saving..." else "Add Star",
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            if (statusMsg.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    statusMsg,
                                    color = if (statusMsg.startsWith("❌")) Color(0xFFDC2626) else PrimaryDark,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }

            // Stars list
            items(stars) { star -> 
                StarCard(star, onDelete = {
                    database.child("stars").child(star.id).removeValue().addOnSuccessListener {
                        stars = stars.filter { it.id != star.id }
                    }
                }) 
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun CustomFlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    // A simple FlowRow implementation using Accompanist or native Layout.
    // For simplicity, falling back to a wrapped Layout
    androidx.compose.ui.layout.Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val itemSpacing = 8.dp.roundToPx()
        val lineSpacing = 8.dp.roundToPx()
        
        var width = 0
        var height = 0
        var lineWidth = 0
        var lineHeight = 0
        
        val placeables = measurables.map { measurable ->
            val placeable = measurable.measure(constraints)
            if (lineWidth + placeable.width > constraints.maxWidth) {
                width = maxOf(width, lineWidth)
                height += lineHeight + lineSpacing
                lineWidth = 0
                lineHeight = 0
            }
            val x = lineWidth
            val y = height
            lineWidth += placeable.width + itemSpacing
            lineHeight = maxOf(lineHeight, placeable.height)
            
            Triple(placeable, x, y)
        }
        
        width = maxOf(width, lineWidth)
        height += lineHeight
        
        layout(width, height) {
            placeables.forEach { (placeable, x, y) ->
                placeable.placeRelative(x, y)
            }
        }
    }
}

@Composable
fun StarCard(star: StarEntry, onDelete: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = BgMid,
        border = androidx.compose.foundation.BorderStroke(1.dp, Border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            if (star.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = star.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.size(64.dp).clip(CircleShape).background(BgCard),
                    contentAlignment = Alignment.Center
                ) { Text("⭐", fontSize = 28.sp) }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp), 
                        color = TagBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Border)
                    ) {
                        Text(
                            star.category, 
                            color = TextMuted, 
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        "✕",
                        color = Color(0xFFDC2626),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onDelete() }.padding(4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    star.studentName, 
                    color = TextPrim,
                    fontSize = 16.sp, 
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    star.achievement, 
                    color = TextMuted, 
                    fontSize = 14.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

suspend fun uploadStarImage(context: Context, imageUri: Uri): String? {
    return withContext(Dispatchers.IO) {
        try {
            val bytes = context.contentResolver.openInputStream(imageUri)
            ?.readBytes() ?: return@withContext null
            val body = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("file", "star.jpg",
                    bytes.toRequestBody("image/*".toMediaType()))
                .addFormDataPart("upload_preset", "shalenamma_preset").build()
            val req = Request.Builder()
                .url("https://api.cloudinary.com/v1_1/ddwxml58b/image/upload")
                .post(body).build()
            val res = OkHttpClient().newCall(req).execute()
            JSONObject(res.body?.string() ?: "").getString("secure_url")
        } catch (e: Exception) { null }
    }
}

