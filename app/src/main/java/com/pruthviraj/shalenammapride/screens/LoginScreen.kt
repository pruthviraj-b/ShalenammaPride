package com.pruthviraj.shalenammapride.screens

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clip
import androidx.navigation.NavController
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.pruthviraj.shalenammapride.AppNotificationManager
import java.util.concurrent.TimeUnit
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.GoogleAuthProvider

// Premium White Theme Colors - Refactored to MaterialTheme 3

@Composable
fun LoginScreen(navController: NavController) {
    // Taj Mahal Theme Tokens
    val BgDark = MaterialTheme.colorScheme.background
    val BgMid = MaterialTheme.colorScheme.surface
    val PrimaryDark = MaterialTheme.colorScheme.primary
    val TextPrim = MaterialTheme.colorScheme.onBackground
    val TextMuted = MaterialTheme.colorScheme.onSurfaceVariant
    val Border = MaterialTheme.colorScheme.outline

    var isPhoneLogin by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }
    
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current as Activity

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(com.pruthviraj.shalenammapride.R.string.default_web_client_id))
        .requestEmail()
        .build()
    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    val googleSignInLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
            statusMessage = "Authenticating with Google..."
            auth.signInWithCredential(credential).addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    AppNotificationManager.trigger("✅", "Google Sign-In Successful!")
                    navController.navigate("dashboard") { popUpTo("login") { inclusive = true } }
                } else {
                    val msg = authTask.exception?.message ?: "Sign-in failed"
                    statusMessage = "❌ $msg"
                    AppNotificationManager.trigger("❌", msg)
                }
            }
        } catch (e: com.google.android.gms.common.api.ApiException) {
            val msg = "Google API Error: ${e.statusCode}"
            statusMessage = "❌ $msg"
            AppNotificationManager.trigger("⚠️", msg)
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: "Unknown Error"
            statusMessage = "❌ Error: $msg"
            AppNotificationManager.trigger("❌", msg)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primaryContainer
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = Color.White.copy(alpha = 0.15f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .shadow(20.dp, RoundedCornerShape(32.dp))
        ) {
            Column(
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.1f))
                    .padding(32.dp)
            ) {
                Text(
                    "SHALE-NAMMA PRIDE", 
                    color = Color.White.copy(alpha = 0.7f), 
                    fontSize = 14.sp, 
                    fontWeight = FontWeight.Bold, 
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Admin Portal", 
                    color = Color.White, 
                    fontSize = 32.sp, 
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    letterSpacing = (-1).sp
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Toggle between Email and Phone
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    TabButton(text = "Email", selected = !isPhoneLogin) { 
                        isPhoneLogin = false 
                        statusMessage = ""
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    TabButton(text = "Phone", selected = isPhoneLogin) { 
                        isPhoneLogin = true 
                        statusMessage = ""
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))

                if (isPhoneLogin) {
                    PhoneLoginSection(auth, navController) { statusMessage = it }
                } else {
                    EmailLoginSection(auth, navController) { statusMessage = it }
                }

                if (statusMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = statusMessage,
                        color = if (statusMessage.startsWith("❌")) Color(0xFFDC2626) else PrimaryDark,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                Divider(color = Border, thickness = 1.dp)
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedButton(
                    onClick = { googleSignInLauncher.launch(googleSignInClient.signInIntent) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Border)
                ) {
                    Text("Continue with Google", color = TextPrim, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun TabButton(text: String, selected: Boolean, onClick: () -> Unit) {
    val PrimaryDark = MaterialTheme.colorScheme.primary
    val TextMuted = MaterialTheme.colorScheme.onSurfaceVariant
    val Border = MaterialTheme.colorScheme.outline

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = if (selected) Color.White else Color.White.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) Color.White else Color.White.copy(alpha = 0.2f)),
        modifier = Modifier.padding(2.dp),
        onClick = onClick
    ) {
        Text(
            text, 
            color = if (selected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.7f),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
        )
    }
}

@Composable
fun EmailLoginSection(auth: FirebaseAuth, navController: NavController, onStatusUpdate: (String) -> Unit) {
    val PrimaryDark = MaterialTheme.colorScheme.primary
    val TextPrim = MaterialTheme.colorScheme.onBackground
    val TextMuted = MaterialTheme.colorScheme.onSurfaceVariant
    val Border = MaterialTheme.colorScheme.outline

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isRegistering by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = email,
        onValueChange = { email = it },
        label = { Text("Email", color = Color.White.copy(alpha = 0.7f)) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = Color.White,
            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
            cursorColor = Color.White
        )
    )
    Spacer(modifier = Modifier.height(16.dp))
    OutlinedTextField(
        value = password,
        onValueChange = { password = it },
        label = { Text("Password", color = Color.White.copy(alpha = 0.7f)) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = Color.White,
            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
            cursorColor = Color.White
        )
    )
    Spacer(modifier = Modifier.height(16.dp))
    
    Button(
        onClick = {
            if (email.isBlank() || password.isBlank()) {
                onStatusUpdate("⚠️ Enter email and password")
                return@Button
            }
            isLoading = true
            
            if (isRegistering) {
                onStatusUpdate("Creating account...")
                auth.createUserWithEmailAndPassword(email.trim(), password)
                    .addOnCompleteListener { task ->
                        isLoading = false
                        if (task.isSuccessful) {
                            AppNotificationManager.trigger("🎉", "Account created successfully!")
                            navController.navigate("dashboard") {
                                popUpTo("login") { inclusive = true }
                            }
                        } else {
                            val msg = task.exception?.message ?: "Registration failed"
                            onStatusUpdate("❌ $msg")
                            AppNotificationManager.trigger("❌", msg)
                        }
                    }
            } else {
                onStatusUpdate("Logging in...")
                auth.signInWithEmailAndPassword(email.trim(), password)
                    .addOnCompleteListener { task ->
                        isLoading = false
                        if (task.isSuccessful) {
                            AppNotificationManager.trigger("✅", "Login Successful!")
                            navController.navigate("dashboard") {
                                popUpTo("login") { inclusive = true }
                            }
                        } else {
                            val msg = task.exception?.message ?: "Login failed"
                            onStatusUpdate("❌ $msg")
                            AppNotificationManager.trigger("❌", msg)
                        }
                    }
            }
        },
        modifier = Modifier.fillMaxWidth().height(60.dp),
        enabled = !isLoading,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = MaterialTheme.colorScheme.primary
        ),
        elevation = ButtonDefaults.buttonElevation(8.dp)
    ) {
        Text(if (isLoading) "Please wait..." else if (isRegistering) "Register" else "Sign In", fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }

    Spacer(modifier = Modifier.height(8.dp))
    TextButton(
        onClick = { isRegistering = !isRegistering },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            if (isRegistering) "Already have an account? Sign In" else "Don't have an account? Register",
            color = Color.White,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun PhoneLoginSection(auth: FirebaseAuth, navController: NavController, onStatusUpdate: (String) -> Unit) {
    val PrimaryDark = MaterialTheme.colorScheme.primary
    val TextPrim = MaterialTheme.colorScheme.onBackground
    val TextMuted = MaterialTheme.colorScheme.onSurfaceVariant
    val Border = MaterialTheme.colorScheme.outline

    val context = LocalContext.current as Activity
    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var verificationIdState by remember { mutableStateOf("") }
    var isCodeSent by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    if (!isCodeSent) {
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Phone Number", color = Color.White.copy(alpha = 0.7f)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                cursorColor = Color.White
            )
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                if (phoneNumber.isBlank()) {
                    onStatusUpdate("⚠️ Enter phone number")
                    return@Button
                }
                
                // Auto-add '+' if the user forgot it
                val finalNumber = if (!phoneNumber.trim().startsWith("+")) "+${phoneNumber.trim()}" else phoneNumber.trim()
                
                isLoading = true
                onStatusUpdate("Sending OTP to $finalNumber...")
                
                val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                        signInWithPhoneAuthCredential(auth, credential, navController, onStatusUpdate)
                    }

                    override fun onVerificationFailed(e: FirebaseException) {
                        isLoading = false
                        val msg = e.message ?: "Verification failed"
                        onStatusUpdate("❌ Verification failed: $msg")
                        AppNotificationManager.trigger("❌", "Verification Failed")
                    }

                    override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                        isLoading = false
                        verificationIdState = verificationId
                        isCodeSent = true
                        onStatusUpdate("✅ OTP Sent")
                        AppNotificationManager.trigger("📩", "OTP Sent to phone")
                    }
                }

                val options = PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(finalNumber)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(context)
                    .setCallbacks(callbacks)
                    .build()
                PhoneAuthProvider.verifyPhoneNumber(options)
            },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            enabled = !isLoading,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = MaterialTheme.colorScheme.primary
            ),
            elevation = ButtonDefaults.buttonElevation(8.dp)
        ) {
            Text(if (isLoading) "Sending..." else "Send OTP", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    } else {
        OutlinedTextField(
            value = otpCode,
            onValueChange = { otpCode = it },
            label = { Text("Enter 6-digit OTP", color = Color.White.copy(alpha = 0.7f)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                cursorColor = Color.White
            )
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                if (otpCode.isBlank()) {
                    onStatusUpdate("⚠️ Enter OTP")
                    return@Button
                }
                isLoading = true
                onStatusUpdate("Verifying OTP...")
                val credential = PhoneAuthProvider.getCredential(verificationIdState, otpCode)
                signInWithPhoneAuthCredential(auth, credential, navController, onStatusUpdate)
            },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            enabled = !isLoading,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = MaterialTheme.colorScheme.primary
            ),
            elevation = ButtonDefaults.buttonElevation(8.dp)
        ) {
            Text(if (isLoading) "Verifying..." else "Verify OTP", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}

private fun signInWithPhoneAuthCredential(
    auth: FirebaseAuth, 
    credential: PhoneAuthCredential, 
    navController: NavController,
    onStatusUpdate: (String) -> Unit
) {
    auth.signInWithCredential(credential)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                AppNotificationManager.trigger("✅", "Login Successful!")
                navController.navigate("dashboard") {
                    popUpTo("login") { inclusive = true }
                }
            } else {
                val msg = task.exception?.message ?: "Login failed"
                onStatusUpdate("❌ $msg")
                AppNotificationManager.trigger("❌", msg)
            }
        }
}
