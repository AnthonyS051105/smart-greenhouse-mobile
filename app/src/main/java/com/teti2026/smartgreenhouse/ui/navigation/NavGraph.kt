package com.teti2026.smartgreenhouse.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.teti2026.smartgreenhouse.ui.auth.LoginRegisterRoute
import com.teti2026.smartgreenhouse.ui.buyer.ListingDetailRoute
import com.teti2026.smartgreenhouse.ui.buyer.MapRoute
import com.teti2026.smartgreenhouse.ui.buyer.MarketplaceRoute
import com.teti2026.smartgreenhouse.ui.buyer.sampleNearbyFarms

private val BUYER_BOTTOM_NAV_DESTINATIONS = setOf(Routes.BUYER_MARKETPLACE, Routes.BUYER_MAP)

/**
 * Graf navigasi utama, lihat `docs/SDD.md §6`. Saat ini login → tab Pasar & Peta App Pembeli
 * yang tersambung; farmerGraph penuh serta tab Pesanan/Profil Pembeli menyusul saat screen
 * lain dibuat.
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

    NavHost(navController = navController, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) {
            LoginRegisterRoute(
                onLoginClick = {
                    navController.navigate(Routes.BUYER_MARKETPLACE) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
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
