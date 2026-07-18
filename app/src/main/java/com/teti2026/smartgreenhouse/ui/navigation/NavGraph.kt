package com.teti2026.smartgreenhouse.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.teti2026.smartgreenhouse.ui.auth.LoginRegisterRoute
import com.teti2026.smartgreenhouse.ui.buyer.MarketplaceRoute

/**
 * Graf navigasi utama, lihat `docs/SDD.md §6`. Saat ini hanya login → marketplace pembeli
 * yang tersambung; farmerGraph/buyerGraph penuh (bottom nav per tab, dsb.) menyusul saat
 * screen lain dibuat.
 */
@Composable
fun GreenhouseNavGraph(
    navController: NavHostController = rememberNavController()
) {
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
                onListingClick = { /* TODO: navigasi ke Detail Listing saat screen tersebut dibuat */ },
                onBottomNavigate = { route ->
                    if (route != Routes.BUYER_MARKETPLACE) {
                        // TODO: tab Peta/Pesanan/Profil belum punya destination.
                    }
                }
            )
        }
    }
}
