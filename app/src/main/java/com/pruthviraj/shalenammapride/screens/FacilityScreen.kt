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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
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
private val BgCard    = Color(0xFFF3F4F6) // Off-white placeholders
private val PrimaryDark = Color(0xFF111827) // Dark text & buttons
private val TagBg     = Color(0xFFF3F4F6) // Light gray for tags
private val TextPrim  = Color(0xFF111827)
private val TextMuted = Color(0xFF6B7280)
private val Border    = Color(0xFFE5E7EB) // Soft gray borders

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
    val context  = LocalContext.current
    var facilities by remember { mutableStateOf<List<FacilityItem>>(emptyList()) }
    var showForm   by remember { mutableStateOf(false) }

    // form fields
    var title       by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category    by remember { mutableStateOf("Classroom") }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var statusMsg   by remember { mutableStateOf("") }

    val database   = FirebaseDatabase.getInstance().reference
    val categories = listOf("Classroom", "Laboratory", "Library", "Sports Ground", "Toilet", "Computer Lab", "Other")

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

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> selectedUri = uri }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val file = java.io.File(context.cacheDir, "facility_temp.jpg")
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
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PrimaryDark
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = Lang.get("Facility Gallery", "ಸೌಲಭ್ಯ ಗ್ಯಾಲರಿ"),
                        color = TextPrim,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = TagBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Border)
                ) {
                    Text(
                        "${facilities.size} photos",
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

            // ── Swipeable Pager (shown only when photos exist) ──
            if (facilities.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        "Featured Facilities",
                        color = TextPrim,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    FacilityPager(facilities)
                }
            } else {
                item { Spacer(modifier = Modifier.height(20.dp)) }
            }

            // ── Add Photo Button ──
            if (!showForm) {
                item {
                    Button(
                        onClick = { showForm = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape  = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Add Facility Photo",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                }
            } else {
                // ── Upload Form ──
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = BgMid,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {

                            Text(
                                "Add New Facility Photo",
                                color = TextPrim,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Image preview
                            if (selectedUri != null) {
                                AsyncImage(
                                    model = selectedUri,
                                    contentDescription = null,
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
                                        .height(140.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(BgCard)
                                        .border(1.dp, Border, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("🏫", fontSize = 28.sp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            "No photo selected",
                                            color = TextMuted,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Camera + Gallery
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(
                                    onClick = { cameraLauncher.launch(null) },
                                    modifier = Modifier.weight(1f),
                                    shape  = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = TagBg),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Border)
                                ) {
                                    Text(Lang.get("📸 Camera", "📸 ಕ್ಯಾಮೆರಾ"), color = PrimaryDark, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                }
                                Button(
                                    onClick = { galleryLauncher.launch("image/*") },
                                    modifier = Modifier.weight(1f),
                                    shape  = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = TagBg),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Border)
                                ) {
                                    Text(Lang.get("🖼️ Gallery", "🖼️ ಗ್ಯಾಲರಿ"), color = PrimaryDark, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Title field
                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                label = { Text(Lang.get("Room/Area name (e.g. Science Lab)", "ಕೋಣೆ/ಪ್ರದೇಶದ ಹೆಸರು (ಉದಾ. ವಿಜ್ಞಾನ ಪ್ರಯೋಗಾಲಯ)"), color = TextMuted) },
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

                            // Description field
                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                label = { Text(Lang.get("Short description (optional)", "ಚಿಕ್ಕ ವಿವರಣೆ (ಐಚ್ಛಿಕ)"), color = TextMuted) },
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

                            Spacer(modifier = Modifier.height(16.dp))

                            // Category chips
                            Text(Lang.get("Category", "ವರ್ಗ"), color = TextPrim, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(8.dp))

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

                            Spacer(modifier = Modifier.height(32.dp))

                            // Cancel + Submit
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        showForm    = false
                                        selectedUri = null
                                        title       = ""
                                        description = ""
                                        statusMsg   = ""
                                    },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    shape    = RoundedCornerShape(10.dp),
                                    border   = androidx.compose.foundation.BorderStroke(1.dp, Border)
                                ) { Text(Lang.get("Cancel", "ರದ್ದುಗೊಳಿಸಿ"), color = TextMuted, fontWeight = FontWeight.Medium) }

                                Button(
                                    onClick = {
                                        if (title.isBlank()) {
                                            statusMsg = "⚠️ Please enter a room name!"
                                            return@Button
                                        }
                                        if (selectedUri == null) {
                                            statusMsg = "⚠️ Please select a photo!"
                                            return@Button
                                        }
                                        isUploading = true
                                        statusMsg   = "Uploading..."

                                        CoroutineScope(Dispatchers.IO).launch {
                                            val imageUrl = uploadFacilityImage(context, selectedUri!!)

                                            if (imageUrl == null) {
                                                withContext(Dispatchers.Main) {
                                                    isUploading = false
                                                    statusMsg   = "❌ Upload failed!"
                                                }
                                                return@launch
                                            }

                                            val key  = database.child("facility").push().key ?: return@launch
                                            val data = mapOf(
                                                "title"       to title,
                                                "description" to description,
                                                "imageUrl"    to imageUrl,
                                                "category"    to category,
                                                "timestamp"   to System.currentTimeMillis()
                                            )
                                            database.child("facility").child(key)
                                                .setValue(data)
                                                .addOnSuccessListener {
                                                    CoroutineScope(Dispatchers.Main).launch {
                                                        val newItem = FacilityItem(
                                                            key, title, description,
                                                            imageUrl, category,
                                                            System.currentTimeMillis()
                                                        )
                                                        facilities  = listOf(newItem) + facilities
                                                        isUploading = false
                                                        showForm    = false
                                                        title       = ""
                                                        description = ""
                                                        selectedUri = null
                                                        statusMsg   = ""
                                                        AppNotificationManager.trigger("🏫", "Photo Added to Gallery!")
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
                                        if (isUploading) Lang.get("Uploading...", "ಅಪ್‌ಲೋಡ್ ಮಾಡಲಾಗುತ್ತಿದೆ...") else Lang.get("Add Photo", "ಫೋಟೋ ಸೇರಿಸಿ"),
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

            // ── Grid list of all facility photos ──
            if (facilities.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        Lang.get("All Rooms & Areas", "ಎಲ್ಲಾ ಕೊಠಡಿಗಳು ಮತ್ತು ಪ್ರದೇಶಗಳು"),
                        color = TextPrim,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                items(facilities) { item ->
                    FacilityCard(item, onDelete = {
                        database.child("facility").child(item.id).removeValue().addOnSuccessListener {
                            facilities = facilities.filter { it.id != item.id }
                        }
                    })
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

// ── Swipeable Pager ──
@Composable
fun FacilityPager(facilities: List<FacilityItem>) {
    val pagerState = rememberPagerState(pageCount = { facilities.size })

    Column {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val item = facilities[page]
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, Border, RoundedCornerShape(16.dp))
            ) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // White gradient overlay for text readability
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                listOf(Color.Transparent, Color(0xCC000000))
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.White.copy(alpha = 0.9f)
                        ) {
                            Text(
                                item.category,
                                color = PrimaryDark,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            item.title,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (item.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                item.description,
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dot indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(facilities.size) { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (pagerState.currentPage == index) 8.dp else 6.dp)
                        .background(
                            if (pagerState.currentPage == index) PrimaryDark else Border,
                            CircleShape
                        )
                )
            }
        }
    }
}

// ── Facility List Card ──
@Composable
fun FacilityCard(item: FacilityItem, onDelete: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = BgMid,
        border = androidx.compose.foundation.BorderStroke(1.dp, Border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = TagBg
                    ) {
                        Text(
                            item.category,
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
                    item.title,
                    color = TextPrim,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (item.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        item.description,
                        color = TextMuted,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

// ── Cloudinary upload ──
suspend fun uploadFacilityImage(context: Context, imageUri: Uri): String? {
    return withContext(Dispatchers.IO) {
        try {
            val bytes = context.contentResolver
                .openInputStream(imageUri)?.readBytes() ?: return@withContext null
            val body  = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file", "facility.jpg",
                    bytes.toRequestBody("image/*".toMediaType())
                )
                .addFormDataPart("upload_preset", "shalenamma_preset")
                .build()
            val req   = Request.Builder()
                .url("https://api.cloudinary.com/v1_1/ddwxml58b/image/upload")
                .post(body).build()
            val res   = OkHttpClient().newCall(req).execute()
            JSONObject(res.body?.string() ?: "").getString("secure_url")
        } catch (e: Exception) { null }
    }
}

@Composable
private fun CustomFlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
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

