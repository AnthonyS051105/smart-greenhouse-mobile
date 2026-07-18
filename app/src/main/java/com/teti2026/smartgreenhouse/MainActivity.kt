package com.teti2026.smartgreenhouse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.teti2026.smartgreenhouse.data.model.UserRole
import com.teti2026.smartgreenhouse.ui.auth.LoginRegisterScreen
import com.teti2026.smartgreenhouse.ui.theme.SmartgreenhousemobileTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartgreenhousemobileTheme {
                LoginRegisterRoute()
            }
        }
    }
}

// TODO: pindahkan state ke AuthViewModel (StateFlow<UiState>) dan sambungkan ke
// Navigation Compose + Firebase Auth saat fitur autentikasi diimplementasikan penuh.
@Composable
private fun LoginRegisterRoute() {
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
        onLoginClick = { /* TODO: AuthViewModel.login(...) */ },
        onForgotPasswordClick = { /* TODO: navigasi ke lupa sandi */ },
        onRegisterClick = { /* TODO: navigasi ke register */ }
    )
}
