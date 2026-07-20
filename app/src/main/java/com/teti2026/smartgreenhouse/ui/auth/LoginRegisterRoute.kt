package com.teti2026.smartgreenhouse.ui.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teti2026.smartgreenhouse.data.model.UserRole
import com.teti2026.smartgreenhouse.viewmodel.AuthUiState
import com.teti2026.smartgreenhouse.viewmodel.AuthViewModel
import com.teti2026.smartgreenhouse.viewmodel.ForgotPasswordUiState

/**
 * Wrapper stateful layar Login/Register — menyambungkan [LoginRegisterScreen] (stateless) ke
 * [AuthViewModel] (Firebase Auth + Firestore `users`, lihat [com.teti2026.smartgreenhouse.repository.AuthRepository]).
 * [onLoginClick] dipanggil PERSIS SEKALI per sesi berhasil (register maupun login) dengan role
 * akun sungguhan (dari Firestore saat login, dari pilihan form saat register) — caller
 * (GreenhouseNavGraph) memakainya untuk menentukan tujuan navigasi (App Petani vs App Pembeli).
 */
@Composable
fun LoginRegisterRoute(
    onLoginClick: (UserRole, Boolean) -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var isRegisterMode by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(UserRole.FARMER) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var forgotPasswordEmail by remember { mutableStateOf("") }

    val authState by viewModel.state.collectAsStateWithLifecycle()
    val forgotPasswordState by viewModel.forgotPasswordState.collectAsStateWithLifecycle()

    // Trigger navigasi hanya saat state BARU menjadi Success (bukan tiap recomposition) —
    // key(authState) pada LaunchedEffect otomatis membatasi ini karena AuthUiState.Success
    // adalah data class baru tiap kali diemisikan ulang oleh ViewModel.
    LaunchedEffect(authState) {
        val success = authState as? AuthUiState.Success ?: return@LaunchedEffect
        onLoginClick(success.role, success.hasFarmSetup)
    }

    val errorMessage = (authState as? AuthUiState.Error)?.let { stringResource(it.messageResId) }

    LoginRegisterScreen(
        isRegisterMode = isRegisterMode,
        name = name,
        onNameChange = { name = it },
        selectedRole = role,
        onRoleSelected = { role = it },
        email = email,
        onEmailChange = { email = it },
        password = password,
        onPasswordChange = { password = it },
        isPasswordVisible = isPasswordVisible,
        onTogglePasswordVisibility = { isPasswordVisible = !isPasswordVisible },
        isLoading = authState is AuthUiState.Loading,
        errorMessage = errorMessage,
        onPrimaryActionClick = {
            if (isRegisterMode) {
                viewModel.register(name, email, password, role)
            } else {
                viewModel.login(email, password)
            }
        },
        onForgotPasswordClick = {
            forgotPasswordEmail = email
            viewModel.resetForgotPasswordState()
            showForgotPasswordDialog = true
        },
        onToggleModeClick = {
            isRegisterMode = !isRegisterMode
            viewModel.consumeError()
        }
    )

    if (showForgotPasswordDialog) {
        ForgotPasswordDialog(
            email = forgotPasswordEmail,
            onEmailChange = { forgotPasswordEmail = it },
            state = forgotPasswordState,
            onSendClick = { viewModel.sendPasswordReset(forgotPasswordEmail) },
            onDismiss = { showForgotPasswordDialog = false }
        )
    }
}
