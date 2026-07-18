package com.teti2026.smartgreenhouse.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.teti2026.smartgreenhouse.data.model.UserRole
import com.teti2026.smartgreenhouse.ui.auth.LoginRegisterRoute
import com.teti2026.smartgreenhouse.ui.buyer.ChatRoute
import com.teti2026.smartgreenhouse.ui.buyer.CheckoutRoute
import com.teti2026.smartgreenhouse.ui.buyer.ListingDetailRoute
import com.teti2026.smartgreenhouse.ui.buyer.MapRoute
import com.teti2026.smartgreenhouse.ui.buyer.MarketplaceRoute
import com.teti2026.smartgreenhouse.ui.buyer.OrderHistoryRoute
import com.teti2026.smartgreenhouse.ui.buyer.OrderSuccessRoute
import com.teti2026.smartgreenhouse.ui.buyer.OrderStatus
import com.teti2026.smartgreenhouse.ui.buyer.ReviewRoute
import com.teti2026.smartgreenhouse.ui.buyer.sampleNearbyFarms
import com.teti2026.smartgreenhouse.ui.buyer.sampleOrderHistory
import com.teti2026.smartgreenhouse.ui.farmer.DashboardFarmerRoute

private val BUYER_BOTTOM_NAV_DESTINATIONS = setOf(Routes.BUYER_MARKETPLACE, Routes.BUYER_MAP, Routes.BUYER_ORDERS)
private val FARMER_BOTTOM_NAV_DESTINATIONS = setOf(Routes.FARMER_DASHBOARD)

/**
 * Graf navigasi utama, lihat `docs/SDD.md §6`. Saat ini login → Dashboard App Petani atau
 * tab Pasar/Peta/Pesanan App Pembeli yang tersambung; sisa farmerGraph & tab Profil menyusul
 * saat screen lain dibuat.
 */
@Composable
fun GreenhouseNavGraph(
    navController: NavHostController = rememberNavController()
) {
    // Handler bersama tab bawah App Pembeli (Pasar <-> Peta <-> Pesanan): pola standar
    // Navigation Compose untuk bottom nav agar back stack tiap tab tersimpan
    // (popUpTo start + saveState/restoreState).
    val onBuyerBottomNavigate: (String) -> Unit = { route ->
        if (route in BUYER_BOTTOM_NAV_DESTINATIONS) {
            navController.navigate(route) {
                popUpTo(Routes.BUYER_MARKETPLACE) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
        // TODO: tab Profil belum punya destination.
    }

    // Sama seperti [onBuyerBottomNavigate], untuk tab bawah App Petani. Hanya Dashboard yang
    // sudah punya destination — tab Riwayat/Tambah/Pesan/Profil menyusul saat screen dibuat.
    val onFarmerBottomNavigate: (String) -> Unit = { route ->
        if (route in FARMER_BOTTOM_NAV_DESTINATIONS) {
            navController.navigate(route) {
                popUpTo(Routes.FARMER_DASHBOARD) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
        // TODO: tab Riwayat/Tambah/Pesan/Profil belum punya destination.
    }

    NavHost(navController = navController, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) {
            LoginRegisterRoute(
                onLoginClick = { role ->
                    when (role) {
                        UserRole.FARMER -> {
                            // TODO: flag dummy — ganti dengan cek Firestore `farms` (apakah
                            // petani ini sudah punya dokumen farm/plot) via AuthViewModel/
                            // FirestoreRepository saat MOB-T06/T07 dikerjakan. Untuk sekarang
                            // selalu true supaya Dashboard bisa dilihat & diuji end-to-end;
                            // saat false, tujuan adalah Routes.FARMER_SETUP_GREENHOUSE
                            // (belum ada destination, screen menyusul).
                            val hasFarmSetup = true
                            val destination = if (hasFarmSetup) {
                                Routes.FARMER_DASHBOARD
                            } else {
                                Routes.FARMER_SETUP_GREENHOUSE
                            }
                            navController.navigate(destination) {
                                popUpTo(Routes.LOGIN) { inclusive = true }
                            }
                        }
                        UserRole.BUYER -> {
                            navController.navigate(Routes.BUYER_MARKETPLACE) {
                                popUpTo(Routes.LOGIN) { inclusive = true }
                            }
                        }
                    }
                }
            )
        }
        composable(Routes.FARMER_DASHBOARD) {
            DashboardFarmerRoute(
                onImageHistoryClick = { /* TODO: navigasi ke Riwayat Citra saat screen dibuat */ },
                onBottomNavigate = onFarmerBottomNavigate
            )
        }
        composable(Routes.BUYER_MARKETPLACE) {
            MarketplaceRoute(
                onListingClick = { listingId -> navController.navigate(Routes.buyerDetail(listingId)) },
                onBottomNavigate = onBuyerBottomNavigate
            )
        }
        composable(Routes.BUYER_MAP) {
            MapRoute(
                // Kartu kebun belum punya daftar komoditas sendiri (lihat catatan di
                // MapFarmItem.primaryListingId) — tap kartu langsung ke listing utama farm itu.
                onFarmClick = { farmId ->
                    val listingId = sampleNearbyFarms.firstOrNull { it.id == farmId }?.primaryListingId
                    if (listingId != null) {
                        navController.navigate(Routes.buyerDetail(listingId))
                    }
                },
                onSeeAllClick = {
                    navController.navigate(Routes.BUYER_MARKETPLACE) {
                        popUpTo(Routes.BUYER_MARKETPLACE) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onBottomNavigate = onBuyerBottomNavigate
            )
        }
        composable(
            route = Routes.BUYER_DETAIL,
            arguments = listOf(navArgument("listingId") { type = NavType.StringType })
        ) { backStackEntry ->
            val listingId = backStackEntry.arguments?.getString("listingId").orEmpty()
            ListingDetailRoute(
                listingId = listingId,
                onBackClick = { navController.popBackStack() },
                onChatClick = { id -> navController.navigate(Routes.buyerChat(id)) },
                onBuyClick = { id -> navController.navigate(Routes.buyerCheckout(id)) }
            )
        }
        composable(
            route = Routes.BUYER_CHAT,
            arguments = listOf(navArgument("listingId") { type = NavType.StringType })
        ) { backStackEntry ->
            val listingId = backStackEntry.arguments?.getString("listingId").orEmpty()
            ChatRoute(
                listingId = listingId,
                // Tombol back kembali ke screen sebelumnya di back stack, yaitu Detail Produk
                // tempat tombol ikon chat ditekan — bukan destination tetap.
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(
            route = Routes.BUYER_CHECKOUT,
            arguments = listOf(navArgument("listingId") { type = NavType.StringType })
        ) { backStackEntry ->
            val listingId = backStackEntry.arguments?.getString("listingId").orEmpty()
            CheckoutRoute(
                listingId = listingId,
                onBackClick = { navController.popBackStack() },
                onOrderConfirmed = { id ->
                    // TODO: setelah FirestoreRepository.createOrder(order) sungguhan tersimpan
                    // (MOB-T21), oper id dokumen order nyata ke OrderSuccessRoute alih-alih hanya
                    // listingId. Detail & Checkout dibersihkan dari back stack (tidak masuk akal
                    // menekan "back" dari layar sukses menuju form checkout yang sudah selesai).
                    navController.navigate(Routes.buyerOrderSuccess(id)) {
                        popUpTo(Routes.BUYER_MARKETPLACE) { inclusive = false }
                    }
                }
            )
        }
        composable(
            route = Routes.BUYER_ORDER_SUCCESS,
            arguments = listOf(navArgument("listingId") { type = NavType.StringType })
        ) { backStackEntry ->
            val listingId = backStackEntry.arguments?.getString("listingId").orEmpty()
            OrderSuccessRoute(
                listingId = listingId,
                // Sama seperti tab bawah App Pembeli: Riwayat Pesanan & Marketplace adalah
                // destination bottom-nav, jadi pakai handler yang sama agar back stack tab
                // konsisten (popUpTo start + saveState/restoreState).
                onViewHistoryClick = { onBuyerBottomNavigate(Routes.BUYER_ORDERS) },
                onBackToHomeClick = { onBuyerBottomNavigate(Routes.BUYER_MARKETPLACE) }
            )
        }
        composable(Routes.BUYER_ORDERS) {
            OrderHistoryRoute(
                onBackClick = { navController.popBackStack() },
                onBottomNavigate = onBuyerBottomNavigate,
                onOrderClick = { orderId ->
                    // Hanya pesanan "Selesai" yang punya tujuan sejauh ini (Beri Rating & Ulasan).
                    // Status lain (Berlangsung/Dibatalkan) belum punya screen "Detail Pesanan" di
                    // Stitch — tap diabaikan sampai screen tersebut dibuat.
                    val order = sampleOrderHistory.firstOrNull { it.id == orderId }
                    if (order?.status == OrderStatus.COMPLETED) {
                        navController.navigate(Routes.buyerReview(orderId))
                    }
                }
            )
        }
        composable(
            route = Routes.BUYER_REVIEW,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId").orEmpty()
            ReviewRoute(
                orderId = orderId,
                onBackClick = { navController.popBackStack() },
                onReviewSubmitted = {
                    // TODO: setelah FirestoreRepository.createReview(review) sungguhan tersimpan
                    // (MOB-T23), tampilkan Snackbar konfirmasi (docs/UIUX-Flow.md §6) sebelum
                    // kembali. Untuk sekarang langsung kembali ke Riwayat Pesanan.
                    navController.popBackStack()
                }
            )
        }
    }
}
