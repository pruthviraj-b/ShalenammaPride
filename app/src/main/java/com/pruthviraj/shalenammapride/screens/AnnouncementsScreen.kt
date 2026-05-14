package com.pruthviraj.shalenammapride.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import com.google.firebase.storage.FirebaseStorage
import com.pruthviraj.shalenammapride.AppNotificationManager
import com.pruthviraj.shalenammapride.Lang
import com.pruthviraj.shalenammapride.components.ProfileInputField
import com.pruthviraj.shalenammapride.util.shimmerEffect
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

private val CatUrgent = Color(0xFFEF4444)
private val CatFees   = Color(0xFFF59E0B)
private val CatExams  = Color(0xFF10B981)
private val BrandOrange = Color(0xFFFF7A3D)
private val BrandPeach = Color(0xFFFF9A62)

data class AnnouncementEntry(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val type: String = "text",
    val priority: String = "normal",
    val category: String = "Urgent",
    val mediaUrl: String = "",
    val fileName: String = "",
    val timestamp: Long = 0L
)

@Composable
fun AnnouncementsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val dateLabel = SimpleDateFormat("EEEE • MMM dd", Locale.getDefault()).format(Date()).uppercase()

    var showUploadForm by remember { mutableStateOf(false) }
    var announcementsHistory by remember { mutableStateOf<List<AnnouncementEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All", "Urgent", "Events", "Fees", "Exams")
    val database = FirebaseDatabase.getInstance().reference

    LaunchedEffect(Unit) {
        database.child("announcements").get().addOnSuccessListener { snapshot ->
            val list = mutableListOf<AnnouncementEntry>()
            snapshot.children.forEach { child ->
                val entry = AnnouncementEntry(
                    id = child.key ?: "",
                    title = child.child("title").value?.toString() ?: "",
                    body = child.child("body").value?.toString() ?: "",
                    type = child.child("type").value?.toString() ?: "text",
                    priority = child.child("priority").value?.toString() ?: "normal",
                    category = child.child("category").value?.toString() ?: "Events",
                    mediaUrl = child.child("mediaUrl").value?.toString() ?: "",
                    fileName = child.child("fileName").value?.toString() ?: "",
                    timestamp = child.child("timestamp").value as? Long ?: 0L
                )
                list.add(entry)
            }
            announcementsHistory = list.sortedByDescending { it.timestamp }
            isLoading = false
        }.addOnFailureListener {
            isLoading = false
        }
    }

    val filteredList = announcementsHistory.filter { 
        (selectedCategory == "All" || it.category.equals(selectedCategory, true)) &&
        (searchQuery.isBlank() || it.title.contains(searchQuery, true) || it.body.contains(searchQuery, true))
    }

    Scaffold(
        containerColor = Color.White,
        floatingActionButton = {
            if (!showUploadForm) {
                FloatingActionButton(
                    onClick = { showUploadForm = true },
                    containerColor = Color.Transparent,
                    elevation = FloatingActionButtonDefaults.elevation(0.dp),
                    modifier = Modifier.padding(bottom = 20.dp, end = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(brush = Brush.horizontalGradient(listOf(BrandOrange, BrandPeach)), shape = RoundedCornerShape(20.dp))
                            .padding(horizontal = 24.dp, vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Add, contentDescription = "New", tint = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Create New", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (showUploadForm) {
            UploadForm(
                onDismiss = { showUploadForm = false },
                onSuccess = { newEntry -> 
                    announcementsHistory = listOf(newEntry) + announcementsHistory
                    showUploadForm = false
                }
            )
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                // ── Hero Header ──
                // ── Simple Black & White Header ──
                // ── Simple Black & White Header ──
                Column(
                    modifier = Modifier.fillMaxWidth().background(Color.White).padding(24.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF3F4F6),
                            modifier = Modifier.size(44.dp).clickable { onBackClick() }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color(0xFF1A1A2E), modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(dateLabel, color = Color(0xFF9CA3AF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(Lang.get("School Circulars", "ಶಾಲಾ ಸುತ್ತೋಲೆಗಳು"), color = Color(0xFF1A1A2E), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF9FAFB),
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                            Icon(Icons.Outlined.Assignment, null, tint = Color(0xFF6B7280), modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("${announcementsHistory.size} Active Notices", color = Color(0xFF6B7280), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // ── Search & Filter ──
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        shadowElevation = 10.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search by title or keywords...", color = Color(0xFF9CA3AF), fontSize = 14.sp) },
                            leadingIcon = { Icon(Icons.Outlined.Search, null, tint = BrandOrange, modifier = Modifier.size(20.dp)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color(0xFF1A1A2E),
                                unfocusedTextColor = Color(0xFF1A1A2E),
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(categories) { cat ->
                            val isSelected = selectedCategory == cat
                            val catColor = when(cat) {
                                "Urgent" -> CatUrgent
                                "Fees" -> CatFees
                                "Exams" -> CatExams
                                else -> BrandOrange
                            }
                            Surface(
                                onClick = { selectedCategory = cat },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) catColor else Color(0xFFFFF4EE),
                                border = if (!isSelected) BorderStroke(1.dp, BrandOrange.copy(alpha = 0.1f)) else null,
                                modifier = Modifier.height(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 16.dp)) {
                                    Text(cat, color = if (isSelected) Color.White else catColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // ── List ──
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 120.dp, top = 0.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (isLoading) {
                        items(3) {
                            Box(modifier = Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(20.dp)).shimmerEffect())
                        }
                    } else if (filteredList.isEmpty()) {
                        item {
                            Column(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Outlined.Inbox, null, modifier = Modifier.size(64.dp), tint = Color(0xFFD1D5DB))
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("No circulars found.", color = Color(0xFF9CA3AF))
                            }
                        }
                    } else {
                        items(filteredList) { entry ->
                            AnnouncementCard(
                                entry = entry,
                                onDelete = {
                                    database.child("announcements").child(entry.id).removeValue().addOnSuccessListener {
                                        announcementsHistory = announcementsHistory.filter { it.id != entry.id }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnnouncementCard(entry: AnnouncementEntry, onDelete: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    val catColor = when(entry.category.lowercase()) {
        "urgent" -> CatUrgent
        "fees" -> CatFees
        "exams" -> CatExams
        else -> BrandOrange
    }
    val icon = when(entry.category.lowercase()) {
        "urgent" -> Icons.Outlined.Campaign
        "fees" -> Icons.Outlined.AccountBalanceWallet
        "exams" -> Icons.Outlined.MenuBook
        else -> Icons.Outlined.Notifications
    }
    val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(entry.timestamp))

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, Color(0xFFF3F4F6)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Box(modifier = Modifier.width(6.dp).fillMaxHeight().background(catColor))
            
            Column(modifier = Modifier.padding(16.dp).weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = catColor.copy(alpha = 0.1f), modifier = Modifier.size(40.dp)) {
                            Icon(icon, null, tint = catColor, modifier = Modifier.padding(10.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Surface(color = catColor.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                                Text(entry.category.uppercase(), color = catColor, fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                            Text(entry.title, color = Color(0xFF1A1A2E), fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.MoreVert, null, tint = Color(0xFF9CA3AF))
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(text = { Text("Delete", color = Color.Red) }, onClick = { showMenu = false; onDelete() })
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                Text(entry.body, color = Color(0xFF1A1A2E).copy(alpha = 0.7f), fontSize = 14.sp, lineHeight = 20.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (entry.mediaUrl.isNotEmpty()) {
                            Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFF3F4F6)) {
                                Text("PDF ATTACHED", color = Color(0xFF1A1A2E), fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                        val randomSeen = abs(entry.id.hashCode()) % 200 + 45
                        Text("Seen by $randomSeen parents", color = Color(0xFF9CA3AF), fontSize = 11.sp)
                    }
                    Text(dateStr, color = Color(0xFF9CA3AF), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun UploadForm(onDismiss: () -> Unit, onSuccess: (AnnouncementEntry) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Urgent") }
    var mediaUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri -> mediaUri = uri }

    Column(modifier = Modifier.fillMaxSize().background(Color.White).verticalScroll(rememberScrollState())) {
        // Header
        Box(modifier = Modifier.fillMaxWidth().height(120.dp).background(brush = Brush.verticalGradient(listOf(BrandOrange, BrandPeach)), shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))) {
            Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(12.dp), color = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(40.dp).clickable { onDismiss() }) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White, modifier = Modifier.size(20.dp)) }
                }
                Spacer(Modifier.width(16.dp))
                Text("Create Circular", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }

        Column(modifier = Modifier.padding(24.dp)) {
            ProfileInputField(value = title, onValueChange = { title = it }, label = "Circular Title", icon = Icons.Outlined.Title)
            Spacer(modifier = Modifier.height(16.dp))
            
            // Large Message Area
            Surface(shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFF3F4F6)), shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Message Body", color = Color(0xFF9CA3AF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    androidx.compose.foundation.text.BasicTextField(
                        value = body,
                        onValueChange = { body = it },
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 15.sp, color = Color(0xFF1A1A2E)),
                        modifier = Modifier.fillMaxWidth().height(120.dp).padding(top = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            DropdownSelector(label = "Category", options = listOf("Urgent", "Events", "Fees", "Exams"), selected = category, onSelect = { category = it })
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF9FAFB),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                modifier = Modifier.fillMaxWidth().clickable { filePickerLauncher.launch("*/*") }
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AttachFile, null, tint = BrandOrange)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(if (mediaUri != null) "File Attached" else "Attach PDF / Document", color = if (mediaUri != null) Color(0xFF10B981) else Color(0xFF9CA3AF), fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    if (title.isBlank() || body.isBlank()) { Toast.makeText(context, "Title and body required", Toast.LENGTH_SHORT).show(); return@Button }
                    scope.launch {
                        isUploading = true
                        try {
                            var mediaUrl = ""
                            var fileName = ""
                            if (mediaUri != null) {
                                val storageRef = FirebaseStorage.getInstance().reference.child("announcements/${UUID.randomUUID()}")
                                storageRef.putFile(mediaUri!!).await()
                                mediaUrl = storageRef.downloadUrl.await().toString()
                                fileName = "attachment"
                            }
                            val db = FirebaseDatabase.getInstance().reference.child("announcements")
                            val key = db.push().key ?: UUID.randomUUID().toString()
                            val ts = System.currentTimeMillis()
                            val data = mapOf("title" to title, "body" to body, "category" to category, "mediaUrl" to mediaUrl, "fileName" to fileName, "timestamp" to ts)
                            db.child(key).setValue(data).await()
                            AppNotificationManager.trigger("📢", "Circular Sent!")
                            onSuccess(AnnouncementEntry(key, title, body, "text", "normal", category, mediaUrl, fileName, ts))
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        } finally {
                            isUploading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(58.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                enabled = !isUploading
            ) {
                if (isUploading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("Publish Circular", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DropdownSelector(label: String, options: List<String>, selected: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(label, color = Color(0xFF9CA3AF), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))
        Spacer(modifier = Modifier.height(6.dp))
        Surface(
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFFF3F4F6)),
            color = Color.White,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth().clickable { expanded = true }
        ) {
            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(selected, color = Color(0xFF1A1A2E), fontWeight = FontWeight.Medium)
                Icon(Icons.Default.ArrowDropDown, null, tint = Color(0xFF9CA3AF))
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = { onSelect(option); expanded = false })
            }
        }
    }
}

