package com.teti2026.smartgreenhouse.ui.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.teti2026.smartgreenhouse.R
import com.teti2026.smartgreenhouse.ui.components.GreenhouseTextField
import com.teti2026.smartgreenhouse.viewmodel.ForgotPasswordUiState

/**
 * Dialog "Lupa Sandi" — TIDAK ada padanan screen di Stitch (dicek, tidak ditemukan), sengaja
 * dibuat sebagai dialog (bukan screen baru) karena aksinya hanya satu field + satu tombol kirim,
 * konsisten dengan pola dialog konfirmasi lain di app ini (`ProfileBuyerLogoutDialog`). Dua
 * tampilan sesuai [state]: form input email ([ForgotPasswordUiState.Idle]/[Loading]/[Error]), atau
 * konfirmasi terkirim ([ForgotPasswordUiState.Success]) — dialog yang sama, isi berbeda, supaya
 * tidak perlu dialog kedua terpisah untuk hasil sukses.
 */
@Composable
fun ForgotPasswordDialog(
    email: String,
    onEmailChange: (String) -> Unit,
    state: ForgotPasswordUiState,
    onSendClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSuccess = state is ForgotPasswordUiState.Success
    val isLoading = state is ForgotPasswordUiState.Loading

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Filled.Email,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primaryContainer
            )
        },
        title = {
            Text(
                text = stringResource(
                    if (isSuccess) R.string.auth_forgot_password_success_title
                    else R.string.auth_forgot_password_dialog_title
                )
            )
        },
        text = {
            if (isSuccess) {
                Text(
                    text = stringResource(R.string.auth_forgot_password_success_message, email),
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Column {
                    Text(
                        text = stringResource(R.string.auth_forgot_password_dialog_description),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    GreenhouseTextField(
                        value = email,
                        onValueChange = onEmailChange,
                        label = stringResource(R.string.auth_email_label),
                        keyboardType = KeyboardType.Email
                    )
                    if (state is ForgotPasswordUiState.Error) {
                        Text(
                            text = stringResource(state.messageResId),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (isSuccess) {
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(R.string.auth_forgot_password_close_button))
                }
            } else {
                TextButton(onClick = onSendClick, enabled = !isLoading) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text(text = stringResource(R.string.auth_forgot_password_send_button))
                    }
                }
            }
        },
        dismissButton = {
            if (!isSuccess) {
                TextButton(onClick = onDismiss, enabled = !isLoading) {
                    Text(text = stringResource(R.string.auth_forgot_password_cancel_button))
                }
            }
        },
        modifier = modifier
    )
}
