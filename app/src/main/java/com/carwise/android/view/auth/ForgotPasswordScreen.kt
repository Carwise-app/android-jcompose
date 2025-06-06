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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.carwise.android.R
import com.carwise.android.navigation.Screen
import com.carwise.android.ui.theme.appRed
import com.carwise.android.viewmodel.AuthViewModel
import java.util.regex.Pattern

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var isEmailError by remember { mutableStateOf(false) }
    val authState by authViewModel.authState.collectAsState()
    val context = LocalContext.current

    // Email validation pattern
    val emailPattern = Pattern.compile(
        "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
        Pattern.CASE_INSENSITIVE
    )

    fun validateEmail(email: String): Boolean {
        return emailPattern.matcher(email).matches()
    }

    fun handleResetPassword() {
        when {
            email.isBlank() -> {
                isEmailError = true
            }
            !validateEmail(email) -> {
                isEmailError = true
            }
            else -> {
                isEmailError = false
                authViewModel.forgotPassword(email)
            }
        }
    }

    // Handle error state
    LaunchedEffect(authState.error) {
        authState.error?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        }
    }

    // Show success dialog when OTP is sent
    if (authState.isSendingOtp) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Text(
                    "E-posta Gönderildi",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                Text(
                    "Şifre sıfırlama bağlantısı e-posta adresinize gönderildi. Lütfen e-postanızı kontrol edin.",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.ForgotPassword.route) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = appRed
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Tamam")
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
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
                text = "Şifremi Unuttum",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                ),
                color = Color(0xFF1A1A1A),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Şifrenizi sıfırlamak için e-posta adresinizi girin",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF666666),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = { 
                    email = it
                    isEmailError = false
                },
                label = { Text("E-posta") },
                singleLine = true,
                isError = isEmailError,
                supportingText = {
                    if (isEmailError) {
                        Text(
                            text = "Geçerli bir e-posta adresi girin",
                            color = Color(0xFFE30613)
                        )
                    }
                },
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
                    keyboardType = KeyboardType.Email,
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
                enabled = !authState.isLoading && email.isNotBlank(),
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
                        "Şifremi Sıfırla",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            // Back to Login Button
            TextButton(
                onClick = { navController.navigateUp() },
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