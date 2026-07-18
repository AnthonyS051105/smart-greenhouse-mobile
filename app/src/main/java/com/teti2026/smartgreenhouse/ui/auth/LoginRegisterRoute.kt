package com.teti2026.smartgreenhouse.ui.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.teti2026.smartgreenhouse.data.model.UserRole

// TODO: pindahkan state ke AuthViewModel (StateFlow<UiState>) dan sambungkan ke
// Firebase Auth saat fitur autentikasi diimplementasikan penuh. [onLoginClick] untuk
// sementara langsung navigasi tanpa validasi kredensial, dan role dikirim ke caller
// (GreenhouseNavGraph) supaya bisa menentukan tujuan navigasi (App Petani vs App Pembeli).
@Composable
fun LoginRegisterRoute(
    onLoginClick: (UserRole) -> Unit
) {
    var role by remember { mutableStateOf(UserRole.FARMER) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    LoginRegisterScreen(
        selectedRole = role,
        onRoleSelected = { role = it },
        email = email,
        onEmailChange = { email = it },
        password = password,
        onPasswordChange = { password = it },
        isPasswordVisible = isPasswordVisible,
        onTogglePasswordVisibility = { isPasswordVisible = !isPasswordVisible },
        onLoginClick = { onLoginClick(role) },
        onForgotPasswordClick = { /* TODO: navigasi ke lupa sandi */ },
        onRegisterClick = { /* TODO: navigasi ke register */ }
    )
}
