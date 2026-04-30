package com.pruthviraj.shalenammapride.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.foundation.clickable

// Reusing your established theme colors
private val BgDark    = Color(0xFF0F172A)
private val BgMid     = Color(0xFF1E293B)
private val Green600  = Color(0xFF16A34A)
private val Green700  = Color(0xFF15803D)
private val Green100  = Color(0xFFBBF7D0)
private val TextPrim  = Color(0xFFF1F5F9)
private val TextMuted = Color(0xFF94A3B8)
private val Border    = Color(0xFF334155)

data class FacilityItem(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String = ""
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FacilityScreen() {
    val context = LocalContext.current
    val database = FirebaseDatabase.getInstance().reference.child("facilities")

    var facilities by remember { mutableStateOf<List<FacilityItem>>(emptyList()) }
    var showAddForm by remember { mutableStateOf(false) }

    // Form States
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    // Sync with Firebase
    LaunchedEffect(Unit) {
        database.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val list = mutableListOf<FacilityItem>()
                snapshot.children.forEach { child ->
                    child.getValue(FacilityItem::class.java)?.let { list.add(it) }
                }
                facilities = list
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
            .verticalScroll(rememberScrollState())
    ) {
        // --- Header Section ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Green600, Green700)))
                .padding(24.dp)
        ) {
            Column {
                Text("SCHOOL TOUR", color = Green100, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("Campus Facilities", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            }
        }

        // --- Swipeable Gallery (The "Tour") ---
        if (facilities.isNotEmpty()) {
            val pagerState = rememberPagerState(pageCount = { facilities.size })

            Column(modifier = Modifier.padding(vertical = 20.dp)) {
                HorizontalPager(
                    state = pagerState,
                    contentPadding = PaddingValues(horizontal = 32.dp),
                    pageSpacing = 16.dp,
                    modifier = Modifier.fillMaxWidth()
                ) { page ->
                    FacilityCard(facilities[page])
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Custom Pager Indicator
                Row(
                    Modifier.fillMaxWidth().height(8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(facilities.size) { iteration ->
                        val color = if (pagerState.currentPage == iteration) Green600 else Border
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .clip(CircleShape)
                                .background(color)
                                .size(if (pagerState.currentPage == iteration) 10.dp else 8.dp)
                        )
                    }
                }
            }
        } else {
            Box(Modifier.fillMaxWidth().height(250.dp), contentAlignment = Alignment.Center) {
                Text("No facilities added yet.", color = TextMuted)
            }
        }

        // --- Add New Facility Section ---
        Column(modifier = Modifier.padding(16.dp)) {
            if (!showAddForm) {
                Button(
                    onClick = { showAddForm = true },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BgMid)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Green600)
                    Spacer(Modifier.width(8.dp))
                    Text("Register New Facility", color = TextPrim)
                }
            } else {
                // Form UI (Matching your MealScreen style)
                Surface(color = BgMid, shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Add Facility Details", color = TextPrim, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))

                        FacilityImagePicker(selectedUri) { galleryLauncher.launch("image/*") }

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Title (e.g. Science Lab)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = textFieldColors()
                        )

                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = desc,
                            onValueChange = { desc = it },
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = textFieldColors()
                        )

                        Spacer(Modifier.height(16.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { showAddForm = false }, modifier = Modifier.weight(1f)) {
                                Text("Cancel", color = TextMuted)
                            }
                            Button(
                                modifier = Modifier.weight(2f),
                                colors = ButtonDefaults.buttonColors(containerColor = Green600),
                                enabled = !isUploading,
                                onClick = {
                                    if (selectedUri != null && title.isNotBlank()) {
                                        isUploading = true
                                        CoroutineScope(Dispatchers.IO).launch {
                                            val url = uploadToCloudinary(context, selectedUri!!)
                                            if (url != null) {
                                                val id = database.push().key ?: ""
                                                val item = FacilityItem(id, title, desc, url)
                                                database.child(id).setValue(item)
                                                withContext(Dispatchers.Main) {
                                                    isUploading = false
                                                    showAddForm = false
                                                    title = ""; desc = ""; selectedUri = null
                                                }
                                            }
                                        }
                                    }
                                }
                            ) {
                                Text(if (isUploading) "Uploading..." else "Save Facility")
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun FacilityCard(item: FacilityItem) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = BgMid),
        modifier = Modifier.fillMaxWidth().height(380.dp)
    ) {
        Column {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(240.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(20.dp)) {
                Text(item.title, color = TextPrim, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(item.description, color = TextMuted, fontSize = 14.sp, lineHeight = 20.sp)
            }
        }
    }
}

@Composable
fun FacilityImagePicker(uri: Uri?, onPick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(BgDark)
            .border(1.dp, Border, RoundedCornerShape(12.dp))
            .clickable { onPick() },
        contentAlignment = Alignment.Center
    ) {
        if (uri != null) {
            AsyncImage(model = uri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Green600)
                Text("Upload Photo", color = TextMuted, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextPrim,
    unfocusedTextColor = TextPrim,
    focusedBorderColor = Green600,
    unfocusedBorderColor = Border,
    cursorColor = Green600
)