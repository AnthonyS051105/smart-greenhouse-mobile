package com.teti2026.smartgreenhouse.ui.farmer.history

/**
 * Data statis Riwayat Citra & Detail Analisis, padanan `crop_images` (`docs/data-contracts.md
 * §3.6`) sebelum FirestoreRepository.getCropImages(plotId) tersambung (menyusul MOB-T09/MOB-T10).
 * [sampleImageAnalysisDetails] di-keyed dengan [CropImageHistoryItem.id] yang sama supaya tap
 * kartu grid bisa resolve ke detail terkait (pola sama seperti `sampleNearbyFarms` di App Pembeli).
 */
val sampleCropImageHistoryItems = listOf(
    CropImageHistoryItem(
        id = "img-001",
        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDp0QPR5i3ejug1piqtft8ZzJY33_h7_m5dOG1CwmdAhmBD2BUE59zhaetp0Mqp14EnkBAUrVGSdVBgX3eNo_ac5G06iEJB42WeziIWdF2tR21ocdDOWonEJZ04-IRUza0mgkQ737yy8GsZxWgbryr0LCP-Q8w7KrdKNT03HG2Z-4YqOZYTfoDzJx7B4NLtta9TosFINLHKsg6ioRMkNu10TqOu_iJh8EE42VjB6MqpQuK3ntiKhrHrwTPn-bcWv4k3WwKd1ZLFwA",
        category = ImageHealthCategory.GOOD,
        timestampLabel = "Hari ini, 08:30",
        plotLabel = "Cabai Rawit - Blok A"
    ),
    CropImageHistoryItem(
        id = "img-002",
        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCjnmT8L22hznsipY2gB4v_IxqZrUnbxJWUdXVYA8AvVmdji0SEbiTMicN7aBFmjW-jZ7YS-RCf7HZcS9xjulCX2ODSO5iQy1XD4VzlZYZNbbk2yWEgYnx5vVSpuXUZWODAufeArtSxp4WvQrIU5E6FDmiHx-zUIQVA_i4wbvO5M3z8UACo4tPjQzzL3RbEb7FFJGaTAVY1DW09tbXXTyLTxe6uFcR96-6fQoxpCfPjTEPoEdH2R1xvZyAosmX8wXsK0ZjycaLn2g",
        category = ImageHealthCategory.MEDIUM,
        timestampLabel = "Kemarin, 14:15",
        plotLabel = "Tomat Beef - Blok B"
    ),
    CropImageHistoryItem(
        id = "img-003",
        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAm3AmIGoIgCeHtOW-gPyJgGq7Dszp5kz_7HfQIuSfkG86spjT9eikEE8DfgypYlV7-t4NpxxbvkjSuAMUbPVt-L9KLaEsPaeCbslmJD8Tp-H12RlEoTjPjykp6sYkvbUgP6ZFACteRJBTOa4K7C79wbfZe5lq-05DOggcm4kBexVRnaUtfUy-9B9zhRwlXHfH2JQL7p447xp0T9QnBSWnJOeLebN6oFIb_K6taCAiiaaalZYSeCJ6retavrBMZ7P6gw4RUJzFmkA",
        category = ImageHealthCategory.GOOD,
        timestampLabel = "Kemarin, 09:00",
        plotLabel = "Tomat Ceri - Blok C"
    ),
    CropImageHistoryItem(
        id = "img-004",
        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCcY9L7qQP5Kdsc59kl-10OmtLybcsOQ-g22DSCWwvesKCB1inhef-UaGkuN1ECk3_zb3wFi0sRr3kUgNFuUEFNZZZpkDXI0VJPRRRtIIKtU83jiVgzbFQnpWCPWq5oAsAdL6fhZlyDIcoNCzmA3hd3mxNTgqlOiErfNJF5sBVO8ExH66eppaUAQKdA1QetQfqgo7eAWw-VXXCyjxKXkH0QSSz6nZLEnqb2QlM5OvwDEnFdeVtgmxMddSAG3J3TGxuK9fQDqRV_bA",
        category = ImageHealthCategory.SICK,
        timestampLabel = "2 Hari lalu",
        plotLabel = "Cabai Merah - Blok A"
    ),
    CropImageHistoryItem(
        id = "img-005",
        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBRBMXRRge_-NhwxkaBq6xkyRZHhMAtKJnkW70CZCYGwY2ZXzTtB_qRt79ov0zYwHuBIc0RnKhA4KuHI8hq55_p-Rt-qei6Heb2V5Xk1RUec2dNi64S12VIlCwoYtIcVjMS75hCudm8S01guiOtmKX1cr3_7XycPL2YaRk1NsK52bQeuwFTnw3YbBEr4pA6huLRGOxUSSzcgkeiVjGgIu72gSoSOKCY_p01oFKmoYVYN4Y0WEEYpAnXYCcHEdLkVnlKZwy2dfGydw",
        category = ImageHealthCategory.GOOD,
        timestampLabel = "3 Hari lalu",
        plotLabel = "Bibit Cabai Baru"
    )
)

val sampleImageAnalysisDetails: Map<String, ImageAnalysisDetail> = mapOf(
    "img-001" to ImageAnalysisDetail(
        id = "img-001",
        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuA9GHfS1LsVPyma24NTOrMBD48FwtSyopvpuvTzpDDhcHqR4ulAt5_i6ZomtzeDqb9NK6uUaw2FdGCtI_Gpjc2twJ6D00Hf7ZJ9dEXnQmAE2xUG_j5e5qe4Z5K7r28USwJBckJj8iPPosttj6mMGJh33GKaBJp7uwBf421UUNeD11YLjrCz6VQmNO3yAqbQvRkZHO_zoaMYhAHC_yrNC-nX6re6nHp1uWUFKG4Mk1rh3rcCHRY30BK4lzBbye3MUF0Jj8zo6jVU4g",
        category = ImageHealthCategory.GOOD,
        healthScore = 87.0,
        timestampLabel = "12 Mei 2023, 08:30 WIB",
        aiNote = "Tanaman terlihat sehat dengan pertumbuhan optimal. Tidak terdeteksi hama atau " +
            "penyakit pada area ini. Kelembapan daun tampak normal berdasarkan pantulan cahaya.",
        detectionLocationLabel = "Sektor Utara, Baris 4",
        deviceLabel = "Kamera Drone v2",
        productName = "Cabai Rawit - Blok A"
    ),
    "img-002" to ImageAnalysisDetail(
        id = "img-002",
        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCjnmT8L22hznsipY2gB4v_IxqZrUnbxJWUdXVYA8AvVmdji0SEbiTMicN7aBFmjW-jZ7YS-RCf7HZcS9xjulCX2ODSO5iQy1XD4VzlZYZNbbk2yWEgYnx5vVSpuXUZWODAufeArtSxp4WvQrIU5E6FDmiHx-zUIQVA_i4wbvO5M3z8UACo4tPjQzzL3RbEb7FFJGaTAVY1DW09tbXXTyLTxe6uFcR96-6fQoxpCfPjTEPoEdH2R1xvZyAosmX8wXsK0ZjycaLn2g",
        category = ImageHealthCategory.MEDIUM,
        healthScore = 62.0,
        timestampLabel = "Kemarin, 14:15 WIB",
        aiNote = "Terdeteksi sedikit gejala klorosis (menguning) pada tepi daun. Disarankan " +
            "memantau kelembapan tanah dan asupan nutrisi dalam 2-3 hari ke depan.",
        detectionLocationLabel = "Sektor Timur, Baris 2",
        deviceLabel = "Kamera Genggam",
        productName = "Tomat Beef - Blok B"
    ),
    "img-003" to ImageAnalysisDetail(
        id = "img-003",
        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAm3AmIGoIgCeHtOW-gPyJgGq7Dszp5kz_7HfQIuSfkG86spjT9eikEE8DfgypYlV7-t4NpxxbvkjSuAMUbPVt-L9KLaEsPaeCbslmJD8Tp-H12RlEoTjPjykp6sYkvbUgP6ZFACteRJBTOa4K7C79wbfZe5lq-05DOggcm4kBexVRnaUtfUy-9B9zhRwlXHfH2JQL7p447xp0T9QnBSWnJOeLebN6oFIb_K6taCAiiaaalZYSeCJ6retavrBMZ7P6gw4RUJzFmkA",
        category = ImageHealthCategory.GOOD,
        healthScore = 91.0,
        timestampLabel = "Kemarin, 09:00 WIB",
        aiNote = "Daun tampak segar dengan embun pagi, tidak ada tanda stres air maupun hama. " +
            "Pertumbuhan tergolong sangat baik untuk fase ini.",
        detectionLocationLabel = "Sektor Selatan, Baris 1",
        deviceLabel = "Kamera Drone v2",
        productName = "Tomat Ceri - Blok C"
    ),
    "img-004" to ImageAnalysisDetail(
        id = "img-004",
        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCcY9L7qQP5Kdsc59kl-10OmtLybcsOQ-g22DSCWwvesKCB1inhef-UaGkuN1ECk3_zb3wFi0sRr3kUgNFuUEFNZZZpkDXI0VJPRRRtIIKtU83jiVgzbFQnpWCPWq5oAsAdL6fhZlyDIcoNCzmA3hd3mxNTgqlOiErfNJF5sBVO8ExH66eppaUAQKdA1QetQfqgo7eAWw-VXXCyjxKXkH0QSSz6nZLEnqb2QlM5OvwDEnFdeVtgmxMddSAG3J3TGxuK9fQDqRV_bA",
        category = ImageHealthCategory.SICK,
        healthScore = 28.0,
        timestampLabel = "2 Hari lalu, 10:05 WIB",
        aiNote = "Terdeteksi bercak coklat dan tepi daun menggulung, indikasi kuat penyakit " +
            "layu bakteri. Disarankan segera isolasi tanaman terdampak dan konsultasi PPL.",
        detectionLocationLabel = "Sektor Utara, Baris 6",
        deviceLabel = "Kamera Genggam",
        productName = "Cabai Merah - Blok A"
    ),
    "img-005" to ImageAnalysisDetail(
        id = "img-005",
        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBRBMXRRge_-NhwxkaBq6xkyRZHhMAtKJnkW70CZCYGwY2ZXzTtB_qRt79ov0zYwHuBIc0RnKhA4KuHI8hq55_p-Rt-qei6Heb2V5Xk1RUec2dNi64S12VIlCwoYtIcVjMS75hCudm8S01guiOtmKX1cr3_7XycPL2YaRk1NsK52bQeuwFTnw3YbBEr4pA6huLRGOxUSSzcgkeiVjGgIu72gSoSOKCY_p01oFKmoYVYN4Y0WEEYpAnXYCcHEdLkVnlKZwy2dfGydw",
        category = ImageHealthCategory.GOOD,
        healthScore = 84.0,
        timestampLabel = "3 Hari lalu, 07:40 WIB",
        aiNote = "Bibit baru tumbuh normal dengan warna daun hijau merata. Belum ditemukan " +
            "tanda penyakit maupun kekurangan nutrisi pada fase awal ini.",
        detectionLocationLabel = "Sektor Barat, Baris 3",
        deviceLabel = "Kamera Drone v2",
        productName = "Bibit Cabai Baru"
    )
)
