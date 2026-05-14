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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import com.pruthviraj.shalenammapride.AppNotificationManager
import java.text.SimpleDateFormat
import java.util.*

data class StarEntry(
    val id          : String = "",
    val studentName : String = "",
    val studentClass: String = "",
    val section     : String = "",
    val achievement : String = "",
    val imageUrl    : String = "",
    val category    : String = "",
    val timestamp   : Long   = 0L
)

@Composable
fun StarsScreen(onBackClick: () -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var stars by remember { mutableStateOf<List<StarEntry>>(emptyList()) }
    var showForm by remember { mutableStateOf(false) }
    var studentName by remember { mutableStateOf("") }
    var studentClass by remember { mutableStateOf("") }
    var section by remember { mutableStateOf("") }
    var achievement by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Academic Excellence") }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var statusMsg by remember { mutableStateOf("") }

    val database = FirebaseDatabase.getInstance().reference
    
    val starCategories = listOf(
        StarCategoryData("Academic Excellence", "🏆", Color(0xFFFEF9C3), Color(0xFFEAB308)),
        StarCategoryData("Good Behavior", "💚", Color(0xFFDCFCE7), Color(0xFF22C55E)),
        StarCategoryData("Leadership", "👤", Color(0xFFDBEAFE), Color(0xFF3B82F6)),
        StarCategoryData("Creativity", "🎨", Color(0xFFF3E8FF), Color(0xFFA855F7)),
        StarCategoryData("Sports", "🏃", Color(0xFFFEE2E2), Color(0xFFEF4444))
    )

    LaunchedEffect(Unit) {
        database.child("stars").get().addOnSuccessListener { snapshot ->
            val list = mutableListOf<StarEntry>()
            snapshot.children.forEach { child ->
                list.add(StarEntry(
                    id          = child.key ?: "",
                    studentName = child.child("studentName").value?.toString() ?: "",
                    studentClass= child.child("studentClass").value?.toString() ?: "",
                    section     = child.child("section").value?.toString() ?: "",
                    achievement = child.child("achievement").value?.toString() ?: "",
                    imageUrl    = child.child("imageUrl").value?.toString() ?: "",
                    category    = child.child("category").value?.toString() ?: "",
                    timestamp   = child.child("timestamp").value as? Long ?: 0L
                ))
            }
            stars = list.sortedByDescending { it.timestamp }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> selectedUri = uri }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            val file = java.io.File(context.cacheDir, "star_temp.jpg")
            file.outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, it) }
            selectedUri = Uri.fromFile(file)
        }
    }

    // Stats Calculation
    val thisWeekCount = remember(stars) {
        val weekStart = Calendar.getInstance().apply { set(Calendar.DAY_OF_WEEK, firstDayOfWeek) }.timeInMillis
        stars.count { it.timestamp >= weekStart }
    }
    val thisMonthCount = remember(stars) {
        val monthStart = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1) }.timeInMillis
        stars.count { it.timestamp >= monthStart }
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
                Text("SCHOOL PRIDE", color = Color(0xFFFF7A3D), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(Lang.get("Student Stars", "ವಿದ್ಯಾರ್ಥಿ ತಾರೆಗಳು"), color = Color(0xFF1A1A2E), fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(4.dp))
                    Text("✨", fontSize = 18.sp)
                }
                Text(Lang.get("Recognize and celebrate achievements", "ವಿದ್ಯಾರ್ಥಿಗಳ ಸಾಧನೆಗಳನ್ನು ಗುರುತಿಸಿ"), color = Color(0xFF9CA3AF), fontSize = 11.sp)
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFFFF4EE)
            ) {
                Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFFF7A3D), modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("${stars.size} stars", color = Color(0xFFFF7A3D), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // ── Stats Row ──
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StarStatCard("⭐", "${stars.size}", Lang.get("Total Stars", "ಒಟ್ಟು"), Color(0xFF6366F1), Modifier.weight(1f))
            StarStatCard("📅", "$thisWeekCount", Lang.get("This Week", "ಈ ವಾರ"), Color(0xFF10B981), Modifier.weight(1f))
            StarStatCard("🏆", "$thisMonthCount", Lang.get("This Month", "ಈ ತಿಂಗಳು"), Color(0xFFF59E0B), Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Primary Action Card ──
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
                                Text(Lang.get("Add Star Student", "ಸ್ಟಾರ್ ವಿದ್ಯಾರ್ಥಿ ಸೇರಿಸಿ"), color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text(Lang.get("Recognize a student's achievement", "ವಿದ್ಯಾರ್ಥಿಯ ಸಾಧನೆಯನ್ನು ಗುರುತಿಸಿ"), color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp)
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
                    Text(Lang.get("New Star Recognition", "ಹೊಸ ಸ್ಟಾರ್ ಮಾನ್ಯತೆ"), color = Color(0xFF1A1A2E), fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(16.dp))

                    Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        if (selectedUri != null) {
                            AsyncImage(model = selectedUri, null, Modifier.size(100.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                        } else {
                            Box(Modifier.size(100.dp).clip(CircleShape).background(Color(0xFFF3F4F6)).border(1.dp, Color(0xFFE5E7EB), CircleShape), contentAlignment = Alignment.Center) {
                                Text("👤", fontSize = 36.sp)
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = { cameraLauncher.launch(null) }, Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3F4F6))) {
                            Text("📸 Camera", color = Color(0xFF374151), fontSize = 12.sp)
                        }
                        Button(onClick = { galleryLauncher.launch("image/*") }, Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3F4F6))) {
                            Text("🖼️ Gallery", color = Color(0xFF374151), fontSize = 12.sp)
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = studentName, 
                        onValueChange = { studentName = it }, 
                        label = { Text("Student Name") }, 
                        modifier = Modifier.fillMaxWidth(), 
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF1A1A2E),
                            unfocusedTextColor = Color(0xFF1A1A2E),
                            focusedBorderColor = Color(0xFFFF7A3D)
                        )
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = studentClass,
                            onValueChange = { studentClass = it },
                            label = { Text("Class") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color(0xFF1A1A2E),
                                unfocusedTextColor = Color(0xFF1A1A2E),
                                focusedBorderColor = Color(0xFFFF7A3D)
                            )
                        )
                        OutlinedTextField(
                            value = section,
                            onValueChange = { section = it },
                            label = { Text("Section") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color(0xFF1A1A2E),
                                unfocusedTextColor = Color(0xFF1A1A2E),
                                focusedBorderColor = Color(0xFFFF7A3D)
                            )
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = achievement, 
                        onValueChange = { achievement = it }, 
                        label = { Text("Achievement details") }, 
                        modifier = Modifier.fillMaxWidth(), 
                        minLines = 2, 
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF1A1A2E),
                            unfocusedTextColor = Color(0xFF1A1A2E),
                            focusedBorderColor = Color(0xFFFF7A3D)
                        )
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    Text("Select Category", fontSize = 12.sp, color = Color(0xFF6B7280))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
                        items(starCategories) { cat ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (category == cat.name) Color(0xFFFF7A3D) else Color(0xFFF3F4F6),
                                modifier = Modifier.clickable { category = cat.name }
                            ) {
                                Text(cat.name, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = if (category == cat.name) Color.White else Color(0xFF374151), fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = { showForm = false; selectedUri = null; statusMsg = "" }, Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                            Text("Cancel", fontSize = 13.sp)
                        }
                        Button(
                            onClick = {
                                if (studentName.isBlank() || achievement.isBlank()) { statusMsg = "⚠️ Fill all details!"; return@Button }
                                isUploading = true; statusMsg = "Saving..."
                                scope.launch {
                                    val url = if (selectedUri != null) uploadStarImage(context, selectedUri!!) else ""
                                    val key = database.child("stars").push().key ?: ""
                                    val data = mapOf(
                                        "studentName" to studentName, 
                                        "studentClass" to studentClass,
                                        "section" to section,
                                        "achievement" to achievement, 
                                        "imageUrl" to (url ?: ""), 
                                        "category" to category, 
                                        "timestamp" to System.currentTimeMillis()
                                    )
                                    database.child("stars").child(key).setValue(data).addOnSuccessListener {
                                        stars = listOf(StarEntry(key, studentName, studentClass, section, achievement, url ?: "", category, System.currentTimeMillis())) + stars
                                        isUploading = false; showForm = false; studentName = ""; studentClass = ""; section = ""; achievement = ""; selectedUri = null; statusMsg = ""
                                        AppNotificationManager.trigger("⭐", "New Star Added!")
                                    }
                                }
                            },
                            Modifier.weight(2f), enabled = !isUploading, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7A3D))
                        ) {
                            Text(if (isUploading) "Saving..." else "Add Star", color = Color.White, fontSize = 13.sp)
                        }
                    }
                    if (statusMsg.isNotEmpty()) Text(statusMsg, color = Color.Red, fontSize = 11.sp, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), textAlign = TextAlign.Center)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Categories Section ──
        Text(Lang.get("Categories", "ವಿಭಾಗಗಳು"), color = Color(0xFF1A1A2E), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 20.dp))
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(starCategories) { cat ->
                val count = stars.count { it.category == cat.name }
                StarCategoryCard(cat, count)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Empty State Achievement Card ──
        if (stars.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color(0xFFF9FAFB),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
            ) {
                Column(Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⭐", fontSize = 48.sp)
                    Spacer(Modifier.height(16.dp))
                    Text(Lang.get("No star students yet", "ಇನ್ನೂ ಯಾವುದೇ ಸ್ಟಾರ್ ವಿದ್ಯಾರ್ಥಿಗಳಿಲ್ಲ"), color = Color(0xFF1A1A2E), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(Lang.get("Start by adding a star student and celebrate their achievements.", "ಸ್ಟಾರ್ ವಿದ್ಯಾರ್ಥಿಯನ್ನು ಸೇರಿಸುವ ಮೂಲಕ ಪ್ರಾರಂಭಿಸಿ."), color = Color(0xFF9CA3AF), fontSize = 11.sp, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(20.dp))
                    Button(onClick = { showForm = true }, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFF4EE))) {
                        Text(Lang.get("Add Star Student", "ಸ್ಟಾರ್ ವಿದ್ಯಾರ್ಥಿ ಸೇರಿಸಿ"), color = Color(0xFFFF7A3D), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Star Students List
            Text(Lang.get("Recent Achievements", "ಇತ್ತೀಚಿನ ಸಾಧನೆಗಳು"), color = Color(0xFF1A1A2E), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(Modifier.height(12.dp))
            LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(stars) { entry ->
                    StarStudentCard(entry, onDelete = {
                        database.child("stars").child(entry.id).removeValue().addOnSuccessListener {
                            stars = stars.filter { it.id != entry.id }
                        }
                    })
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Motivational Tip Card ──
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
                        Lang.get("Celebrating small achievements today builds great leaders for tomorrow.", "ಇಂದಿನ ಸಣ್ಣ ಸಾಧನೆಗಳನ್ನು ಆಚರಿಸುವುದು ನಾಳೆಯ ಉತ್ತಮ ನಾಯಕರನ್ನು ನಿರ್ಮಿಸುತ್ತದೆ."),
                        color = Color(0xFF92400E), fontSize = 11.sp, lineHeight = 16.sp
                    )
                }
                Text("📜", fontSize = 24.sp, modifier = Modifier.alpha(0.5f))
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun StarStatCard(emoji: String, value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 3.dp, modifier = modifier) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(shape = CircleShape, color = color.copy(alpha = 0.1f), modifier = Modifier.size(34.dp)) {
                Box(contentAlignment = Alignment.Center) { Text(emoji, fontSize = 16.sp) }
            }
            Spacer(Modifier.height(8.dp))
            Text(value, color = Color(0xFF1A1A2E), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(label, color = Color(0xFF6B7280), fontSize = 9.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun StarCategoryCard(cat: StarCategoryData, count: Int) {
    Surface(shape = RoundedCornerShape(18.dp), color = Color.White, shadowElevation = 2.dp, modifier = Modifier.width(120.dp)) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(shape = CircleShape, color = cat.bgColor, modifier = Modifier.size(42.dp)) {
                Box(contentAlignment = Alignment.Center) { Text(cat.icon, fontSize = 22.sp) }
            }
            Spacer(Modifier.height(8.dp))
            Text(cat.name, color = Color(0xFF1A1A2E), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, maxLines = 1)
            Text("$count stars", color = Color(0xFF6B7280), fontSize = 9.sp)
        }
    }
}

@Composable
fun StarStudentCard(entry: StarEntry, onDelete: () -> Unit) {
    Surface(shape = RoundedCornerShape(20.dp), color = Color.White, shadowElevation = 3.dp, modifier = Modifier.width(180.dp)) {
        Column {
            Box {
                if (entry.imageUrl.isNotEmpty()) {
                    AsyncImage(model = entry.imageUrl, null, Modifier.fillMaxWidth().height(110.dp).clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)), contentScale = ContentScale.Crop)
                } else {
                    Box(Modifier.fillMaxWidth().height(110.dp).background(Color(0xFFF3F4F6)), contentAlignment = Alignment.Center) { Text("👤", fontSize = 40.sp) }
                }
                Surface(shape = CircleShape, color = Color.Black.copy(alpha = 0.5f), modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).clickable { onDelete() }) {
                    Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(16.dp).padding(2.dp))
                }
            }
            Column(Modifier.padding(12.dp)) {
                Text(entry.category.uppercase(), color = Color(0xFFFF7A3D), fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
                Text(entry.studentName, color = Color(0xFF1A1A2E), fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (entry.studentClass.isNotEmpty()) {
                    Text("${entry.studentClass} - ${entry.section}", color = Color(0xFF6B7280), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
                Spacer(Modifier.height(2.dp))
                Text(entry.achievement, color = Color(0xFF6B7280), fontSize = 11.sp, maxLines = 2, lineHeight = 14.sp, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

data class StarCategoryData(val name: String, val icon: String, val bgColor: Color, val accentColor: Color)

suspend fun uploadStarImage(context: Context, imageUri: Uri): String? {
    return withContext(Dispatchers.IO) {
        try {
            val bytes = context.contentResolver.openInputStream(imageUri)?.readBytes() ?: return@withContext null
            val body = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("file", "star.jpg", bytes.toRequestBody("image/*".toMediaType()))
                .addFormDataPart("upload_preset", "shalenamma_preset")
                .build()
            val req = Request.Builder().url("https://api.cloudinary.com/v1_1/ddwxml58b/image/upload").post(body).build()
            val res = OkHttpClient().newCall(req).execute()
            JSONObject(res.body?.string() ?: "").getString("secure_url")
        } catch (e: Exception) { null }
    }
}
