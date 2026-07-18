package com.teti2026.smartgreenhouse.ui.buyer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.teti2026.smartgreenhouse.R
import com.teti2026.smartgreenhouse.ui.theme.BorderOutline
import com.teti2026.smartgreenhouse.ui.theme.MintTint
import com.teti2026.smartgreenhouse.ui.theme.SmartgreenhousemobileTheme

/**
 * Layar "Konfirmasi Pesanan - Berhasil" dari Stitch. Stateless: seluruh data & event di-hoist
 * ke caller. Dijangkau dari tombol "Konfirmasi Pesanan" di Checkout setelah order berhasil
 * dibuat (lihat `docs/UIUX-Flow.md §4.4`). Sesuai mockup, layar ini tidak punya top bar/bottom
 * nav ("Transactional/Success intent") — konten dipusatkan penuh secara vertikal & horizontal.
 */
@Composable
fun OrderSuccessScreen(
    item: OrderSuccessItem,
    onViewHistoryClick: () -> Unit,
    onBackToHomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 32.dp)
    ) {
        OrderSuccessIllustration()
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.order_success_title),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.order_success_message, item.sellerName),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 280.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        OrderIdChip(orderId = item.orderId)
        Spacer(modifier = Modifier.height(32.dp))
        OrderSuccessActions(
            onViewHistoryClick = onViewHistoryClick,
            onBackToHomeClick = onBackToHomeClick
        )
    }
}

@Composable
private fun OrderSuccessIllustration(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(128.dp)
            .background(MintTint, CircleShape)
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(64.dp)
        )
    }
}

@Composable
private fun OrderIdChip(orderId: String, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.order_success_order_id, orderId),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.outline,
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(8.dp))
            .border(1.dp, BorderOutline, RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun OrderSuccessActions(
    onViewHistoryClick: () -> Unit,
    onBackToHomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(
            onClick = onViewHistoryClick,
            shape = RoundedCornerShape(percent = 50),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(text = stringResource(R.string.order_success_view_history_button), style = MaterialTheme.typography.labelLarge)
        }
        OutlinedButton(
            onClick = onBackToHomeClick,
            shape = RoundedCornerShape(percent = 50),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primaryContainer),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(text = stringResource(R.string.order_success_back_home_button), style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun OrderSuccessScreenPreview() {
    SmartgreenhousemobileTheme {
        OrderSuccessScreen(
            item = orderSuccessItemFrom(sampleListingDetails.getValue("listing-cabai-rawit-1")),
            onViewHistoryClick = {},
            onBackToHomeClick = {}
        )
    }
}
