package com.pruthviraj.shalenammapride.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Theme colors
private val BgDark    = Color(0xFF0F172A)
private val BgMid     = Color(0xFF1E293B)
private val Green600  = Color(0xFF16A34A)
private val Green700  = Color(0xFF15803D)
private val Green100  = Color(0xFFBBF7D0)
private val TextPrim  = Color(0xFFF1F5F9)
private val TextMuted = Color(0xFF94A3B8)
private val Border    = Color(0xFF334155)
private val Gold      = Color(0xFFFFD700)

data class StudentStar(
    val id: String = "",
    val name: String = "",
    val achievement: String = "",
    val imageUrl: String = "",
    val timestamp: Long = 0L
)

@Composable
fun StarsScreen() {
    val context = LocalContext.current
    val database = FirebaseDatabase.getInstance().reference.child("stars")

    var starsList by remember { mutableStateOf<List<StudentStar>>(emptyList()) }
    var showAddForm by remember { mutableStateOf(false) }

    // Form States
    var studentName by remember { mutableStateOf("") }
    var achievementDesc by remember { mutableStateOf("") }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    // Sync with Firebase
    LaunchedEffect(Unit) {
        database.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val list = mutableListOf<StudentStar>()
                snapshot.children.forEach { child ->
                    child.getValue(StudentStar::class.java)?.let { list.add(it) }
                }
                starsList = list.sortedByDescending { it.timestamp }
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        })
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        selectedUri = it
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
    ) {
        // --- Celebratory Header ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color(0xFF4338CA), Color(0xFF312E81))))
                .padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("PRIDE OF SCHOOL", color = Color(0xFFA5B4FC), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Student Stars", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                }
                Icon(Icons.Default.Star, contentDescription = null, tint = Gold, modifier = Modifier.size(40.dp))
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Add Button / Form ---
            item {
                if (!showAddForm) {
                    Button(
                        onClick = { showAddForm = true },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BgMid)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Green600)
                        Spacer(Modifier.width(8.dp))
                        Text("Celebrate a Student", color = TextPrim)
                    }
                } else {
                    AchievementForm(
                        name = studentName,
                        onNameChange = { studentName = it },
                        achievement = achievementDesc,
                        onAchChange = { achievementDesc = it },
                        uri = selectedUri,
                        onPickImg = { galleryLauncher.launch("image/*") },
                        isUploading = isUploading,
                        onCancel = { showAddForm = false },
                        onSave = {
                            if (selectedUri != null && studentName.isNotBlank()) {
                                isUploading = true
                                CoroutineScope(Dispatchers.IO).launch {
                                    val url = uploadToCloudinary(context, selectedUri!!)
                                    if (url != null) {
                                        val id = database.push().key ?: ""
                                        val star = StudentStar(id, studentName, achievementDesc, url, System.currentTimeMillis())
                                        database.child(id).setValue(star)
                                        withContext(Dispatchers.Main) {
                                            isUploading = false; showAddForm = false
                                            studentName = ""; achievementDesc = ""; selectedUri = null
                                        }
                                    }
                                }
                            }
                        }
                    )
                }
            }

            // --- List of Stars ---
            items(starsList) { star ->
                StarCard(star)
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun StarCard(star: StudentStar) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BgMid),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = star.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .border(2.dp, Gold, CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.width(16.dp))

            Column {
                Text(star.name, color = TextPrim, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(star.achievement, color = Gold, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(4.dp))
                Text("Awarded Achievement", color = TextMuted, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun AchievementForm(
    name: String, onNameChange: (String) -> Unit,
    achievement: String, onAchChange: (String) -> Unit,
    uri: Uri?, onPickImg: () -> Unit,
    isUploading: Boolean, onCancel: () -> Unit, onSave: () -> Unit
) {
    Surface(color = BgMid, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("New Achievement", color = TextPrim, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            // Round Image Picker for Students
            Box(
                modifier = Modifier.size(100.dp).align(Alignment.CenterHorizontally)
                    .clip(CircleShape).background(BgDark).border(1.dp, Border, CircleShape)
                    .clickable { onPickImg() },
                contentAlignment = Alignment.Center
            ) {
                if (uri != null) {
                    AsyncImage(model = uri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    Icon(Icons.Default.Person, contentDescription = null, tint = TextMuted, modifier = Modifier.size(40.dp))
                }
            }

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = name, onValueChange = onNameChange,
                label = { Text("Student Name") }, modifier = Modifier.fillMaxWidth(),
                colors = starTextFieldColors()
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = achievement, onValueChange = onAchChange,
                label = { Text("Achievement (e.g. Sports Day Winner)") }, modifier = Modifier.fillMaxWidth(),
                colors = starTextFieldColors()
            )

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                    Text("Cancel", color = TextMuted)
                }
                Button(
                    modifier = Modifier.weight(2f),
                    colors = ButtonDefaults.buttonColors(containerColor = Green600),
                    enabled = !isUploading,
                    onClick = onSave
                ) {
                    Text(if (isUploading) "Celebrating..." else "Post Achievement")
                }
            }
        }
    }
}

@Composable
fun starTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextPrim,
    unfocusedTextColor = TextPrim,
    focusedBorderColor = Color(0xFF4338CA),
    unfocusedBorderColor = Border,
    cursorColor = Color(0xFF4338CA)
)