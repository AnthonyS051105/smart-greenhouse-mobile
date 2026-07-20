package com.teti2026.smartgreenhouse.ui.buyer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teti2026.smartgreenhouse.ui.components.ProfileErrorView
import com.teti2026.smartgreenhouse.ui.components.ProfileLoadingIndicator
import com.teti2026.smartgreenhouse.viewmodel.ReviewSubmitState
import com.teti2026.smartgreenhouse.viewmodel.ReviewUiState
import com.teti2026.smartgreenhouse.viewmodel.ReviewViewModel

@Composable
fun ReviewRoute(
    orderId: String,
    onBackClick: () -> Unit = {},
    onReviewSubmitted: (orderId: String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ReviewViewModel = viewModel()
) {
    LaunchedEffect(orderId) { viewModel.load(orderId) }

    val state by viewModel.state.collectAsStateWithLifecycle()
    val submitState by viewModel.submitState.collectAsStateWithLifecycle()

    when (val s = state) {
        is ReviewUiState.Loading -> ProfileLoadingIndicator()
        is ReviewUiState.Error -> ProfileErrorView(
            messageResId = s.messageResId,
            onRetryClick = { viewModel.load(orderId) }
        )
        is ReviewUiState.Success -> {
            var rating by remember(orderId) { mutableIntStateOf(0) }
            var comment by remember(orderId) { mutableStateOf("") }
            val submitErrorMessage = (submitState as? ReviewSubmitState.Error)?.messageResId?.let {
                stringResource(it)
            }

            ReviewScreen(
                target = s.target,
                rating = rating,
                onRatingChange = { rating = it },
                comment = comment,
                onCommentChange = { comment = it },
                onBackClick = onBackClick,
                onSubmitClick = {
                    viewModel.submitReview(rating, comment) { onReviewSubmitted(s.target.orderId) }
                },
                isSubmitting = submitState is ReviewSubmitState.Submitting,
                submitErrorMessage = submitErrorMessage,
                modifier = modifier
            )
        }
    }
}
