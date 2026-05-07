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

private val BgDark    = Color(0xFFF9FAFB)
private val BgMid     = Color(0xFFFFFFFF)
private val PrimaryDark = Color(0xFF111827)
private val TextPrim  = Color(0xFF111827)
private val TextMuted = Color(0xFF6B7280)
private val Border    = Color(0xFFE5E7EB)

@Composable
fun LoginScreen(navController: NavController) {
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = BgMid,
            border = androidx.compose.foundation.BorderStroke(1.dp, Border),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    "SHALE-NAMMA PRIDE", 
                    color = TextMuted, 
                    fontSize = 12.sp, 
                    fontWeight = FontWeight.Medium, 
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Admin Access", 
                    color = TextPrim, 
                    fontSize = 26.sp, 
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
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
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (selected) PrimaryDark else Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) PrimaryDark else Border),
        modifier = Modifier.padding(2.dp),
        onClick = onClick
    ) {
        Text(
            text, 
            color = if (selected) Color.White else TextMuted,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun EmailLoginSection(auth: FirebaseAuth, navController: NavController, onStatusUpdate: (String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isRegistering by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = email,
        onValueChange = { email = it },
        label = { Text("Email", color = TextMuted) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextPrim, unfocusedTextColor = TextPrim,
            focusedBorderColor = PrimaryDark, unfocusedBorderColor = Border
        )
    )
    Spacer(modifier = Modifier.height(12.dp))
    OutlinedTextField(
        value = password,
        onValueChange = { password = it },
        label = { Text("Password", color = TextMuted) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextPrim, unfocusedTextColor = TextPrim,
            focusedBorderColor = PrimaryDark, unfocusedBorderColor = Border
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
        modifier = Modifier.fillMaxWidth().height(50.dp),
        enabled = !isLoading,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark)
    ) {
        Text(if (isLoading) "Please wait..." else if (isRegistering) "Register" else "Sign In", color = Color.White, fontWeight = FontWeight.SemiBold)
    }

    Spacer(modifier = Modifier.height(8.dp))
    TextButton(
        onClick = { isRegistering = !isRegistering },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            if (isRegistering) "Already have an account? Sign In" else "Don't have an account? Register",
            color = PrimaryDark,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun PhoneLoginSection(auth: FirebaseAuth, navController: NavController, onStatusUpdate: (String) -> Unit) {
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
            label = { Text("Phone Number (with country code, e.g. +91)", color = TextMuted) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrim, unfocusedTextColor = TextPrim,
                focusedBorderColor = PrimaryDark, unfocusedBorderColor = Border
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
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = !isLoading,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark)
        ) {
            Text(if (isLoading) "Sending..." else "Send OTP", color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    } else {
        OutlinedTextField(
            value = otpCode,
            onValueChange = { otpCode = it },
            label = { Text("Enter 6-digit OTP", color = TextMuted) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrim, unfocusedTextColor = TextPrim,
                focusedBorderColor = PrimaryDark, unfocusedBorderColor = Border
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
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = !isLoading,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark)
        ) {
            Text(if (isLoading) "Verifying..." else "Verify OTP", color = Color.White, fontWeight = FontWeight.SemiBold)
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
