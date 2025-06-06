package com.carwise.android.view.auth

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.carwise.android.R
import com.carwise.android.navigation.Screen
import com.carwise.android.ui.theme.appRed
import com.carwise.android.viewmodel.AuthViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.regex.Pattern

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
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

    fun handleLogin() {
        when {
            email.isBlank() -> {
                isEmailError = true
            }
            !validateEmail(email) -> {
                isEmailError = true
            }
            password.isBlank() -> {
                Toast.makeText(context, "Lütfen şifrenizi girin", Toast.LENGTH_SHORT).show()
            }
            else -> {
                isEmailError = false
                authViewModel.login(email, password)
            }
        }
    }

    LaunchedEffect(authState.isAuthenticated) {
        if (authState.isAuthenticated) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }

    LaunchedEffect(authState.error) {
        if (authState.error != null) {
            authState.error?.let { error ->
                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            }
        }

    }

    val scope = rememberCoroutineScope()
    val credentialManager = CredentialManager.create(context)
    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(stringResource(id = R.string.google_client_id))
        .setNonce(UUID.randomUUID().toString())
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    fun googleLogin() {
        scope.launch {
            try {
                val result = credentialManager.getCredential(
                    context = context,
                    request = request
                )

                val credential = result.credential
                when (credential) {
                    is CustomCredential -> {
                        if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                            try {
                                val googleIdTokenCredential = GoogleIdTokenCredential
                                    .createFrom(credential.data)
                                val googleIdToken = googleIdTokenCredential.idToken
                                authViewModel.loginWithGoogle(googleIdToken)
                            } catch (e: GoogleIdTokenParsingException) {
                                Log.e("GOOGLE LOGIN", "Received an invalid google id token response", e)
                            }
                        } else {
                            Log.e("GOOGLE LOGIN", "Unexpected type of credential")
                        }
                    }
                }

            } catch (e : Exception) {
                e.printStackTrace()
            }
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
                text = "Hoş Geldiniz",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                ),
                color = Color(0xFF1A1A1A),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Devam etmek için giriş yapın",
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
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Şifre") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = appRed,
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedLabelColor = appRed,
                    cursorColor = appRed
                ),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 2.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 2.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { navController.navigate(Screen.ForgotPassword.route) }
                ) {
                    Text(
                        "Şifremi Unuttum",
                        color = Color(0xFF666666),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Login Button
            Button(
                onClick = { handleLogin() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp)),
                enabled = !authState.isLoading && email.isNotBlank() && password.isNotBlank(),
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
                        "Giriş Yap",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            // Divider
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Divider(
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFE0E0E0)
                )
                Text(
                    text = "veya",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF666666)
                )
                Divider(
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFE0E0E0)
                )
            }

            // Google Sign In Button
            OutlinedButton(
                onClick = { googleLogin() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF1A1A1A)
                ),
                border = BorderStroke(1.dp, Color(0xFFE0E0E0))
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (authState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.LightGray,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.google_logo),
                            contentDescription = "Google Logo",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Google ile devam et",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }

           Spacer(Modifier.weight(1f))
            TextButton(
                onClick = { navController.navigate(Screen.Register.route) },
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                Text(
                    "Hesabınız yok mu? Kayıt olun",
                    color = appRed,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

        }
    }
} 