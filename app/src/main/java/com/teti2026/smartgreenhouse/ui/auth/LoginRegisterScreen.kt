package com.teti2026.smartgreenhouse.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.teti2026.smartgreenhouse.R
import com.teti2026.smartgreenhouse.data.model.UserRole
import com.teti2026.smartgreenhouse.ui.components.GreenhouseTextField
import com.teti2026.smartgreenhouse.ui.components.RoleSegmentedControl
import com.teti2026.smartgreenhouse.ui.theme.SmartgreenhousemobileTheme

private const val HERO_IMAGE_URL =
    "https://lh3.googleusercontent.com/aida-public/AB6AXuCJMEsDxqVyrdvgbIwmaoUyYEeRExpbzUV2VhqsYQBQ1c9wjhCfkHvwYn5VTOUKsoZxRasnqThxFAQZj1kr1W4TRTLCQXG5kAqFRHt3l9S7ZfZ9pAZLcQv2mvzTf17DiXZ-7VXHyKpt4tBag3abiRZmNzGWCryg6qYUHkPESZvx1HYPIMzyA_v8BaMIv-l-6i9HTNop6u-WicK0HTPxflSPn6-rMM4W_f4YFOmv7XjtdXq-cLbUBlSLPrep8mOdI1iHL80ww9mi1Q"

/**
 * Layar Login/Register — screen "Login / Register - GreenHouse+" dari Stitch.
 * Stateless: seluruh state (role, email, password, visibility) di-hoist ke caller
 * (nantinya AuthViewModel), sesuai pola MVVM+UDF di `docs/SDD.md §5`.
 *
 * Form ditampilkan sebagai [ModalBottomSheet] SUNGGUHAN (bukan `Surface` statis dengan
 * `weight` tetap seperti sebelumnya) — pola sama seperti [com.teti2026.smartgreenhouse.
 * ui.farmer.control.IrrigationControlScreen] — agar panel bisa ditarik (drag) untuk
 * memperluas area terlihat saat mode Daftar (field lebih banyak: Role + Nama + Email +
 * Password) daripada dipaksa muat di ruang 45% layar yang tetap. Konten form JUGA
 * `verticalScroll` sebagai jaring pengaman kedua di layar pendek meski sheet sudah full-
 * expanded. Hero image tetap terlihat penuh di BELAKANG sheet (bukan terpotong `weight`).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginRegisterScreen(
    isRegisterMode: Boolean,
    name: String,
    onNameChange: (String) -> Unit,
    selectedRole: UserRole,
    onRoleSelected: (UserRole) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    isPasswordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    onPrimaryActionClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onToggleModeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // `confirmValueChange` menolak `Hidden` — Auth adalah halaman awal wajib, drag-down
    // hanya boleh kembali ke `PartiallyExpanded`, tidak pernah benar-benar menutup sheet.
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false,
        confirmValueChange = { it != SheetValue.Hidden }
    )

    Box(modifier = modifier.fillMaxSize()) {
        // Tinggi hero DIBATASI (bukan `fillMaxSize()`) — sengaja lebih tinggi dari posisi awal
        // sheet (~55% desain lama) hanya secukupnya agar sheet draggable ke atas tidak langsung
        // menutupinya, TAPI TIDAK memenuhi seluruh layar: `ContentScale.Crop` pada area sebesar
        // itu membuat foto sumber (resolusi tetap) di-crop ke porsi yang jauh lebih kecil →
        // tampak sangat di-zoom dan pecah (dilaporkan user). 65% mendekati rasio lama, cukup
        // ruang untuk drag handle terlihat di atas sheet collapsed.
        AuthHeroSection(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.65f))

        ModalBottomSheet(
            onDismissRequest = {},
            sheetState = sheetState,
            // Auth adalah halaman awal wajib, tidak boleh benar-benar hilang dari layar —
            // scrim ditransparankan (bukan default gelap) supaya hero tetap terlihat penuh
            // saat sheet ditarik naik, dan drag-down TIDAK menutup sheet ke luar layar
            // (`confirmValueChange` menolak `Hidden`).
            scrimColor = Color.Transparent,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            AuthFormContent(
                isRegisterMode = isRegisterMode,
                name = name,
                onNameChange = onNameChange,
                selectedRole = selectedRole,
                onRoleSelected = onRoleSelected,
                email = email,
                onEmailChange = onEmailChange,
                password = password,
                onPasswordChange = onPasswordChange,
                isPasswordVisible = isPasswordVisible,
                onTogglePasswordVisibility = onTogglePasswordVisibility,
                isLoading = isLoading,
                errorMessage = errorMessage,
                onPrimaryActionClick = onPrimaryActionClick,
                onForgotPasswordClick = onForgotPasswordClick,
                onToggleModeClick = onToggleModeClick
            )
        }
    }
}

@Composable
private fun AuthHeroSection(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth()) {
        AsyncImage(
            model = HERO_IMAGE_URL,
            contentDescription = stringResource(R.string.auth_hero_content_description),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        // Scrim gelap agar logo & teks brand tetap terbaca di atas foto.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(20.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = Icons.Filled.Eco,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primaryContainer
                    )
                }
            }
            Text(
                text = stringResource(R.string.auth_brand_name),
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
        }
    }
}

/**
 * Konten [ModalBottomSheet] Login/Register. TIDAK membungkus `Surface` sendiri (sheet sudah
 * menyediakan container+shape) dan seluruh isi (di bawah drag handle & header) dibungkus
 * `verticalScroll` — jaring pengaman saat mode Daftar (Role + Nama + Email + Password, 4
 * field) di layar pendek, supaya tombol utama & link toggle tidak pernah terpotong/mepet
 * seperti sebelumnya (keluhan user: "3 baris input membuat halaman terlihat penuh").
 *
 * [onToggleModeClick] SEKARANG dipicu dua cara: link teks di bawah (perilaku lama, tetap ada)
 * DAN [IconButton] "X" eksplisit di header saat [isRegisterMode] true — sebelumnya tidak ada
 * cara jelas untuk kembali dari Daftar ke Masuk selain scroll cari link kecil di bawah, yang
 * mudah tidak sengaja terlewat.
 */
@Composable
private fun AuthFormContent(
    isRegisterMode: Boolean,
    name: String,
    onNameChange: (String) -> Unit,
    selectedRole: UserRole,
    onRoleSelected: (UserRole) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    isPasswordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    onPrimaryActionClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onToggleModeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 20.dp)
            .widthIn(max = 480.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
            Text(
                text = stringResource(R.string.auth_welcome_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().align(Alignment.Center)
            )
            if (isRegisterMode) {
                IconButton(
                    onClick = onToggleModeClick,
                    modifier = Modifier.align(Alignment.CenterEnd).size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.auth_back_to_login_content_description),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Role hanya relevan saat mendaftar — akun yang sudah ada punya role tetap
        // (tersimpan di Firestore `users/{uid}.role`), tidak bisa dipilih ulang saat login.
        if (isRegisterMode) {
            RoleSegmentedControl(
                selectedRole = selectedRole,
                onRoleSelected = onRoleSelected,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            GreenhouseTextField(
                value = name,
                onValueChange = onNameChange,
                label = stringResource(R.string.auth_name_label),
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        GreenhouseTextField(
            value = email,
            onValueChange = onEmailChange,
            label = stringResource(R.string.auth_email_label),
            keyboardType = KeyboardType.Email,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        GreenhouseTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = stringResource(R.string.auth_password_label),
            keyboardType = KeyboardType.Password,
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = onTogglePasswordVisibility) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = stringResource(R.string.auth_toggle_password_visibility),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (!isRegisterMode) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text(
                    text = stringResource(R.string.auth_forgot_password),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .clickable(onClick = onForgotPasswordClick)
                )
            }
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        Button(
            onClick = onPrimaryActionClick,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(percent = 50),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = stringResource(
                        if (isRegisterMode) R.string.auth_register_button else R.string.auth_login_button
                    ),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggleModeClick),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(
                    if (isRegisterMode) R.string.auth_login_prompt else R.string.auth_register_prompt
                ) + " ",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(
                    if (isRegisterMode) R.string.auth_login_cta else R.string.auth_register_cta
                ),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primaryContainer
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun LoginRegisterScreenPreview() {
    var isRegisterMode by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(UserRole.FARMER) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    SmartgreenhousemobileTheme {
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
            isPasswordVisible = passwordVisible,
            onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
            isLoading = false,
            errorMessage = null,
            onPrimaryActionClick = {},
            onForgotPasswordClick = {},
            onToggleModeClick = { isRegisterMode = !isRegisterMode }
        )
    }
}
