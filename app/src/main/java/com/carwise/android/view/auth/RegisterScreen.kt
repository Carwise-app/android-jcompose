package com.carwise.android.view.auth

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.DoNotDisturbOnTotalSilence
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
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
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import java.util.*
import java.util.regex.Pattern

data class Country(
    val name: String,
    val code: String,
    val dialCode: String,
    val flag: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var selectedCountry by remember { mutableStateOf(getDefaultCountry()) }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    var isEmailError by remember { mutableStateOf(false) }
    var isPhoneError by remember { mutableStateOf(false) }
    var isPasswordError by remember { mutableStateOf(false) }
    var showCountryPicker by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    val authState by authViewModel.authState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val phoneUtil = PhoneNumberUtil.createInstance(context)
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    // Validation patterns
    val emailPattern = Pattern.compile(
        "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
        Pattern.CASE_INSENSITIVE
    )

    fun validateEmail(email: String): Boolean {
        return emailPattern.matcher(email).matches()
    }

    fun validatePhone(phone: String, countryCode: String): Boolean {
        return try {
            val number = phoneUtil.parse(phone, countryCode)
            phoneUtil.isValidNumber(number)
        } catch (e: Exception) {
            false
        }
    }

    fun validateForm(): Boolean {
        return firstName.isNotBlank() &&
                lastName.isNotBlank() &&
                validateEmail(email) &&
                validatePhone(phoneNumber, selectedCountry.code) &&
                password.length >= 6 &&
                password == confirmPassword
    }

    fun handleRegister() {
        when {
            firstName.isBlank() -> {
                Toast.makeText(context, "Lütfen adınızı girin", Toast.LENGTH_SHORT).show()
            }
            lastName.isBlank() -> {
                Toast.makeText(context, "Lütfen soyadınızı girin", Toast.LENGTH_SHORT).show()
            }
            email.isBlank() -> {
                Toast.makeText(context, "Lütfen e-posta adresinizi girin", Toast.LENGTH_SHORT).show()
                isEmailError = true
            }
            !validateEmail(email) -> {
                Toast.makeText(context, "Geçerli bir e-posta adresi girin", Toast.LENGTH_SHORT).show()
                isEmailError = true
            }
            phoneNumber.isBlank() -> {
                Toast.makeText(context, "Lütfen telefon numaranızı girin", Toast.LENGTH_SHORT).show()
                isPhoneError = true
            }
            !validatePhone(phoneNumber, selectedCountry.code) -> {
                Toast.makeText(context, "Geçerli bir telefon numarası girin", Toast.LENGTH_SHORT).show()
                isPhoneError = true
            }
            password.isBlank() -> {
                Toast.makeText(context, "Lütfen şifrenizi girin", Toast.LENGTH_SHORT).show()
                isPasswordError = true
            }
            password.length < 6 -> {
                Toast.makeText(context, "Şifre en az 6 karakter olmalıdır", Toast.LENGTH_SHORT).show()
                isPasswordError = true
            }
            password != confirmPassword -> {
                Toast.makeText(context, "Şifreler eşleşmiyor", Toast.LENGTH_SHORT).show()
                isPasswordError = true
            }
            else -> {
                isEmailError = false
                isPhoneError = false
                isPasswordError = false
                authViewModel.register(
                    firstName = firstName,
                    lastName = lastName,
                    countryCode = selectedCountry.dialCode,
                    phoneNumber = phoneNumber,
                    email = email,
                    password = password
                )
            }
        }
    }

    LaunchedEffect(authState.isAuthenticated) {
        if (authState.isAuthenticated) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Register.route) { inclusive = true }
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

    if (showCountryPicker) {
        ModalBottomSheet(
            onDismissRequest = { showCountryPicker = false },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = Color.White,

        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Ülke Seçin",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Ülke Ara") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = appRed,
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedLabelColor = appRed,
                        cursorColor = appRed
                    )
                )

                // Country List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    val filteredCountries = getCountries(context,phoneUtil).filter {
                        it.name.contains(searchQuery, ignoreCase = true) ||
                        it.dialCode.contains(searchQuery)
                    }
                    
                    items(filteredCountries) { country ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedCountry = country
                                    showCountryPicker = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = country.flag,
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = country.name,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = country.dialCode,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                            if (country.code == selectedCountry.code) {
                                Icon(
                                    imageVector = Icons.Filled.Circle,
                                    contentDescription = null,
                                    tint = appRed
                                )
                            }
                        }
                    }
                }
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
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
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
                text = "Hesap Oluştur",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                ),
                color = Color(0xFF1A1A1A),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Carwise'a hoş geldiniz",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF666666),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // First Name Field
            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("Ad") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = appRed,
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedLabelColor = appRed,
                    cursorColor = appRed
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            // Last Name Field
            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Soyad") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = appRed,
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedLabelColor = appRed,
                    cursorColor = appRed
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
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

            // Phone Number Field
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Country Code Button
                Box(
                    modifier = Modifier
                        .weight(0.4f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showCountryPicker = true }
                        .border(
                            width = 1.dp,
                            color = Color(0xFFE0E0E0),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 15.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${selectedCountry.flag} ${selectedCountry.dialCode}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF1A1A1A)
                        )
                        if (showCountryPicker) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "Ülke Seç",
                                tint = appRed
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Ülke Seç",
                                tint = appRed
                            )
                        }

                    }
                }

                // Phone Number Field
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { 
                        phoneNumber = it
                        isPhoneError = false
                    },
                    label = { Text("Telefon") },
                    singleLine = true,
                    isError = isPhoneError,
                    supportingText = {
                        if (isPhoneError) {
                            Text(
                                text = "Geçerli bir telefon numarası girin",
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
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.weight(0.6f)
                )
            }

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { 
                    password = it
                    isPasswordError = false
                },
                label = { Text("Şifre") },
                singleLine = true,
                isError = isPasswordError,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = appRed,
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedLabelColor = appRed,
                    cursorColor = appRed,
                    errorBorderColor = Color(0xFFE30613),
                    errorLabelColor = Color(0xFFE30613)
                ),
                visualTransformation = PasswordVisualTransformation(),
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
                    isPasswordError = false
                },
                label = { Text("Şifre Tekrar") },
                singleLine = true,
                isError = isPasswordError,
                supportingText = {
                    if (isPasswordError) {
                        Text(
                            text = "Şifreler eşleşmiyor",
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
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )

            // Register Button
            Button(
                onClick = { handleRegister() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp)),
                enabled = !authState.isLoading && validateForm(),
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
                        "Kayıt Ol",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            // Login Link
            TextButton(
                onClick = { navController.navigate(Screen.Login.route) },
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                Text(
                    "Zaten hesabınız var mı? Giriş yapın",
                    color = appRed,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

fun getDefaultCountry(): Country {
    return Country(
        name = "Türkiye",
        code = "TR",
        dialCode = "+90",
        flag = "🇹🇷"
    )
}

fun getCountries(context: Context,phoneUtil: PhoneNumberUtil): List<Country> {
    val countries = mutableListOf<Country>()
    val locales = Locale.getAvailableLocales()
    
    for (locale in locales) {
        val countryCode = locale.country
        if (countryCode.isNotEmpty()) {
            val countryName = locale.displayCountry
            val dialCode = "+${phoneUtil.getCountryCodeForRegion(countryCode)}"
            val flag = getCountryFlag(countryCode)
            
            if (!countries.any { it.code == countryCode } && dialCode != "+0") {
                countries.add(
                    Country(
                        name = countryName,
                        code = countryCode,
                        dialCode = dialCode,
                        flag = flag
                    )
                )
            }
        }
    }
    
    return countries.sortedBy { it.name }
}

private fun getCountryFlag(countryCode: String): String {
    val firstLetter = Character.codePointAt(countryCode, 0) - 0x41 + 0x1F1E6
    val secondLetter = Character.codePointAt(countryCode, 1) - 0x41 + 0x1F1E6
    return String(Character.toChars(firstLetter)) + String(Character.toChars(secondLetter))
} 