package com.teti2026.smartgreenhouse.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.teti2026.smartgreenhouse.ui.farmer.ProfileFarmerRoute
import com.teti2026.smartgreenhouse.ui.farmer.history.ImageHistoryRoute
import com.teti2026.smartgreenhouse.ui.farmer.history.sampleImageAnalysisDetails
import com.teti2026.smartgreenhouse.ui.farmer.listing.CreateListingFormState
import com.teti2026.smartgreenhouse.ui.farmer.listing.CreateListingRoute
import com.teti2026.smartgreenhouse.ui.farmer.setup.GreenhouseSetupDataRoute
import com.teti2026.smartgreenhouse.ui.farmer.setup.GreenhouseSetupLocationRoute
import com.teti2026.smartgreenhouse.ui.farmer.setup.GreenhouseSetupPairingRoute
import com.teti2026.smartgreenhouse.ui.farmer.setup.rememberGreenhouseSetupStateHolder

private val BUYER_BOTTOM_NAV_DESTINATIONS = setOf(Routes.BUYER_MARKETPLACE, Routes.BUYER_MAP, Routes.BUYER_ORDERS)
private val FARMER_BOTTOM_NAV_DESTINATIONS =
    setOf(Routes.FARMER_DASHBOARD, Routes.FARMER_IMAGE_HISTORY, Routes.FARMER_PROFILE)

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

    // Sama seperti [onBuyerBottomNavigate], untuk tab bawah App Petani. Dashboard, Riwayat &
    // Profil adalah tab persisten (pakai popUpTo+saveState/restoreState) — tab Pesan belum
    // punya destination. "Tambah" (FARMER_CREATE_LISTING) BUKAN tab persisten (form
    // transaksional dengan tombol back sendiri, sesuai desain Stitch "Buat Listing"), jadi
    // di-push biasa lewat navigate() agar kembali dengan back-stack normal, bukan tab-switch.
    val onFarmerBottomNavigate: (String) -> Unit = { route ->
        if (route == Routes.FARMER_CREATE_LISTING_BASE) {
            navController.navigate(route)
        } else if (route in FARMER_BOTTOM_NAV_DESTINATIONS) {
            navController.navigate(route) {
                popUpTo(Routes.FARMER_DASHBOARD) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
        // TODO: tab Pesan belum punya destination.
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
                            // saat false, tujuan adalah flow Setup Greenhouse (langkah 1/3).
                            val hasFarmSetup = true
                            val destination = if (hasFarmSetup) {
                                Routes.FARMER_DASHBOARD
                            } else {
                                Routes.FARMER_SETUP_GREENHOUSE_DATA
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
                onImageHistoryClick = { onFarmerBottomNavigate(Routes.FARMER_IMAGE_HISTORY) },
                onBottomNavigate = onFarmerBottomNavigate
            )
        }
        // Detail Analisis Citra BUKAN destination NavHost terpisah — dirender sebagai
        // `ModalBottomSheet` overlay DI ATAS composable ini (state `selectedImageId` di-hoist di
        // dalam `ImageHistoryRoute`), supaya grid tetap ada di composition sebagai layar di
        // belakang sheet (scrim menampilkan konten Riwayat asli, bukan layar kosong). Pendekatan
        // awal (destination NavHost terpisah via `dialog()`) menyebabkan window Dialog kosong
        // bertumpuk, karena `ModalBottomSheet` M3 sudah membuat `Dialog`-nya sendiri.
        composable(Routes.FARMER_IMAGE_HISTORY) {
            ImageHistoryRoute(
                onCreateListingFromImage = { selectedDetail ->
                    // Transfer data (gambar, deskripsi, skor kesehatan) ke Buat Listing lewat
                    // query param [prefillImageId] pada route — [CreateListingFormState]
                    // sungguhan dibangun di composable Routes.FARMER_CREATE_LISTING di bawah
                    // (pola sama seperti resolve listingId/farmId di destination Pembeli).
                    navController.navigate(Routes.farmerCreateListingFromImage(selectedDetail.id))
                },
                onCreateListingClick = { navController.navigate(Routes.FARMER_CREATE_LISTING_BASE) },
                onBackClick = { navController.popBackStack() },
                onBottomNavigate = onFarmerBottomNavigate
            )
        }
        composable(Routes.FARMER_PROFILE) {
            ProfileFarmerRoute(
                onMyGreenhousesClick = {
                    navController.navigate(Routes.FARMER_SETUP_GREENHOUSE_DATA)
                },
                onLogoutClick = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onBottomNavigate = onFarmerBottomNavigate
            )
        }
        // Setup Greenhouse — flow 3 langkah. [GreenhouseSetupStateHolder] di-scope ke
        // NavBackStackEntry milik Routes.FARMER_SETUP_GREENHOUSE_DATA (langkah 1) sehingga
        // ketiga step berbagi instance yang SAMA — pola standar Navigation Compose untuk
        // shared state antar-destination dalam satu alur. Instance baru otomatis dibuat tiap
        // kali flow ini dimulai ulang (dari Login atau "Greenhouse Saya" di Profil), karena
        // back stack entry lama sudah di-pop (popUpTo inclusive) saat flow sebelumnya selesai.
        composable(Routes.FARMER_SETUP_GREENHOUSE_DATA) { backStackEntry ->
            val setupStateHolder = rememberGreenhouseSetupStateHolder(backStackEntry)
            GreenhouseSetupDataRoute(
                stateHolder = setupStateHolder,
                onBackClick = { navController.popBackStack() },
                onNextClick = { navController.navigate(Routes.FARMER_SETUP_GREENHOUSE_LOCATION) }
            )
        }
        composable(Routes.FARMER_SETUP_GREENHOUSE_LOCATION) {
            val parentEntry = remember(it) {
                navController.getBackStackEntry(Routes.FARMER_SETUP_GREENHOUSE_DATA)
            }
            val setupStateHolder = rememberGreenhouseSetupStateHolder(parentEntry)
            GreenhouseSetupLocationRoute(
                stateHolder = setupStateHolder,
                onBackClick = { navController.popBackStack() },
                onNextClick = { navController.navigate(Routes.FARMER_SETUP_GREENHOUSE_PAIRING) }
            )
        }
        composable(Routes.FARMER_SETUP_GREENHOUSE_PAIRING) {
            val parentEntry = remember(it) {
                navController.getBackStackEntry(Routes.FARMER_SETUP_GREENHOUSE_DATA)
            }
            val setupStateHolder = rememberGreenhouseSetupStateHolder(parentEntry)
            GreenhouseSetupPairingRoute(
                stateHolder = setupStateHolder,
                onBackClick = { navController.popBackStack() },
                onFinishClick = {
                    // TODO: simpan Farm + Plot ke Firestore via FirestoreRepository saat
                    // MOB-T07 dikerjakan. Untuk sekarang langsung navigasi ke Dashboard.
                    navController.navigate(Routes.FARMER_DASHBOARD) {
                        popUpTo(Routes.FARMER_SETUP_GREENHOUSE_DATA) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = Routes.FARMER_CREATE_LISTING,
            arguments = listOf(
                navArgument("prefillImageId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            // [prefillImageId] terisi hanya saat masuk dari tombol "Buat Listing dari Data Ini"
            // di Detail Analisis Citra (lihat Routes.farmerCreateListingFromImage) — `null` berarti
            // masuk dari tombol "+" navbar biasa, form pakai data sampel default seperti semula.
            val prefillImageId = backStackEntry.arguments?.getString("prefillImageId")
            val prefillDetail = prefillImageId?.let { sampleImageAnalysisDetails[it] }
            val initialFormState = prefillDetail?.let { detail ->
                CreateListingFormState(
                    productName = detail.productName,
                    healthScore = detail.healthScore,
                    photoUris = listOf(Uri.parse(detail.imageUrl)),
                    description = detail.aiNote
                )
            }

            CreateListingRoute(
                onBackClick = { navController.popBackStack() },
                onPublishClick = {
                    // TODO: simpan Listing ke Firestore via FirestoreRepository.createListing
                    // (MOB-T13) begitu form & upload Cloudinary sungguhan dikerjakan. Untuk
                    // sekarang langsung kembali ke Dashboard setelah "Publikasikan".
                    navController.popBackStack()
                },
                initialFormState = initialFormState
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
