package com.teti2026.smartgreenhouse.ui.buyer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.teti2026.smartgreenhouse.R
import com.teti2026.smartgreenhouse.ui.components.GreenhouseTextField
import com.teti2026.smartgreenhouse.ui.theme.MintTint
import com.teti2026.smartgreenhouse.ui.theme.SmartgreenhousemobileTheme
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

/** Metode pengiriman pesanan — lihat layar "Checkout - Pembeli" (Stitch) & `docs/SRS.md` MOB-FR-18. */
enum class DeliveryMethod {
    PICKUP,
    DELIVERY
}

// Nilai demo tetap — checkout belum terhubung payment gateway/kalkulasi ongkir sungguhan
// (di luar lingkup versi bootcamp, lihat `docs/PRD.md §5.2`).
private const val CHECKOUT_SERVICE_FEE_RUPIAH = 2_000L
private const val CHECKOUT_SHIPPING_COST_RUPIAH = 10_000L

// Kuantitas default & batas bawah stepper = 1 (bukan minOrderKg listing) — minOrderKg tetap
// dipakai sebagai info di Detail Produk, tapi di Checkout stepper mulai dari 1 seperti pola
// e-commerce umum.
private const val MIN_QUANTITY = 1

/**
 * Layar "Checkout - Pembeli" dari Stitch. Stateless: seluruh data & event di-hoist ke caller
 * (nantinya CheckoutViewModel + FirestoreRepository.createOrder, lihat `docs/SDD.md §4.2/§5`).
 * Dijangkau dari tombol "Beli Sekarang" di Detail Produk.
 */
@Composable
fun CheckoutScreen(
    listing: ListingDetailItem,
    quantity: Int,
    onIncreaseQuantity: () -> Unit,
    onDecreaseQuantity: () -> Unit,
    deliveryMethod: DeliveryMethod,
    onDeliveryMethodChange: (DeliveryMethod) -> Unit,
    address: String,
    onAddressChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val subtotal = listing.pricePerKg * quantity
    val shippingCost = when (deliveryMethod) {
        DeliveryMethod.PICKUP -> 0L
        DeliveryMethod.DELIVERY -> CHECKOUT_SHIPPING_COST_RUPIAH
    }
    val total = subtotal + CHECKOUT_SERVICE_FEE_RUPIAH + shippingCost
    val canConfirm = deliveryMethod == DeliveryMethod.PICKUP || address.isNotBlank()

    Column(modifier = modifier.fillMaxSize()) {
        CheckoutTopBar(onBackClick = onBackClick)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            CheckoutItemSection(
                listing = listing,
                quantity = quantity,
                canDecrease = quantity > MIN_QUANTITY,
                canIncrease = quantity < listing.quantityAvailableKg.roundToInt(),
                onDecreaseQuantity = onDecreaseQuantity,
                onIncreaseQuantity = onIncreaseQuantity
            )
            CheckoutDeliverySection(
                deliveryMethod = deliveryMethod,
                onDeliveryMethodChange = onDeliveryMethodChange,
                shippingCostLabel = formatRupiah(CHECKOUT_SHIPPING_COST_RUPIAH),
                address = address,
                onAddressChange = onAddressChange
            )
            CheckoutSummarySection(
                subtotalLabel = formatRupiah(subtotal),
                serviceFeeLabel = formatRupiah(CHECKOUT_SERVICE_FEE_RUPIAH),
                shippingLabel = if (deliveryMethod == DeliveryMethod.DELIVERY) formatRupiah(shippingCost) else null,
                totalLabel = formatRupiah(total)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        CheckoutBottomBar(confirmEnabled = canConfirm, onConfirmClick = onConfirmClick)
    }
}

@Composable
private fun CheckoutTopBar(onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .height(56.dp)
    ) {
        IconButton(onClick = onBackClick, modifier = Modifier.align(Alignment.CenterStart)) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.checkout_back_content_description),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = stringResource(R.string.checkout_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun CheckoutItemSection(
    listing: ListingDetailItem,
    quantity: Int,
    canDecrease: Boolean,
    canIncrease: Boolean,
    onDecreaseQuantity: () -> Unit,
    onIncreaseQuantity: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.checkout_item_section_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerLowest, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            AsyncImage(
                model = listing.imageUrls.first(),
                contentDescription = listing.imageContentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(88.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = listing.cropName,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = listing.pricePerKgLabel,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(
                                R.string.checkout_price_per_unit_suffix,
                                listing.unitLabel.lowercase()
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                QuantityStepper(
                    quantity = quantity,
                    canDecrease = canDecrease,
                    canIncrease = canIncrease,
                    onDecreaseQuantity = onDecreaseQuantity,
                    onIncreaseQuantity = onIncreaseQuantity,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
private fun QuantityStepper(
    quantity: Int,
    canDecrease: Boolean,
    canIncrease: Boolean,
    onDecreaseQuantity: () -> Unit,
    onIncreaseQuantity: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        StepperButton(
            icon = Icons.Filled.Remove,
            contentDescription = stringResource(R.string.checkout_quantity_decrease_content_description),
            enabled = canDecrease,
            onClick = onDecreaseQuantity,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            borderColor = MaterialTheme.colorScheme.outline
        )
        Text(
            text = quantity.toString(),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(24.dp)
        )
        StepperButton(
            icon = Icons.Filled.Add,
            contentDescription = stringResource(R.string.checkout_quantity_increase_content_description),
            enabled = canIncrease,
            onClick = onIncreaseQuantity,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            borderColor = Color.Transparent
        )
    }
}

@Composable
private fun StepperButton(
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
    containerColor: Color,
    contentColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier
) {
    val alpha = if (enabled) 1f else 0.4f
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(containerColor.copy(alpha = containerColor.alpha * alpha))
            .border(1.dp, borderColor.copy(alpha = borderColor.alpha * alpha), CircleShape)
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = contentColor.copy(alpha = alpha),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun CheckoutDeliverySection(
    deliveryMethod: DeliveryMethod,
    onDeliveryMethodChange: (DeliveryMethod) -> Unit,
    shippingCostLabel: String,
    address: String,
    onAddressChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.checkout_delivery_section_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        DeliveryMethodOption(
            icon = Icons.Filled.Storefront,
            label = stringResource(R.string.checkout_delivery_pickup_label),
            sublabel = stringResource(R.string.checkout_delivery_pickup_sublabel),
            selected = deliveryMethod == DeliveryMethod.PICKUP,
            onClick = { onDeliveryMethodChange(DeliveryMethod.PICKUP) }
        )
        DeliveryMethodOption(
            icon = Icons.Filled.LocalShipping,
            label = stringResource(R.string.checkout_delivery_delivery_label),
            sublabel = stringResource(R.string.checkout_delivery_delivery_sublabel, shippingCostLabel),
            selected = deliveryMethod == DeliveryMethod.DELIVERY,
            onClick = { onDeliveryMethodChange(DeliveryMethod.DELIVERY) }
        )
        if (deliveryMethod == DeliveryMethod.DELIVERY) {
            GreenhouseTextField(
                value = address,
                onValueChange = onAddressChange,
                label = stringResource(R.string.checkout_address_label),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun DeliveryMethodOption(
    icon: ImageVector,
    label: String,
    sublabel: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val backgroundColor = if (selected) MintTint else MaterialTheme.colorScheme.surfaceContainerLowest
    val iconTint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(if (selected) 2.dp else 1.dp, borderColor, RoundedCornerShape(12.dp))
            .selectable(selected = selected, onClick = onClick)
            .padding(16.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(20.dp)
                .border(2.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, CircleShape)
        ) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            Text(
                text = sublabel,
                style = MaterialTheme.typography.bodySmall,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(imageVector = icon, contentDescription = null, tint = iconTint)
    }
}

@Composable
private fun CheckoutSummarySection(
    subtotalLabel: String,
    serviceFeeLabel: String,
    shippingLabel: String?,
    totalLabel: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.checkout_summary_section_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerLowest, RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryRow(stringResource(R.string.checkout_summary_subtotal_label), subtotalLabel)
            SummaryRow(stringResource(R.string.checkout_summary_service_fee_label), serviceFeeLabel)
            if (shippingLabel != null) {
                SummaryRow(stringResource(R.string.checkout_summary_shipping_label), shippingLabel)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.checkout_summary_total_label),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = totalLabel,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, modifier: Modifier = Modifier) {
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun CheckoutBottomBar(
    confirmEnabled: Boolean,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 8.dp
    ) {
        Button(
            onClick = onConfirmClick,
            enabled = confirmEnabled,
            shape = RoundedCornerShape(percent = 50),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .height(52.dp)
        ) {
            Text(text = stringResource(R.string.checkout_confirm_button), style = MaterialTheme.typography.labelLarge)
        }
    }
}

private fun formatRupiah(amount: Long): String {
    val formatted = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")).format(amount)
    return "Rp $formatted"
}

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun CheckoutScreenPickupPreview() {
    SmartgreenhousemobileTheme {
        CheckoutScreen(
            listing = sampleListingDetails.getValue("listing-cabai-rawit-1"),
            quantity = 5,
            onIncreaseQuantity = {},
            onDecreaseQuantity = {},
            deliveryMethod = DeliveryMethod.PICKUP,
            onDeliveryMethodChange = {},
            address = "",
            onAddressChange = {},
            onBackClick = {},
            onConfirmClick = {}
        )
    }
}

@Preview(showBackground = true, heightDp = 1100)
@Composable
private fun CheckoutScreenDeliveryPreview() {
    SmartgreenhousemobileTheme {
        CheckoutScreen(
            listing = sampleListingDetails.getValue("listing-cabai-rawit-1"),
            quantity = 5,
            onIncreaseQuantity = {},
            onDecreaseQuantity = {},
            deliveryMethod = DeliveryMethod.DELIVERY,
            onDeliveryMethodChange = {},
            address = "Jl. Merdeka No. 10, Boyolali",
            onAddressChange = {},
            onBackClick = {},
            onConfirmClick = {}
        )
    }
}
