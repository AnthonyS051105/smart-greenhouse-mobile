package com.teti2026.smartgreenhouse.ui.buyer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.teti2026.smartgreenhouse.R
import com.teti2026.smartgreenhouse.ui.theme.BorderOutline
import com.teti2026.smartgreenhouse.ui.theme.DisabledBg
import com.teti2026.smartgreenhouse.ui.theme.DisabledText
import com.teti2026.smartgreenhouse.ui.theme.SmartgreenhousemobileTheme

/**
 * Layar "Beri Rating & Ulasan - AgriSmart" dari Stitch. Stateless: seluruh data & event
 * di-hoist ke caller (nantinya ReviewViewModel + FirestoreRepository.createReview, lihat
 * `docs/SDD.md §4.2/§5`). Dijangkau dari kartu pesanan berstatus "Selesai" di Riwayat Pesanan.
 */
@Composable
fun ReviewScreen(
    target: ReviewTargetItem,
    rating: Int,
    onRatingChange: (Int) -> Unit,
    comment: String,
    onCommentChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onSubmitClick: () -> Unit,
    isSubmitting: Boolean = false,
    submitErrorMessage: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        ReviewTopBar(onBackClick = onBackClick)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            ReviewProductSummaryCard(target = target)
            ReviewRatingSelector(rating = rating, onRatingChange = onRatingChange)
            ReviewCommentField(
                value = comment,
                onValueChange = onCommentChange,
                placeholder = stringResource(R.string.review_comment_placeholder, target.sellerName)
            )
        }
        // Kirim baru aktif setelah bintang dipilih (rating adalah field wajib `reviews.rating`,
        // docs/data-contracts.md §3.10, ulasan teks bersifat opsional) DAN belum sedang submit.
        ReviewBottomBar(
            submitEnabled = rating > 0 && !isSubmitting,
            onSubmitClick = onSubmitClick,
            isSubmitting = isSubmitting,
            errorMessage = submitErrorMessage
        )
    }
}

@Composable
private fun ReviewTopBar(onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(56.dp)
        ) {
            IconButton(onClick = onBackClick, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.review_back_content_description),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = stringResource(R.string.review_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
private fun ReviewProductSummaryCard(target: ReviewTargetItem, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .border(1.dp, BorderOutline, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        AsyncImage(
            model = target.imageUrl,
            contentDescription = target.imageContentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(128.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = target.cropName,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.review_seller_label, target.sellerName),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ReviewRatingSelector(
    rating: Int,
    onRatingChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.review_rating_prompt),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            for (value in 1..5) {
                val filled = value <= rating
                IconButton(
                    onClick = { onRatingChange(value) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = stringResource(R.string.review_star_content_description, value),
                        // secondaryContainer = amber (#FDB243) — persis token `secondary-container`
                        // di mockup Stitch untuk bintang terisi, bukan warna health_score.
                        tint = if (filled) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
        Text(
            text = if (rating > 0) reviewRatingLabel(rating) else "",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.height(16.dp)
        )
    }
}

@Composable
private fun reviewRatingLabel(rating: Int): String = when (rating) {
    1 -> stringResource(R.string.review_rating_label_1)
    2 -> stringResource(R.string.review_rating_label_2)
    3 -> stringResource(R.string.review_rating_label_3)
    4 -> stringResource(R.string.review_rating_label_4)
    else -> stringResource(R.string.review_rating_label_5)
}

@Composable
private fun ReviewCommentField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.review_comment_label),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 112.dp)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                .border(1.dp, BorderOutline, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ReviewBottomBar(
    submitEnabled: Boolean,
    onSubmitClick: () -> Unit,
    isSubmitting: Boolean,
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            Button(
                onClick = onSubmitClick,
                enabled = submitEnabled,
                shape = RoundedCornerShape(percent = 50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    disabledContainerColor = DisabledBg,
                    disabledContentColor = DisabledText
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(text = stringResource(R.string.review_submit_button), style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun ReviewScreenUnratedPreview() {
    SmartgreenhousemobileTheme {
        ReviewScreen(
            target = reviewTargetFrom(
                sampleOrderHistory.first { it.status == OrderStatus.COMPLETED },
                sampleListingDetails["listing-tomat-1"]
            ),
            rating = 0,
            onRatingChange = {},
            comment = "",
            onCommentChange = {},
            onBackClick = {},
            onSubmitClick = {}
        )
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun ReviewScreenRatedPreview() {
    SmartgreenhousemobileTheme {
        ReviewScreen(
            target = reviewTargetFrom(
                sampleOrderHistory.first { it.status == OrderStatus.COMPLETED },
                sampleListingDetails["listing-tomat-1"]
            ),
            rating = 4,
            onRatingChange = {},
            comment = "Cabai segar, sesuai deskripsi. Pengiriman cepat.",
            onCommentChange = {},
            onBackClick = {},
            onSubmitClick = {}
        )
    }
}
