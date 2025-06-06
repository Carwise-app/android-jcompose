package com.carwise.android.view.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.carwise.android.R
import com.carwise.android.navigation.Screen
import com.carwise.android.ui.theme.appRed
import com.carwise.android.viewmodel.AuthViewModel
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(
    navController: NavController,
    token: String,
    email: String,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordError by remember { mutableStateOf(false) }
    var isConfirmPasswordError by remember { mutableStateOf(false) }
    val authState by authViewModel.authState.collectAsState()
    val context = LocalContext.current

    // Handle error state
    LaunchedEffect(authState.error) {
        authState.error?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        }
    }

    // Navigate to login on success
    LaunchedEffect(authState.isAuthenticated) {
        if (authState.isAuthenticated) {
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.ResetPassword.route) { inclusive = true }
            }
        }
    }

    fun validatePasswords(): Boolean {
        var isValid = true

        if (password.length < 6) {
            isPasswordError = true
            isValid = false
        } else {
            isPasswordError = false
        }

        if (password != confirmPassword) {
            isConfirmPasswordError = true
            isValid = false
        } else {
            isConfirmPasswordError = false
        }

        return isValid
    }

    fun handleResetPassword() {
        if (validatePasswords() && token.isNotEmpty() && email.isNotEmpty()) {
            authViewModel.resetPassword(email, token, password)
        } else {
            Toast.makeText(context, "Geçersiz token veya e-posta", Toast.LENGTH_LONG).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(70.dp))
            
            // Logo
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Carwise Logo",
                modifier = Modifier
                    .size(60.dp)
                    .padding(bottom = 24.dp),
                colorFilter = ColorFilter.tint(appRed)
            )

            Text(
                text = "Şifre Sıfırlama",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                ),
                color = Color(0xFF1A1A1A),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Yeni şifrenizi belirleyin",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF666666),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { 
                    password = it
                    isPasswordError = false
                },
                label = { Text("Yeni Şifre") },
                singleLine = true,
                isError = isPasswordError,
                supportingText = {
                    if (isPasswordError) {
                        Text(
                            text = "Şifre en az 6 karakter olmalıdır",
                            color = Color(0xFFE30613)
                        )
                    }
                },
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = appRed,
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedLabelColor = appRed,
                    cursorColor = appRed,
                    errorBorderColor = Color(0xFFE30613),
                    errorLabelColor = Color(0xFFE30613)
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            // Confirm Password Field
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { 
                    confirmPassword = it
                    isConfirmPasswordError = false
                },
                label = { Text("Şifre Tekrar") },
                singleLine = true,
                isError = isConfirmPasswordError,
                supportingText = {
                    if (isConfirmPasswordError) {
                        Text(
                            text = "Şifreler eşleşmiyor",
                            color = Color(0xFFE30613)
                        )
                    }
                },
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = appRed,
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedLabelColor = appRed,
                    cursorColor = appRed,
                    errorBorderColor = Color(0xFFE30613),
                    errorLabelColor = Color(0xFFE30613)
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )

            // Reset Password Button
            Button(
                onClick = { handleResetPassword() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp)),
                enabled = !authState.isLoading && password.isNotBlank() && confirmPassword.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = appRed,
                    disabledContainerColor = appRed.copy(alpha = 0.6f)
                )
            ) {
                if (authState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Şifremi Güncelle",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            // Back to Login Button
            TextButton(
                onClick = { navController.navigate(Screen.Login.route) },
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                Text(
                    "Giriş ekranına dön",
                    color = appRed,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

private fun extractToken(uri: String): String? {
    return try {
        val decodedUri = URLDecoder.decode(uri, StandardCharsets.UTF_8.toString())
        val tokenParam = decodedUri.split("token=").getOrNull(1)?.split("&")?.firstOrNull()
        tokenParam
    } catch (e: Exception) {
        null
    }
}

private fun extractEmail(uri: String): String? {
    return try {
        val decodedUri = URLDecoder.decode(uri, StandardCharsets.UTF_8.toString())
        val emailParam = decodedUri.split("email=").getOrNull(1)
        emailParam
    } catch (e: Exception) {
        null
    }
} 