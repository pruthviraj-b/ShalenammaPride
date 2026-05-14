package com.pruthviraj.shalenammapride.screens

import com.pruthviraj.shalenammapride.Lang
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.PhotoLibrary
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
import java.util.Calendar

data class FacilityItem(
    val id        : String = "",
    val title     : String = "",
    val description: String = "",
    val imageUrl  : String = "",
    val category  : String = "",
    val timestamp : Long   = 0L
)

@Composable
fun FacilityScreen(onBackClick: () -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var facilities by remember { mutableStateOf<List<FacilityItem>>(emptyList()) }
    var showForm by remember { mutableStateOf(false) }
    var showHistoryView by remember { mutableStateOf(false) }

    // form fields
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Classroom") }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var statusMsg by remember { mutableStateOf("") }

    val database = FirebaseDatabase.getInstance().reference
    val categories = listOf(
        CategoryData("Building", "🏢", Color(0xFFFEE2E2), Color(0xFFEF4444)),
        CategoryData("Classrooms", "🏫", Color(0xFFDCFCE7), Color(0xFF22C55E)),
        CategoryData("Playground", "⛹️", Color(0xFFDBEAFE), Color(0xFF3B82F6)),
        CategoryData("Library", "📚", Color(0xFFFEF9C3), Color(0xFFEAB308)),
        CategoryData("Laboratory", "🧪", Color(0xFFF3E8FF), Color(0xFFA855F7))
    )

    // Load facilities from Firebase
    LaunchedEffect(Unit) {
        database.child("facility").get().addOnSuccessListener { snapshot ->
            val list = mutableListOf<FacilityItem>()
            snapshot.children.forEach { child ->
                list.add(FacilityItem(
                    id          = child.key ?: "",
                    title       = child.child("title").value.toString(),
                    description = child.child("description").value.toString(),
                    imageUrl    = child.child("imageUrl").value.toString(),
                    category    = child.child("category").value.toString(),
                    timestamp   = child.child("timestamp").value as? Long ?: 0L
                ))
            }
            facilities = list.sortedByDescending { it.timestamp }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> selectedUri = uri }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            val file = java.io.File(context.cacheDir, "facility_temp.jpg")
            file.outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, it) }
            selectedUri = Uri.fromFile(file)
        }
    }

    // Stats
    val thisWeekCount = remember(facilities) {
        val weekStart = Calendar.getInstance().apply { set(Calendar.DAY_OF_WEEK, firstDayOfWeek) }.timeInMillis
        facilities.count { it.timestamp >= weekStart }
    }
    val lastUpdated = remember(facilities) {
        if (facilities.isEmpty()) "No uploads yet"
        else {
            val date = java.util.Date(facilities.first().timestamp)
            java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault()).format(date)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Top Header ──
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFF3F4F6),
                modifier = Modifier.size(44.dp).clickable { onBackClick() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color(0xFF1A1A2E), modifier = Modifier.size(20.dp))
                }
            }
            
            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(Lang.get("Facility Gallery", "ಸೌಲಭ್ಯ ಗ್ಯಾಲರಿ"), color = Color(0xFF1A1A2E), fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(Lang.get("Upload and manage facility photos", "ಸೌಲಭ್ಯ ಫೋಟೋಗಳನ್ನು ನಿರ್ವಹಿಸಿ"), color = Color(0xFF9CA3AF), fontSize = 11.sp)
            }

            Surface(
                onClick = { showHistoryView = !showHistoryView },
                shape = RoundedCornerShape(20.dp),
                color = if (showHistoryView) Color(0xFFFF7A3D).copy(alpha = 0.1f) else Color(0xFFFFF4EE)
            ) {
                Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.PhotoLibrary, null, tint = Color(0xFFFF7A3D), modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("${facilities.size} photos", color = Color(0xFFFF7A3D), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // ── Stats Row ──
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FacilityStatCard("🖼️", "${facilities.size}", Lang.get("Total Photos", "ಒಟ್ಟು ಫೋಟೋಗಳು"), Color(0xFF6366F1), Modifier.weight(1f))
            FacilityStatCard("📅", "$thisWeekCount", Lang.get("This Week", "ಈ ವಾರ"), Color(0xFF10B981), Modifier.weight(1f))
            FacilityStatCard("🕒", lastUpdated, Lang.get("Last Updated", "ಕೊನೆಯದಾಗಿ ನವೀಕರಿಸಲಾಗಿದೆ"), Color(0xFFF59E0B), Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Primary Upload Card ──
        if (!showForm) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).clickable { showForm = true }
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .background(Brush.horizontalGradient(listOf(Color(0xFFFF7A3D), Color(0xFFFF9A62))), RoundedCornerShape(22.dp))
                        .padding(20.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.25f), modifier = Modifier.size(44.dp)) {
                                Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(24.dp)) }
                            }
                            Spacer(Modifier.width(14.dp))
                            Column {
                                Text(Lang.get("Add Facility Photo", "ಸೌಲಭ್ಯ ಫೋಟೋ ಸೇರಿಸಿ"), color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text(Lang.get("Upload photos of school facilities", "ಶಾಲಾ ಸೌಲಭ್ಯಗಳ ಫೋಟೋಗಳನ್ನು ಅಪ್‌ಲೋಡ್ ಮಾಡಿ"), color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp)
                            }
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                }
            }
        } else {
            // ── Upload Form ──
            Surface(
                shape = RoundedCornerShape(22.dp), color = Color.White, shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(Lang.get("Add New Photo", "ಹೊಸ ಫೋಟೋ ಸೇರಿಸಿ"), color = Color(0xFF1A1A2E), fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(12.dp))

                    if (selectedUri != null) {
                        AsyncImage(model = selectedUri, null, Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(14.dp)), contentScale = ContentScale.Crop)
                    } else {
                        Box(Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFFF9FAFB)).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                            Text("🖼️ Tap below to select", color = Color(0xFF9CA3AF), fontSize = 11.sp)
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp)) {
                        Button(onClick = { cameraLauncher.launch(null) }, Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3F4F6))) {
                            Text("📸 Camera", color = Color(0xFF374151), fontSize = 12.sp)
                        }
                        Button(onClick = { galleryLauncher.launch("image/*") }, Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3F4F6))) {
                            Text("🖼️ Gallery", color = Color(0xFF374151), fontSize = 12.sp)
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = title, 
                        onValueChange = { title = it }, 
                        label = { Text("Name (e.g. Science Lab)") }, 
                        modifier = Modifier.fillMaxWidth(), 
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF1A1A2E),
                            unfocusedTextColor = Color(0xFF1A1A2E),
                            focusedBorderColor = Color(0xFFFF7A3D)
                        )
                    )
                    Spacer(Modifier.height(12.dp))
                    
                    // Category Selection
                    Text("Select Category", fontSize = 12.sp, color = Color(0xFF6B7280))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
                        items(listOf("Classroom", "Laboratory", "Library", "Playground", "Building", "Other")) { cat ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (category == cat) Color(0xFFFF7A3D) else Color(0xFFF3F4F6),
                                modifier = Modifier.clickable { category = cat }
                            ) {
                                Text(cat, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = if (category == cat) Color.White else Color(0xFF374151), fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = { showForm = false; selectedUri = null; statusMsg = "" }, Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                            Text("Cancel", fontSize = 13.sp)
                        }
                        Button(
                            onClick = {
                                if (title.isBlank() || selectedUri == null) { statusMsg = "⚠️ Title and Image required!"; return@Button }
                                isUploading = true; statusMsg = "Uploading..."
                                scope.launch {
                                    val url = uploadFacilityImage(context, selectedUri!!)
                                    if (url != null) {
                                        val key = database.child("facility").push().key ?: ""
                                        val data = mapOf("title" to title, "description" to description, "imageUrl" to url, "category" to category, "timestamp" to System.currentTimeMillis())
                                        database.child("facility").child(key).setValue(data).addOnSuccessListener {
                                            facilities = listOf(FacilityItem(key, title, description, url, category, System.currentTimeMillis())) + facilities
                                            isUploading = false; showForm = false; title = ""; selectedUri = null; statusMsg = ""
                                            AppNotificationManager.trigger("🏫", "Photo Added!")
                                        }
                                    } else { isUploading = false; statusMsg = "❌ Failed" }
                                }
                            },
                            Modifier.weight(2f), enabled = !isUploading, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7A3D))
                        ) {
                            Text(if (isUploading) "Uploading..." else "Add Photo", color = Color.White, fontSize = 13.sp)
                        }
                    }
                    if (statusMsg.isNotEmpty()) Text(statusMsg, color = Color.Red, fontSize = 11.sp, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), textAlign = TextAlign.Center)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Empty State or Gallery ──
        if (facilities.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color(0xFFF9FAFB),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
            ) {
                Column(Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🖼️", fontSize = 48.sp)
                    Spacer(Modifier.height(16.dp))
                    Text(Lang.get("No photos added yet", "ಇನ್ನೂ ಯಾವುದೇ ಫೋಟೋಗಳನ್ನು ಸೇರಿಸಲಾಗಿಲ್ಲ"), color = Color(0xFF1A1A2E), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(Lang.get("Start by adding photos of your school facilities.", "ನಿಮ್ಮ ಶಾಲೆಯ ಸೌಲಭ್ಯಗಳ ಫೋಟೋಗಳನ್ನು ಸೇರಿಸುವ ಮೂಲಕ ಪ್ರಾರಂಭಿಸಿ."), color = Color(0xFF9CA3AF), fontSize = 11.sp, textAlign = TextAlign.Center)
                }
            }
        }

        // ── Full History Grid ──
        if (showHistoryView && facilities.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(Lang.get("All Facilities History", "ಎಲ್ಲಾ ಸೌಲಭ್ಯಗಳ ಇತಿಹಾಸ"), color = Color(0xFF1A1A2E), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                facilities.chunked(2).forEach { rowItems ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        rowItems.forEach { item ->
                            FacilityHistoryItem(item, Modifier.weight(1f), onDelete = {
                                database.child("facility").child(item.id).removeValue().addOnSuccessListener {
                                    facilities = facilities.filter { it.id != item.id }
                                }
                            })
                        }
                        if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Suggested Categories ──
        Text(Lang.get("Suggested Categories", "ಸೂಚಿಸಲಾದ ವಿಭಾಗಗಳು"), color = Color(0xFF1A1A2E), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 20.dp))
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(categories) { cat ->
                val count = facilities.count { it.category.equals(cat.name, ignoreCase = true) }
                CategoryCard(cat, count)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Smart Tip Card ──
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFFFFF8F0),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
        ) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("💡", fontSize = 24.sp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(Lang.get("Tip", "ಸಲಹೆ"), color = Color(0xFF92400E), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(
                        Lang.get("High quality photos help showcase your school facilities to parents and visitors.", "ಉತ್ತಮ ಗುಣಮಟ್ಟದ ಫೋಟೋಗಳು ನಿಮ್ಮ ಶಾಲೆಯ ಸೌಲಭ್ಯಗಳನ್ನು ಪೋಷಕರಿಗೆ ತೋರಿಸಲು ಸಹಾಯ ಮಾಡುತ್ತದೆ."),
                        color = Color(0xFF92400E), fontSize = 11.sp, lineHeight = 16.sp
                    )
                }
                Text("🖼️", fontSize = 24.sp, modifier = Modifier.alpha(0.5f))
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun FacilityStatCard(emoji: String, value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 3.dp, modifier = modifier) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(shape = CircleShape, color = color.copy(alpha = 0.1f), modifier = Modifier.size(34.dp)) {
                Box(contentAlignment = Alignment.Center) { Text(emoji, fontSize = 16.sp) }
            }
            Spacer(Modifier.height(8.dp))
            Text(value, color = Color(0xFF1A1A2E), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(label, color = Color(0xFF6B7280), fontSize = 8.sp, textAlign = TextAlign.Center, maxLines = 1)
        }
    }
}

@Composable
fun CategoryCard(cat: CategoryData, count: Int) {
    Surface(shape = RoundedCornerShape(18.dp), color = Color.White, shadowElevation = 2.dp, modifier = Modifier.width(110.dp)) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(shape = CircleShape, color = cat.bgColor, modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) { Text(cat.icon, fontSize = 20.sp) }
            }
            Spacer(Modifier.height(8.dp))
            Text(cat.name, color = Color(0xFF1A1A2E), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Text("$count photos", color = Color(0xFF6B7280), fontSize = 9.sp)
        }
    }
}

@Composable
fun FacilityHistoryItem(item: FacilityItem, modifier: Modifier = Modifier, onDelete: () -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFF3F4F6)), modifier = modifier) {
        Column {
            Box {
                AsyncImage(model = item.imageUrl, null, Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)), contentScale = ContentScale.Crop)
                Surface(shape = RoundedCornerShape(8.dp), color = Color.Black.copy(alpha = 0.5f), modifier = Modifier.align(Alignment.TopEnd).padding(6.dp).clickable { onDelete() }) {
                    Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(16.dp).padding(2.dp))
                }
            }
            Column(Modifier.padding(10.dp)) {
                Text(item.category, color = Color(0xFFFF7A3D), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(item.title, color = Color(0xFF1A1A2E), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                if (item.description.isNotEmpty()) {
                    Text(item.description, color = Color(0xFF9CA3AF), fontSize = 10.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
fun FacilityGridItem(item: FacilityItem, onDelete: () -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 2.dp, modifier = Modifier.width(160.dp)) {
        Column {
            Box {
                AsyncImage(model = item.imageUrl, null, Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)), contentScale = ContentScale.Crop)
                Surface(shape = RoundedCornerShape(8.dp), color = Color.Black.copy(alpha = 0.5f), modifier = Modifier.align(Alignment.TopEnd).padding(6.dp).clickable { onDelete() }) {
                    Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(16.dp).padding(2.dp))
                }
            }
            Column(Modifier.padding(10.dp)) {
                Text(item.category, color = Color(0xFFFF7A3D), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(item.title, color = Color(0xFF1A1A2E), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

data class CategoryData(val name: String, val icon: String, val bgColor: Color, val accentColor: Color)

suspend fun uploadFacilityImage(context: Context, imageUri: Uri): String? {
    return withContext(Dispatchers.IO) {
        try {
            val bytes = context.contentResolver.openInputStream(imageUri)?.readBytes() ?: return@withContext null
            val body = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("file", "facility.jpg", bytes.toRequestBody("image/*".toMediaType()))
                .addFormDataPart("upload_preset", "shalenamma_preset")
                .build()
            val req = Request.Builder().url("https://api.cloudinary.com/v1_1/ddwxml58b/image/upload").post(body).build()
            val res = OkHttpClient().newCall(req).execute()
            JSONObject(res.body?.string() ?: "").getString("secure_url")
        } catch (e: Exception) { null }
    }
}
