package com.teti2026.smartgreenhouse.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.teti2026.smartgreenhouse.R
import com.teti2026.smartgreenhouse.data.model.UserRole
import com.teti2026.smartgreenhouse.ui.auth.LoginRegisterRoute
import com.teti2026.smartgreenhouse.ui.buyer.ChatRoute
import com.teti2026.smartgreenhouse.ui.buyer.CheckoutRoute
import com.teti2026.smartgreenhouse.ui.buyer.ListingDetailRoute
import com.teti2026.smartgreenhouse.ui.buyer.MapRoute
import com.teti2026.smartgreenhouse.ui.buyer.MarketplaceRoute
import com.teti2026.smartgreenhouse.ui.buyer.NotificationRoute
import com.teti2026.smartgreenhouse.ui.buyer.OrderHistoryRoute
import com.teti2026.smartgreenhouse.ui.buyer.OrderSuccessRoute
import com.teti2026.smartgreenhouse.ui.buyer.OrderStatus
import com.teti2026.smartgreenhouse.ui.buyer.ProfileBuyerRoute
import com.teti2026.smartgreenhouse.ui.buyer.ReviewRoute
import com.teti2026.smartgreenhouse.ui.buyer.sampleNearbyFarms
import com.teti2026.smartgreenhouse.ui.buyer.sampleOrderHistory
import com.teti2026.smartgreenhouse.ui.farmer.DashboardFarmerRoute
import com.teti2026.smartgreenhouse.ui.farmer.NotificationFarmerRoute
import com.teti2026.smartgreenhouse.ui.farmer.ProfileFarmerRoute
import com.teti2026.smartgreenhouse.ui.farmer.chat.ChatListRoute
import com.teti2026.smartgreenhouse.ui.farmer.chat.FarmerChatRoute
import com.teti2026.smartgreenhouse.ui.farmer.history.ImageHistoryRoute
import com.teti2026.smartgreenhouse.ui.farmer.history.sampleImageAnalysisDetails
import com.teti2026.smartgreenhouse.ui.farmer.listing.CreateListingFormState
import com.teti2026.smartgreenhouse.ui.farmer.listing.CreateListingRoute
import com.teti2026.smartgreenhouse.ui.farmer.scan.ScanAnalysisResult
import com.teti2026.smartgreenhouse.ui.farmer.scan.ScanPlantRoute
import com.teti2026.smartgreenhouse.ui.farmer.setup.GreenhouseSetupDataRoute
import com.teti2026.smartgreenhouse.ui.farmer.setup.GreenhouseSetupLocationRoute
import com.teti2026.smartgreenhouse.ui.farmer.setup.GreenhouseSetupPairingRoute
import com.teti2026.smartgreenhouse.ui.farmer.setup.rememberGreenhouseSetupStateHolder

private val BUYER_BOTTOM_NAV_DESTINATIONS =
    setOf(Routes.BUYER_MARKETPLACE, Routes.BUYER_MAP, Routes.BUYER_ORDERS, Routes.BUYER_PROFILE)
private val FARMER_BOTTOM_NAV_DESTINATIONS =
    setOf(Routes.FARMER_DASHBOARD, Routes.FARMER_IMAGE_HISTORY, Routes.FARMER_CHAT, Routes.FARMER_PROFILE)

/**
 * Graf navigasi utama, lihat `docs/SDD.md §6`. Saat ini login → Dashboard App Petani atau
 * tab Pasar/Peta/Pesanan App Pembeli yang tersambung; sisa farmerGraph & tab Profil menyusul
 * saat screen lain dibuat.
 */
@Composable
fun GreenhouseNavGraph(
    navController: NavHostController = rememberNavController()
) {
    // Menampung hasil pindaian tanaman on-demand terakhir (ScanPlantRoute) untuk ditransfer ke
    // Buat Listing — tidak lewat NavArgs karena ScanAnalysisResult bukan objek statis yang bisa
    // di-resolve by-id (beda dari sampleImageAnalysisDetails). Cukup bertahan selama sesi
    // navigasi (hilang saat proses mati), konsisten dengan seluruh state lain di app ini yang
    // belum persisten (TODO: MOB-T09/T10 untuk penyimpanan sungguhan).
    var lastScanResult by remember { mutableStateOf<ScanAnalysisResult?>(null) }

    // Handler bersama tab bawah App Pembeli (Pasar <-> Peta <-> Pesanan <-> Profil): pola standar
    // Navigation Compose untuk bottom nav agar back stack tiap tab tersimpan
    // (popUpTo start + saveState/restoreState).
    val onBuyerBottomNavigate: (String) -> Unit = { route ->
        if (route == Routes.BUYER_MARKETPLACE) {
            // "navigate(X){ popUpTo(X){saveState=true}; restoreState=true }" ke diri sendiri
            // (X == X) — dan juga popBackStack(X) ke entry yang sudah ada — keduanya terbukti
            // tidak reliable di sini: back stack berubah tapi NavHost tidak memicu perpindahan
            // tampilan (bug: tombol "Kembali ke Beranda" & tab "Pasar" tidak berpindah layar).
            // Solusi paling pasti: hapus TOTAL entry Marketplace lama (inclusive = true) lalu
            // push instance BARU — pola sama persis dengan navigate(BUYER_MARKETPLACE){
            // popUpTo(LOGIN){inclusive=true} } saat login yang sudah terbukti selalu berhasil.
            // Konsekuensi: state lokal Marketplace (search/filter) ikut ter-reset setiap kembali
            // ke tab ini — dapat diterima karena datanya masih sampel statis.
            navController.navigate(Routes.BUYER_MARKETPLACE) {
                popUpTo(Routes.BUYER_MARKETPLACE) { inclusive = true }
                launchSingleTop = true
            }
        } else if (route in BUYER_BOTTOM_NAV_DESTINATIONS) {
            navController.navigate(route) {
                popUpTo(Routes.BUYER_MARKETPLACE) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    // Sama seperti [onBuyerBottomNavigate], untuk tab bawah App Petani. Dashboard, Riwayat,
    // Pesan & Profil adalah tab persisten (pakai popUpTo+saveState/restoreState). "Tambah"
    // (FARMER_CREATE_LISTING) BUKAN tab persisten (form transaksional dengan tombol back
    // sendiri, sesuai desain Stitch "Buat Listing"), jadi di-push biasa lewat navigate() agar
    // kembali dengan back-stack normal, bukan tab-switch.
    val onFarmerBottomNavigate: (String) -> Unit = { route ->
        if (route == Routes.FARMER_CREATE_LISTING_BASE || route == Routes.FARMER_SCAN_PLANT) {
            navController.navigate(route)
        } else if (route in FARMER_BOTTOM_NAV_DESTINATIONS) {
            navController.navigate(route) {
                popUpTo(Routes.FARMER_DASHBOARD) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
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
                onNotificationsClick = { navController.navigate(Routes.FARMER_NOTIFICATIONS) },
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
                // "Buat Listing" FAB di Riwayat Citra memicu pindaian tanaman on-demand lewat
                // kamera HP (bukan langsung ke form Buat Listing kosong) — lihat diskusi sebelum
                // fitur ini dibuat: petani ingin tahu health_score seketika tanpa menunggu siklus
                // ESP32-CAM, mis. saat melihat gejala penyakit di lapangan.
                onCreateListingClick = { navController.navigate(Routes.FARMER_SCAN_PLANT) },
                onBackClick = { navController.popBackStack() },
                onBottomNavigate = onFarmerBottomNavigate
            )
        }
        composable(Routes.FARMER_SCAN_PLANT) {
            ScanPlantRoute(
                onCloseClick = { navController.popBackStack() },
                onSaveToHistoryClick = {
                    // TODO: simpan CropImage ke Firestore via FirestoreRepository saat
                    // MOB-T09/T10 dikerjakan (padanan hasil ESP32-CAM, tapi berasal dari kamera
                    // HP). Untuk sekarang langsung kembali ke Riwayat Citra.
                    navController.popBackStack()
                },
                onCreateListingClick = { result ->
                    lastScanResult = result
                    navController.navigate(Routes.FARMER_CREATE_LISTING_BASE)
                }
            )
        }
        composable(Routes.FARMER_CHAT) {
            ChatListRoute(
                onConversationClick = { conversation ->
                    navController.navigate(Routes.farmerChatConversation(conversation.id))
                },
                onNotificationsClick = { navController.navigate(Routes.FARMER_NOTIFICATIONS) },
                onBottomNavigate = onFarmerBottomNavigate
            )
        }
        composable(Routes.FARMER_NOTIFICATIONS) {
            NotificationFarmerRoute(
                onBackClick = { navController.popBackStack() },
                // "" (bukan salah satu Routes.FARMER_* tab): lihat catatan di
                // Routes.FARMER_NOTIFICATIONS & NotificationFarmerRoute — tidak ada item
                // FarmerBottomNavBar yang tersorot aktif di sini.
                currentBottomNavRoute = "",
                onBottomNavigate = onFarmerBottomNavigate
            )
        }
        composable(
            route = Routes.FARMER_CHAT_CONVERSATION,
            arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId").orEmpty()
            FarmerChatRoute(
                conversationId = conversationId,
                // Tombol back kembali ke Pesan (daftar percakapan) di back stack, bukan
                // destination tetap — sama seperti pola [Routes.BUYER_CHAT].
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(Routes.FARMER_PROFILE) {
            ProfileFarmerRoute(
                onMyGreenhousesClick = {
                    navController.navigate(Routes.FARMER_SETUP_GREENHOUSE_DATA)
                },
                onNotificationsClick = { navController.navigate(Routes.FARMER_NOTIFICATIONS) },
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
            // [lastScanResult] terisi hanya saat masuk dari "Buat Listing dari Hasil Ini" di
            // Pindai Tanaman — Hasil Analisis (lihat destination Routes.FARMER_SCAN_PLANT di
            // atas). Diprioritaskan di atas [prefillDetail] karena keduanya tidak pernah terisi
            // bersamaan (dua entry point berbeda ke Buat Listing), lalu di-reset supaya tidak
            // ikut ter-prefill lagi saat kembali ke sini lewat jalur lain (mis. tombol "+").
            val scanResult = lastScanResult
            val initialFormState = if (scanResult != null) {
                lastScanResult = null
                CreateListingFormState(
                    productName = scanResult.productName,
                    healthScore = scanResult.healthScore,
                    photoUris = listOf(scanResult.imageUri),
                    description = stringResource(
                        R.string.scan_result_prefill_description,
                        scanResult.ripenessLabel,
                        scanResult.healthLabel
                    )
                )
            } else {
                prefillDetail?.let { detail ->
                    CreateListingFormState(
                        productName = detail.productName,
                        healthScore = detail.healthScore,
                        photoUris = listOf(Uri.parse(detail.imageUrl)),
                        description = detail.aiNote
                    )
                }
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
                onNotificationsClick = { navController.navigate(Routes.BUYER_NOTIFICATIONS) },
                onBottomNavigate = onBuyerBottomNavigate
            )
        }
        composable(Routes.BUYER_NOTIFICATIONS) {
            NotificationRoute(
                onBackClick = { navController.popBackStack() },
                // "" (bukan salah satu Routes.BUYER_* tab): lihat catatan di Routes.BUYER_NOTIFICATIONS
                // & NotificationRoute — tidak ada item BuyerBottomNavBar yang tersorot aktif di sini.
                currentBottomNavRoute = "",
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
                // Sama seperti tab "Pasar" di BuyerBottomNavBar — pakai handler yang sama
                // (popBackStack ke Marketplace) alih-alih navigate+popUpTo ke diri sendiri.
                onSeeAllClick = { onBuyerBottomNavigate(Routes.BUYER_MARKETPLACE) },
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
        composable(Routes.BUYER_PROFILE) {
            ProfileBuyerRoute(
                // Riwayat Pesanan & Marketplace/Peta adalah tab bottom-nav lain — pakai handler
                // yang sama agar back stack tab konsisten (popUpTo start + saveState/restoreState).
                onOrdersClick = { onBuyerBottomNavigate(Routes.BUYER_ORDERS) },
                onNotificationsClick = { navController.navigate(Routes.BUYER_NOTIFICATIONS) },
                // "Keluar" (setelah konfirmasi dialog "Ya" di ProfileBuyerScreen): kembali ke Login,
                // seluruh back stack App Pembeli dibersihkan — pola sama seperti onLogoutClick
                // ProfileFarmerRoute di atas.
                onLogoutConfirmed = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onBottomNavigate = onBuyerBottomNavigate
            )
        }
    }
}
