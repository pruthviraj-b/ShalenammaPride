package com.pruthviraj.shalenammapride.screens

import com.pruthviraj.shalenammapride.Lang
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import coil.compose.AsyncImage
import com.pruthviraj.shalenammapride.AppNotificationManager
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Premium White Theme Colors
private val BgDark    = Color(0xFFF9FAFB) // Light gray background
private val BgMid     = Color(0xFFFFFFFF) // White cards
private val PrimaryDark = Color(0xFF111827) // Dark text & buttons
private val TagBg     = Color(0xFFF3F4F6) // Light gray for tags
private val TextPrim  = Color(0xFF111827)
private val TextMuted = Color(0xFF6B7280)
private val Border    = Color(0xFFE5E7EB) // Soft gray borders

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

    val database = FirebaseDatabase.getInstance().reference

    // Real-time listener for the Inbox
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
                            "ADMIN INBOX",
                            color = TextMuted,
                            fontSize = 12.sp,
                            letterSpacing = 1.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            Lang.get("Parent Feedback", "ಪೋಷಕರ ಪ್ರತಿಕ್ರಿಯೆ"),
                            color = PrimaryDark,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                val pendingCount = feedbackList.count { !it.resolved }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (pendingCount > 0) Color(0xFFFEE2E2) else TagBg,
                ) {
                    Text(
                        "$pendingCount Pending",
                        color = if (pendingCount > 0) Color(0xFF991B1B) else TextMuted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }



        // Filter Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Show Resolved", color = TextMuted, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = filterResolved,
                onCheckedChange = { filterResolved = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = PrimaryDark,
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = TagBg
                ),
                modifier = Modifier.scale(0.8f)
            )
        }

        val filteredList = if (filterResolved) feedbackList else feedbackList.filter { !it.resolved }

        if (filteredList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Inbox is clear! ✨", fontSize = 20.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No pending feedback from parents.", fontSize = 14.sp, color = TextMuted)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredList) { fb ->
                    FeedbackCard(fb, onResolve = { id, reply ->
                        val updates = mutableMapOf<String, Any>("resolved" to true)
                        if (reply.isNotBlank()) {
                            updates["adminReply"] = reply
                            updates["replyTimestamp"] = System.currentTimeMillis()
                        }
                        database.child("feedback").child(id).updateChildren(updates).addOnSuccessListener {
                            if (reply.isNotBlank()) {
                                AppNotificationManager.trigger("📩", "Reply Sent & Feedback Resolved!")
                            } else {
                                AppNotificationManager.trigger("✅", "Feedback Marked as Resolved!")
                            }
                        }
                    })
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun FeedbackCard(fb: FeedbackEntry, onResolve: (String, String) -> Unit) {
    var isReplying by remember { mutableStateOf(false) }
    var replyText by remember { mutableStateOf("") }
    
    val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        .format(Date(fb.timestamp))

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = BgMid,
        border = androidx.compose.foundation.BorderStroke(1.dp, Border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                if (fb.resolved) Color(0xFFD1FAE5) else if (fb.anonymous) TagBg else PrimaryDark,
                                RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) { Text(if (fb.resolved) "✓" else if (fb.anonymous) "🔒" else "👤", fontSize = 18.sp, color = if(fb.resolved) Color(0xFF065F46) else Color.White) }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            if (fb.anonymous) "Anonymous" else fb.name,
                            color = TextPrim,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(dateStr, color = TextMuted, fontSize = 12.sp)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (fb.resolved) Color(0xFFD1FAE5) else if (fb.anonymous) TagBg else PrimaryDark.copy(alpha = 0.05f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (fb.resolved) Color(0xFF10B981) else if (fb.anonymous) Border else PrimaryDark.copy(alpha = 0.2f))
                ) {
                    Text(
                        if (fb.resolved) "Resolved" else if (fb.anonymous) "Anonymous" else "Named",
                        color = if (fb.resolved) Color(0xFF065F46) else if (fb.anonymous) TextMuted else PrimaryDark,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                fb.message,
                color = TextPrim,
                fontSize = 15.sp,
                lineHeight = 22.sp
            )
            
            if (fb.imageUrl.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                
                val imageModel: Any = if (fb.imageUrl.startsWith("data:image")) {
                    try {
                        android.util.Base64.decode(fb.imageUrl.substringAfter("base64,"), android.util.Base64.DEFAULT)
                    } catch (e: Exception) {
                        fb.imageUrl
                    }
                } else {
                    fb.imageUrl
                }
                
                AsyncImage(
                    model = imageModel,
                    contentDescription = "Attached photo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            // Show Admin Reply if it exists
            if (fb.adminReply.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = TagBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Admin Reply:", color = PrimaryDark, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(fb.adminReply, color = TextPrim, fontSize = 14.sp)
                    }
                }
            }

            if (!fb.resolved && !isReplying) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Border, thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { onResolve(fb.id, "") }) {
                        Text("Just Mark Resolved", color = TextMuted, fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { isReplying = true },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Reply", color = Color.White, fontSize = 14.sp)
                    }
                }
            } else if (!fb.resolved && isReplying) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    label = { Text("Write your reply...", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrim, unfocusedTextColor = TextPrim,
                        focusedBorderColor = PrimaryDark, unfocusedBorderColor = Border
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { isReplying = false; replyText = "" }) {
                        Text("Cancel", color = TextMuted)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onResolve(fb.id, replyText); isReplying = false },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Send & Resolve", color = Color.White, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}