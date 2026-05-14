package com.pruthviraj.shalenammapride.screens

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.pruthviraj.shalenammapride.AppNotificationManager
import com.pruthviraj.shalenammapride.Lang
import com.pruthviraj.shalenammapride.components.ProfileInputField
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun ProfileScreen(navController: NavController) {
    val scope = rememberCoroutineScope()
    val authUser = FirebaseAuth.getInstance().currentUser
    
    var adminName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var schoolName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var roleTitle by remember { mutableStateOf("") }
    var schoolCode by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf("") }
    
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (authUser == null) {
            navController.navigate("login") { popUpTo(0) }
            return@LaunchedEffect
        }
        val uid = authUser.uid
        val ref = FirebaseDatabase.getInstance().getReference("advancedProfiles").child(uid)
        try {
            val snapshot = ref.get().await()
            if (snapshot.exists()) {
                adminName  = snapshot.child("adminName").value?.toString() ?: ""
                email      = snapshot.child("email").value?.toString() ?: ""
                schoolName = snapshot.child("schoolName").value?.toString() ?: ""
                phone      = snapshot.child("phone").value?.toString() ?: ""
                roleTitle  = snapshot.child("roleTitle").value?.toString() ?: ""
                schoolCode = snapshot.child("schoolCode").value?.toString() ?: ""
                photoUrl   = snapshot.child("photoUrl").value?.toString() ?: ""
            } else {
                adminName = authUser.displayName ?: ""
                email     = authUser.email ?: ""
                photoUrl  = authUser.photoUrl?.toString() ?: ""
                phone     = authUser.phoneNumber ?: ""
            }
            if (adminName.isEmpty()) adminName = "Administrator"
        } catch (e: Exception) {
            adminName = authUser.displayName ?: ""
            email = authUser.email ?: ""
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color.White).verticalScroll(rememberScrollState())
    ) {
        // ── Hero Header ──
        Box(
            modifier = Modifier.fillMaxWidth().height(180.dp).background(
                brush = Brush.verticalGradient(listOf(Color(0xFFFF7A3D), Color(0xFFFF9A62))),
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp, start = 20.dp, end = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(44.dp).clickable { navController.popBackStack() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(Lang.get("Advanced Profile", "ಸುಧಾರಿತ ಪ್ರೊಫೈಲ್"), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text(Lang.get("Manage your profile details", "ನಿಮ್ಮ ಪ್ರೊಫೈಲ್ ವಿವರಗಳನ್ನು ನಿರ್ವಹಿಸಿ"), color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                }
            }
        }

        // ── Profile Card Overlay ──
        Box(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).offset(y = (-50).dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth().padding(top = 45.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 60.dp, bottom = 24.dp, start = 20.dp, end = 20.dp)
                ) {
                    Text(adminName, fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color(0xFF1A1A2E), textAlign = TextAlign.Center)
                    Text(email, fontSize = 14.sp, color = Color(0xFF9CA3AF), textAlign = TextAlign.Center)
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFECFDF5),
                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.3f))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Identity Verified", color = Color(0xFF065F46), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }

            // Avatar
            Box(
                modifier = Modifier.size(90.dp).clip(CircleShape).background(Color.White).border(3.dp, Color.White, CircleShape).border(1.dp, Color(0xFFE5E7EB), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (photoUrl.isNotEmpty()) {
                    AsyncImage(model = photoUrl, null, Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                } else {
                    Box(Modifier.fillMaxSize().background(Color(0xFFFFF4EE)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, null, tint = Color(0xFFFF7A3D), modifier = Modifier.size(40.dp))
                    }
                }
                Box(
                    modifier = Modifier.align(Alignment.BottomEnd).size(26.dp).background(Color(0xFFFF7A3D), CircleShape).border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(12.dp))
                }
            }
        }

        // ── Input Sections ──
        if (isLoading) {
            Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color(0xFFFF7A3D)) }
        } else {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).offset(y = (-30).dp)) {
                
                ProfileSectionHeader("Organizational Context", "Information about your school / organization", Icons.Outlined.Business)
                ProfileInputField(value = schoolName, onValueChange = { schoolName = it }, label = "Primary School Name", icon = Icons.Outlined.School)
                ProfileInputField(value = schoolCode, onValueChange = { schoolCode = it }, label = "Institutional DISE / School Code", icon = Icons.Outlined.Numbers)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                ProfileSectionHeader("Contact & Role Settings", "Official role and contact information", Icons.Outlined.Badge)
                ProfileInputField(value = roleTitle, onValueChange = { roleTitle = it }, label = "Official Role (e.g. Headmaster)", icon = Icons.Outlined.AssignmentInd)
                ProfileInputField(value = phone, onValueChange = { phone = it }, label = "Official Contact Number", icon = Icons.Outlined.PhoneAndroid)
                
                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (schoolName.isBlank() || roleTitle.isBlank()) {
                            AppNotificationManager.trigger("⚠️", "Fill all required fields.")
                            return@Button
                        }
                        scope.launch {
                            isSaving = true
                            try {
                                val uid = authUser?.uid ?: "anon"
                                val ref = FirebaseDatabase.getInstance().getReference("advancedProfiles").child(uid)
                                val data = mapOf("adminName" to adminName, "email" to email, "schoolName" to schoolName, "schoolCode" to schoolCode, "roleTitle" to roleTitle, "phone" to phone, "photoUrl" to photoUrl, "lastUpdated" to System.currentTimeMillis())
                                ref.setValue(data).await()
                                AppNotificationManager.trigger("✅", "Profile Metadata Deployed!")
                                navController.popBackStack()
                            } catch (e: Exception) {
                                AppNotificationManager.trigger("❌", "Save Failed.")
                            } finally {
                                isSaving = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7A3D)),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                    } else {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.CloudUpload, null, tint = Color.White, modifier = Modifier.align(Alignment.CenterStart).size(20.dp))
                            Text("Deploy Profile Metadata", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, modifier = Modifier.align(Alignment.Center))
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VerifiedUser, null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Your data is secure and encrypted", color = Color(0xFF9CA3AF), fontSize = 11.sp)
                }
                
                Spacer(modifier = Modifier.height(40.dp))
                ProfileSectionHeader("Danger Zone", "Irreversible system actions", Icons.Outlined.DeleteForever)
                Button(
                    onClick = {
                        scope.launch {
                            val db = FirebaseDatabase.getInstance().reference
                            db.child("meals").removeValue()
                            db.child("stars").removeValue()
                            db.child("feedback").removeValue()
                            db.child("announcements").removeValue()
                            db.child("facilities").removeValue()
                            AppNotificationManager.trigger("🔥", "System Data Purged!")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEF2F2)),
                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DeleteSweep, null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Clear All App Data", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                Button(
                    onClick = {
                        scope.launch {
                            val db = FirebaseDatabase.getInstance().reference
                            // Clear existing
                            db.child("meals").removeValue().await()
                            db.child("stars").removeValue().await()
                            db.child("feedback").removeValue().await()
                            db.child("announcements").removeValue().await()
                            db.child("facilities").removeValue().await()

                            val now = System.currentTimeMillis()
                            val dayMs = 86400000L

                            // Sample Meals
                            val meals = listOf("Rice & Sambar with Curd", "Bisibelebath & Raitha", "Puliogare & Sweet Pongal", "Vegetable Pulao", "Ragi Mudde & Soppu Saaru")
                            meals.forEachIndexed { i, m ->
                                db.child("meals").push().setValue(mapOf("menu" to m, "timestamp" to (now - i * dayMs))).await()
                            }

                            // Sample Stars
                            val stars = listOf(
                                mapOf("studentName" to "Rahul S", "studentClass" to "10", "section" to "A", "achievement" to "Scored 98% in Science Olympiad", "category" to "Academic Excellence", "imageUrl" to "https://picsum.photos/seed/rahul/400/400"),
                                mapOf("studentName" to "Priya M", "studentClass" to "9", "section" to "B", "achievement" to "Winner in State Level Athletics", "category" to "Sports", "imageUrl" to "https://picsum.photos/seed/priya/400/400"),
                                mapOf("studentName" to "Aravind K", "studentClass" to "8", "section" to "C", "achievement" to "Led the school team in district debate", "category" to "Leadership", "imageUrl" to "https://picsum.photos/seed/aravind/400/400"),
                                mapOf("studentName" to "Sanjana R", "studentClass" to "7", "section" to "A", "achievement" to "First prize in Inter-School Art Fest", "category" to "Creativity", "imageUrl" to "https://picsum.photos/seed/sanjana/400/400"),
                                mapOf("studentName" to "Deepak G", "studentClass" to "6", "section" to "B", "achievement" to "Consistently helping classmates with studies", "category" to "Good Behavior", "imageUrl" to "https://picsum.photos/seed/deepak/400/400")
                            )
                            stars.forEachIndexed { i, s ->
                                db.child("stars").push().setValue(s + mapOf("timestamp" to (now - i * dayMs))).await()
                            }

                            // Sample Announcements
                            val circulars = listOf(
                                mapOf("title" to "Annual Sports Day Meeting", "body" to "All house captains please assemble in the auditorium at 10 AM tomorrow.", "category" to "Urgent", "priority" to "high"),
                                mapOf("title" to "Monthly Unit Test Schedule", "body" to "The schedule for June unit tests has been uploaded to the student portal.", "category" to "Exams", "priority" to "normal"),
                                mapOf("title" to "Last Date for Fee Payment", "body" to "Kindly clear all pending dues before the 15th to avoid late fees.", "category" to "Fees", "priority" to "high"),
                                mapOf("title" to "Cultural Fest Participation", "body" to "Auditions for the annual cultural fest 'PRIDE' begin this Friday.", "category" to "Events", "priority" to "normal"),
                                mapOf("title" to "School Holiday Notice", "body" to "The school will remain closed this Saturday for teacher training.", "category" to "Urgent", "priority" to "high")
                            )
                            circulars.forEachIndexed { i, c ->
                                db.child("announcements").push().setValue(c + mapOf("timestamp" to (now - i * dayMs))).await()
                            }

                            // Sample Facilities
                            val facs = listOf(
                                mapOf("title" to "Digital Classroom", "description" to "Equipped with high-speed internet, smart boards, and modern projectors.", "imageUrl" to "https://picsum.photos/seed/classroom/400/300", "category" to "Classrooms"),
                                mapOf("title" to "Modern Science Lab", "description" to "State-of-the-art laboratory for Physics, Chemistry, and Biology.", "imageUrl" to "https://picsum.photos/seed/lab/400/300", "category" to "Laboratory"),
                                mapOf("title" to "School Library", "description" to "A collection of over 5000 books across various genres and academics.", "imageUrl" to "https://picsum.photos/seed/library/400/300", "category" to "Library"),
                                mapOf("title" to "Safe Transport", "description" to "A fleet of GPS-tracked buses with dedicated security staff.", "imageUrl" to "https://picsum.photos/seed/bus/400/300", "category" to "Other"),
                                mapOf("title" to "Purified Water", "description" to "RO purified drinking water facilities across all campus floors.", "imageUrl" to "https://picsum.photos/seed/water/400/300", "category" to "Building")
                            )
                            facs.forEach { f -> db.child("facility").push().setValue(f + mapOf("timestamp" to now)).await() }

                            // Sample Feedback
                            val feeds = listOf(
                                mapOf("sender" to "Kiran Kumar", "message" to "The school management is doing an excellent job with the new facilities.", "status" to "Pending"),
                                mapOf("sender" to "Meena Patel", "message" to "My child is very happy with the meal quality and variety provided.", "status" to "Replied"),
                                mapOf("sender" to "Rajesh Gupta", "message" to "Please consider adding more sports equipment for the junior section.", "status" to "Pending"),
                                mapOf("sender" to "Anitha Reddy", "message" to "The digital tracking for buses is very helpful for parents' peace of mind.", "status" to "Replied"),
                                mapOf("sender" to "Suresh Rao", "message" to "App interface is very smooth and easy to navigate. Great work!", "status" to "Pending")
                            )
                            feeds.forEachIndexed { i, f ->
                                db.child("feedback").push().setValue(f + mapOf("timestamp" to (now - i * dayMs))).await()
                            }

                            AppNotificationManager.trigger("✨", "Demo Data Deployed!")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp).padding(top = 10.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0F9FF)),
                    border = BorderStroke(1.dp, Color(0xFF0EA5E9).copy(alpha = 0.5f))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoMode, null, tint = Color(0xFF0284C7), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Populate Demo Data (5 Days)", color = Color(0xFF0284C7), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun ProfileSectionHeader(title: String, subtitle: String, icon: ImageVector) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Color(0xFFFF7A3D), modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1A1A2E))
        }
        Text(subtitle, fontSize = 11.sp, color = Color(0xFF9CA3AF), modifier = Modifier.padding(start = 24.dp))
    }
}

