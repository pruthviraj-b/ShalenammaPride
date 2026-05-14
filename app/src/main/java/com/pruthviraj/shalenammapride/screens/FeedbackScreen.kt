package com.pruthviraj.shalenammapride.screens

import com.pruthviraj.shalenammapride.Lang
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import com.pruthviraj.shalenammapride.AppNotificationManager
import java.text.SimpleDateFormat
import java.util.*

data class FeedbackEntry(
    val id        : String  = "",
    val message   : String  = "",
    val name      : String  = "",
    val anonymous : Boolean = true,
    val resolved  : Boolean = false,
    val timestamp : Long    = 0L,
    val adminReply: String  = "",
    val replyTimestamp: Long= 0L,
    val imageUrl  : String  = ""
)

@Composable
fun FeedbackScreen(onBackClick: () -> Unit = {}) {
    var feedbackList by remember { mutableStateOf<List<FeedbackEntry>>(emptyList()) }
    var filterResolved by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val database = FirebaseDatabase.getInstance().reference

    // Real-time listener
    LaunchedEffect(Unit) {
        database.child("feedback").addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val list = mutableListOf<FeedbackEntry>()
                snapshot.children.forEach { child ->
                    list.add(FeedbackEntry(
                        id        = child.key ?: "",
                        message   = child.child("message").value?.toString() ?: "",
                        name      = child.child("name").value?.toString() ?: "",
                        anonymous = child.child("anonymous").value as? Boolean ?: true,
                        resolved  = child.child("resolved").value as? Boolean ?: false,
                        timestamp = child.child("timestamp").value as? Long ?: 0L,
                        adminReply= child.child("adminReply").value?.toString() ?: "",
                        replyTimestamp = child.child("replyTimestamp").value as? Long ?: 0L,
                        imageUrl  = child.child("image").value?.toString() ?: ""
                    ))
                }
                feedbackList = list.sortedByDescending { it.timestamp }
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        })
    }

    val pendingCount = feedbackList.count { !it.resolved }
    val resolvedCount = feedbackList.count { it.resolved }
    val totalCount = feedbackList.size

    val filteredList = feedbackList.filter { 
        (if (filterResolved) true else !it.resolved) && 
        (it.message.contains(searchQuery, ignoreCase = true) || it.name.contains(searchQuery, ignoreCase = true))
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color.White)
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
                Text("ADMIN INBOX", color = Color(0xFFFF7A3D), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                Text(Lang.get("Parent Feedback", "ಪೋಷಕರ ಪ್ರತಿಕ್ರಿಯೆ"), color = Color(0xFF1A1A2E), fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(Lang.get("View and manage feedback", "ಪ್ರತಿಕ್ರಿಯೆಗಳನ್ನು ನಿರ್ವಹಿಸಿ"), color = Color(0xFF9CA3AF), fontSize = 11.sp)
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFFFF4EE)
            ) {
                Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.ChatBubbleOutline, null, tint = Color(0xFFFF7A3D), modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("$pendingCount Pending", color = Color(0xFFFF7A3D), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // ── Stats Section ──
            item {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FeedbackStatCard("💬", "$pendingCount", "Pending", Color(0xFFFF7A3D), Modifier.weight(1f))
                    FeedbackStatCard("✅", "$resolvedCount", "Resolved", Color(0xFF10B981), Modifier.weight(1f))
                    FeedbackStatCard("👥", "$totalCount", "Total", Color(0xFF6366F1), Modifier.weight(1f))
                }
                Spacer(Modifier.height(20.dp))
            }

            // ── Search & Filter ──
            item {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search feedback...", fontSize = 14.sp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(20.dp)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF1A1A2E),
                            unfocusedTextColor = Color(0xFF1A1A2E),
                            unfocusedBorderColor = Color(0xFFF3F4F6),
                            focusedBorderColor = Color(0xFFFF7A3D),
                            unfocusedContainerColor = Color(0xFFF9FAFB),
                            focusedContainerColor = Color.White
                        )
                    )
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFF9FAFB),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF3F4F6)),
                        modifier = Modifier.size(56.dp).clickable { /* Show Filter */ }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Tune, null, tint = Color(0xFF1A1A2E), modifier = Modifier.size(20.dp))
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // ── Toggle Section ──
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFFFF8F5),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp, 8.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Text("Show Resolved", fontSize = 14.sp, color = Color(0xFF1A1A2E), fontWeight = FontWeight.Medium)
                        Switch(
                            checked = filterResolved,
                            onCheckedChange = { filterResolved = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFFFF7A3D),
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFE5E7EB)
                            )
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            // ── Feedback Cards ──
            if (filteredList.isEmpty()) {
                item {
                    Column(Modifier.fillMaxWidth().padding(top = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("✨", fontSize = 48.sp)
                        Text("Inbox is clear!", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
                        Text("No feedback items to show.", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                    }
                }
            } else {
                items(filteredList) { fb ->
                    FeedbackItemCard(fb, onResolve = { id, reply ->
                        val updates = mutableMapOf<String, Any>("resolved" to true)
                        if (reply.isNotBlank()) {
                            updates["adminReply"] = reply
                            updates["replyTimestamp"] = System.currentTimeMillis()
                        }
                        database.child("feedback").child(id).updateChildren(updates).addOnSuccessListener {
                            AppNotificationManager.trigger("✅", if(reply.isBlank()) "Resolved!" else "Replied & Resolved!")
                        }
                    })
                    Spacer(Modifier.height(16.dp))
                }
            }

            // ── Smart Tip Card ──
            item {
                Spacer(Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFFFF8F0),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("💡", fontSize = 24.sp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Tip", color = Color(0xFF92400E), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "Quick responses to feedback build stronger trust with parents.",
                                color = Color(0xFF92400E), fontSize = 11.sp, lineHeight = 16.sp
                            )
                        }
                        Text("✉️", fontSize = 24.sp, modifier = Modifier.alpha(0.5f))
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun FeedbackStatCard(emoji: String, value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Surface(shape = RoundedCornerShape(18.dp), color = Color.White, shadowElevation = 3.dp, modifier = modifier) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(shape = CircleShape, color = color.copy(alpha = 0.1f), modifier = Modifier.size(34.dp)) {
                Box(contentAlignment = Alignment.Center) { Text(emoji, fontSize = 16.sp) }
            }
            Spacer(Modifier.height(8.dp))
            Text(value, color = Color(0xFF1A1A2E), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(label, color = Color(0xFF6B7280), fontSize = 9.sp)
        }
    }
}

@Composable
fun FeedbackItemCard(fb: FeedbackEntry, onResolve: (String, String) -> Unit) {
    var isReplying by remember { mutableStateOf(false) }
    var replyText by remember { mutableStateOf("") }
    val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(fb.timestamp))

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFFFF4EE), modifier = Modifier.size(44.dp)) {
                        Box(contentAlignment = Alignment.Center) { Text(if(fb.anonymous) "🔒" else "👤", fontSize = 20.sp) }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(if(fb.anonymous) "Anonymous" else fb.name, color = Color(0xFF1A1A2E), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(dateStr, color = Color(0xFF9CA3AF), fontSize = 11.sp)
                    }
                }
                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFF3F4F6)) {
                    Text(if(fb.anonymous) "Anonymous" else "Named", color = Color(0xFF6B7280), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(fb.message, color = Color(0xFF1A1A2E), fontSize = 14.sp, lineHeight = 20.sp)

            if (fb.imageUrl.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                AsyncImage(model = fb.imageUrl, null, Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(14.dp)), contentScale = ContentScale.Crop)
            }

            if (fb.adminReply.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFF9FAFB)) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Admin Reply:", color = Color(0xFFFF7A3D), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(fb.adminReply, color = Color(0xFF374151), fontSize = 13.sp)
                    }
                }
            }

            if (!fb.resolved) {
                Spacer(Modifier.height(16.dp))
                Divider(color = Color(0xFFF3F4F6), thickness = 1.dp)
                Spacer(Modifier.height(12.dp))
                
                if (!isReplying) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        TextButton(onClick = { onResolve(fb.id, "") }) {
                            Text("Just Mark Resolved", color = Color(0xFF9CA3AF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { isReplying = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF7A3D)),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Reply, null, tint = Color(0xFFFF7A3D), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Reply", color = Color(0xFFFF7A3D), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    Column {
                        OutlinedTextField(
                            value = replyText,
                            onValueChange = { replyText = it },
                            placeholder = { Text("Write your reply...", fontSize = 14.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF1A1A2E),
                            unfocusedTextColor = Color(0xFF1A1A2E),
                            focusedBorderColor = Color(0xFFFF7A3D),
                            unfocusedBorderColor = Color(0xFFE5E7EB)
                        )
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth(), Arrangement.End, Alignment.CenterVertically) {
                            TextButton(onClick = { isReplying = false; replyText = "" }) {
                                Text("Cancel", color = Color(0xFF9CA3AF), fontSize = 12.sp)
                            }
                            Spacer(Modifier.width(8.dp))
                            Button(
                                onClick = { onResolve(fb.id, replyText); isReplying = false },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7A3D))
                            ) {
                                Text("Send & Resolve", color = Color.White, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}