# SDD — System Design Document (Mobile)

> Repo: **mobile/**. Referensi: `shared/Architecture.md`, `shared/data-contracts.md`, `mobile/SRS.md`, `mobile/UIUX-Flow.md`.
> **Stack UI: Kotlin + Jetpack Compose** (revisi dari XML setelah dikonfirmasi mentor saat coaching).

---

## 1. Arsitektur Aplikasi

Pola **MVVM + Unidirectional Data Flow (UDF)** khas Compose:

```
┌──────────────────────────────────────────────────────┐
│  UI LAYER (@Composable screens)                       │
│  - Petani: DashboardScreen, ControlScreen,            │
│            CreateListingScreen, ImageHistoryScreen    │
│  - Pembeli: MarketplaceScreen, MapScreen,             │
│             ListingDetailScreen, CheckoutScreen       │
│  - Stateless composables + state hoisting             │
└───────────────────────┬──────────────────────────────┘
        collectAsStateWithLifecycle()  │  events (lambda)
┌───────────────────────▼──────────────────────────────┐
│  VIEWMODEL LAYER                                       │
│  - Expose StateFlow<UiState>                          │
│  - Terima event dari UI, panggil Repository           │
└───────────────────────┬──────────────────────────────┘
                        │
┌───────────────────────▼──────────────────────────────┐
│  REPOSITORY LAYER                                     │
│  - AuthRepository (Firebase Auth)                     │
│  - FirestoreRepository (listing, order, chat, dst)    │
│  - BackendRepository (Retrofit → FastAPI)             │
│  - CloudinaryRepository (upload foto)                 │
│  - MapsRepository (Places/Geocoding)                  │
└───────────────────────┬──────────────────────────────┘
                        │
┌───────────────────────▼──────────────────────────────┐
│  DATA SOURCES                                        │
│  Firebase SDK · Retrofit API · Cloudinary · Room     │
└──────────────────────────────────────────────────────┘
```

**Prinsip Compose yang dipakai:**
- **State hoisting** — composable UI stateless; state dipegang ViewModel.
- **Single source of truth** — `StateFlow<UiState>` di ViewModel.
- **Unidirectional data flow** — state turun (ViewModel→UI), event naik (UI→ViewModel).

---

## 2. Modul & Package

```
com.teti.greenhouse
├── ui/
│   ├── theme/           # Color.kt, Type.kt, Theme.kt (Material 3)
│   ├── components/      # composable reusable (ListingCard, HealthBadge, SensorChart, ...)
│   ├── navigation/      # NavGraph.kt, Routes.kt, bottom bar per role
│   ├── auth/            # LoginScreen.kt, RegisterScreen.kt
│   ├── farmer/          # DashboardScreen, ControlScreen, ImageHistoryScreen, CreateListingScreen
│   ├── buyer/           # MarketplaceScreen, MapScreen, ListingDetailScreen, CheckoutScreen, ReviewScreen
│   └── common/          # ChatScreen, ProfileScreen, NotificationScreen
├── viewmodel/           # satu VM per screen/fitur
├── repository/
├── data/
│   ├── remote/          # Retrofit API service + DTO
│   ├── firebase/        # Firestore & Auth helpers
│   ├── local/           # Room (cache)
│   └── model/           # domain models (User, Listing, Order, ...)
├── util/                # helpers (token, formatter, image)
└── di/                  # dependency wiring (Hilt disarankan; manual DI juga cukup)
```

---

## 3. Model Data (Domain)

Selaras dengan `data-contracts.md §3`.

```kotlin
data class Listing(
    val id: String,
    val farmId: String,
    val plotId: String,
    val cropType: String,
    val quantityKg: Double,
    val pricePerKg: Long,
    val healthScore: Double,
    val harvestDate: String,
    val status: String,       // "available" | "sold"
    val createdAt: String
)

data class Order(
    val id: String,
    val buyerUid: String,
    val listingId: String,
    val quantityKg: Double,
    val totalPrice: Long,
    val status: String,       // pending|confirmed|completed|cancelled
    val createdAt: String
)
```

---

## 4. Desain Antarmuka Data (Repository)

### 4.1 AuthRepository (Firebase Auth)
- `register(email, pass, role): Result<User>`
- `login(email, pass): Result<User>`
- `currentUser(): User?`, `logout()`

### 4.2 FirestoreRepository
- `getListings(filter): Flow<List<Listing>>`
- `createListing(listing)`, `updateListing(...)`
- `getOrders(buyerUid)`, `createOrder(order)`
- `getMessages(listingId): Flow<List<ChatMessage>>` (realtime snapshot listener)
- `sendMessage(msg)`
- `getFarmsForMap(): List<FarmMarker>`
- `getSensorReadings(plotId, range): List<SensorReading>`
- `createReview(review)`

### 4.3 BackendRepository (Retrofit → FastAPI)
- `triggerActuator(plotId, actuator, action, durationSec)` → `POST /irrigation/trigger`
- `getIrrigationRecommendation(plotId)` → `GET /plots/{id}/irrigation/recommendation`
- `autoFillHealthScore(plotId)` → `POST /listings/auto-fill-health-score`
- `analyzeVision(imageId)` → `POST /vision/analyze`
- Setiap call menyertakan header `Authorization: Bearer <idToken>`.

### 4.4 CloudinaryRepository
- `uploadImage(bytes): String (url)`

### 4.5 MapsRepository
- `autocomplete(query)`, `geocode(address)`, `reverseGeocode(lat,lng)`

---

## 5. State Management (Compose)

Setiap screen punya ViewModel yang mengekspos `StateFlow<UiState>`:

```kotlin
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
    data object Empty : UiState<Nothing>
}

class MarketplaceViewModel(
    private val repo: FirestoreRepository
) : ViewModel() {
    private val _state = MutableStateFlow<UiState<List<Listing>>>(UiState.Loading)
    val state: StateFlow<UiState<List<Listing>>> = _state.asStateFlow()

    fun loadListings(filter: ListingFilter) { /* ... */ }
}
```

Di composable:
```kotlin
@Composable
fun MarketplaceScreen(vm: MarketplaceViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    when (val s = state) {
        is UiState.Loading -> LoadingIndicator()
        is UiState.Empty   -> EmptyView("Belum ada listing")
        is UiState.Error   -> ErrorView(s.message) { vm.retry() }
        is UiState.Success -> ListingList(s.data)
    }
}
```

---

## 6. Navigasi (Navigation Compose)

```kotlin
// Routes.kt
object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    // Farmer
    const val FARMER_DASHBOARD = "farmer/dashboard"
    const val FARMER_CONTROL = "farmer/control"
    const val FARMER_IMAGES = "farmer/images"
    const val FARMER_CREATE_LISTING = "farmer/listing/create"
    // Buyer
    const val BUYER_MARKETPLACE = "buyer/marketplace"
    const val BUYER_MAP = "buyer/map"
    const val BUYER_DETAIL = "buyer/listing/{listingId}"
    const val BUYER_CHECKOUT = "buyer/checkout/{listingId}"
    // Common
    const val CHAT = "chat/{listingId}"
    const val PROFILE = "profile"
}
```

**Struktur graf:**
```
NavHost(startDestination = LOGIN)
 ├── login / register
 ├── farmerGraph  (jika role = farmer)   → Scaffold + NavigationBar
 │     Dashboard | Listing | Chat | Profil
 └── buyerGraph   (jika role = buyer)    → Scaffold + NavigationBar
       Marketplace | Peta | Pesanan | Profil
```

> Tidak memakai Fragment. Setiap "screen" adalah `@Composable` destination.

---

## 7. Komponen Compose Kunci

| Kebutuhan | Padanan Compose | Untuk |
|-----------|-----------------|-------|
| Daftar | `LazyColumn` / `LazyVerticalGrid` | listing, chat, riwayat |
| Kartu | `Card` (M3) | listing, status aktuator |
| Navigasi bawah | `NavigationBar` + `NavigationBarItem` | menu per role |
| Bottom sheet | `ModalBottomSheet` (M3) | detail farm saat tap marker |
| Peta | `GoogleMap` dari **maps-compose** | peta marketplace |
| Grafik | **Vico** / **YCharts** (Compose-native) | grafik sensor |
| Gambar | `AsyncImage` (**Coil**) | foto tanaman/produk |
| Badge skor | `Badge` / custom `Surface` berwarna | health_score |
| Filter | `FilterChip` (M3) | filter marketplace |
| Feedback | `Snackbar` via `SnackbarHostState` | error/sukses |

> Catatan migrasi: MPAndroidChart (View-based) diganti **Vico/YCharts** (Compose-native); Glide diganti **Coil**; `SupportMapFragment` diganti composable `GoogleMap` dari `maps-compose`.

---

## 8. Penanganan State & Error

- `UiState` sealed interface (Loading/Success/Error/Empty) per screen.
- Error jaringan → `Snackbar` + tombol "Coba lagi".
- Token expired → refresh via Firebase; gagal → navigasi ke Login (clear back stack).
- Gunakan `collectAsStateWithLifecycle()` agar collection sadar lifecycle.

---

## 9. Integrasi Detail

| Fitur | Sumber Data | Catatan |
|-------|-------------|---------|
| Login | Firebase Auth | role disimpan di `users` Firestore |
| Marketplace list | Firestore `listings` | filter client/server-side |
| Detail sensor grafik | Firestore `sensor_readings` | query by plot_id + range |
| Kontrol aktuator | Backend REST | hanya backend bisa MQTT |
| Auto-fill health_score | Backend REST | sebelum simpan listing ke Firestore |
| Chat | Firestore `chat_messages` | realtime snapshot listener → `Flow` |
| Peta | Firestore `farms` + maps-compose | marker dari koordinat farm |
| Foto produk | Cloudinary | simpan URL ke Firestore |
| Notifikasi | FCM | token per user |

---

## 10. Strategi Pengujian

| Level | Uji |
|-------|-----|
| Unit | ViewModel logic (StateFlow emissions), mapping DTO↔domain. |
| Integrasi | Repository ↔ Firestore/Backend. |
| UI (Compose) | `createAndroidComposeRule` — alur kritis: login→listing→checkout. |
| Manual | Demo end-to-end dengan perangkat IoT nyata. |

---

## 11. Diagram UML (yang perlu digambar)

1. **Use Case Diagram** — aktor Petani & Pembeli (lihat SRS §3).
2. **Class Diagram** — domain models + repository + ViewModel.
3. **Sequence Diagram:**
   - Login → routing by role.
   - Sensor → (backend/AI) → dashboard update.
   - Buat listing → auto-fill health_score → simpan Firestore → muncul di marketplace.
   - Pembeli → chat → checkout → notifikasi → rating.
   - Peta: tap marker → daftar komoditas → detail.

> Gambar dengan draw.io/PlantUML/Lucidchart, lampirkan ke materi presentasi (syarat wajib).
> Catatan: pada Compose, "Screen" = composable destination (bukan Activity/Fragment) — sesuaikan notasi di diagram.
