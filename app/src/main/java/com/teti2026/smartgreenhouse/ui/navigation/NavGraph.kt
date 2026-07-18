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
import com.teti2026.smartgreenhouse.ui.buyer.ListingDetailRoute
import com.teti2026.smartgreenhouse.ui.buyer.MapRoute
import com.teti2026.smartgreenhouse.ui.buyer.MarketplaceRoute
import com.teti2026.smartgreenhouse.ui.buyer.sampleNearbyFarms
import com.teti2026.smartgreenhouse.ui.farmer.DashboardFarmerRoute

private val BUYER_BOTTOM_NAV_DESTINATIONS = setOf(Routes.BUYER_MARKETPLACE, Routes.BUYER_MAP)
private val FARMER_BOTTOM_NAV_DESTINATIONS = setOf(Routes.FARMER_DASHBOARD)

/**
 * Graf navigasi utama, lihat `docs/SDD.md §6`. Saat ini login → Dashboard App Petani atau
 * tab Pasar/Peta App Pembeli yang tersambung; sisa farmerGraph & tab Pesanan/Profil menyusul
 * saat screen lain dibuat.
 */
@Composable
fun GreenhouseNavGraph(
    navController: NavHostController = rememberNavController()
) {
    // Handler bersama tab bawah App Pembeli (Pasar <-> Peta): pola standar Navigation Compose
    // untuk bottom nav agar back stack tiap tab tersimpan (popUpTo start + saveState/restoreState).
    val onBuyerBottomNavigate: (String) -> Unit = { route ->
        if (route in BUYER_BOTTOM_NAV_DESTINATIONS) {
            navController.navigate(route) {
                popUpTo(Routes.BUYER_MARKETPLACE) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
        // TODO: tab Pesanan/Profil belum punya destination.
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
                onChatClick = { /* TODO: navigasi ke Chat saat screen tersebut dibuat */ },
                onBuyClick = { /* TODO: navigasi ke Checkout saat screen tersebut dibuat */ }
            )
        }
    }
}
