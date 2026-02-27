Ide ini adalah **klasik namun selalu dibutuhkan**. Hampir semua server Survival SMP membutuhkan fitur ini. Namun, karena sudah banyak plugin sejenis (seperti *OreExcavator*, *TreeAssist*, *McMMO*), kunci agar plugin kamu sukses di Modrinth adalah **Performa**, **Keamanan**, dan **Fitur Visual** yang tidak dimiliki plugin lama.

Berikut adalah konsep pengembangan plugin **"SwiftHarvest"** (nama sementara) yang modern, ringan, dan siap rilis:

---

### 1. Nama & Branding
*   **Nama Plugin:** `SwiftHarvest`
*   **Tagline:** "Mine Faster, Lag Less. The Ultimate Vein & Timber Solution."
*   **Kategori:** Utility / Quality of Life
*   **Lisensi:** **MIT License** (Standar Modrinth, membangun kepercayaan).
*   **Target Platform:** PaperMC / Purpur (Wajib, untuk performa async).

### 2. Mekanisme Utama (Core Mechanics)

#### A. VeinMiner (Penambangan Urat)
*   **Aktivasi:** Pemain memegang **Pickaxe** + **Sneak** + **Break Ore**.
*   **Logika:**
    1.  Deteksi blok yang di-break.
    2.  Cari blok sejenis (ID sama) di sekitar (radius X, Y, Z).
    3.  Hanya blok yang **tersambung secara fisik** (bertetangga) yang ikut hancur.
    4.  **Batas Maksimum:** Misal maksimal 64 blok per sekali break (mencegah lag & exploit).
*   **Syarat Alat:** Bisa dikonfigurasi (misal: Hanya Diamond Pickaxe ke atas, atau semua Pickaxe).

#### B. Timber (Penebangan Pohon)
*   **Aktivasi:** Pemain memegang **Axe** + **Sneak** + **Break Log**.
*   **Logika:**
    1.  Deteksi log yang di-break.
    2.  Cari semua log yang terhubung (atas, bawah, samping) hingga ke ujung pohon.
    3.  **Daun Opsional:** Bisa diatur apakah daun ikut hancur atau tetap (biasanya tetap agar tidak lag).
    4.  **Sapling Drop:** Memastikan sapling tetap drop sesuai peluang vanilla.
*   **Syarat Alat:** Semua Axe atau konfigurasi tertentu.

---

### 3. Fitur Unik (Unique Selling Points)
Agar berbeda dari plugin lain di Modrinth, tambahkan fitur ini:

1.  **Preview Particles (Fitur Premium Gratis):**
    *   Saat pemain mengarahkan crosshair ke blok sambil Sneak, tampilkan **partikel kotak tipis** di sekitar blok yang *akan* hancur.
    *   *Manfaat:* Pemain tahu seberapa banyak blok yang akan hancur sebelum mereka memecahkannya (mencegah salah pencet).
2.  **Smart Durability Cost:**
    *   Banyak plugin veinminer bug dimana durability hanya berkurang 1 meski memecah 100 blok.
    *   Plugin kamu harus mengurangi durability **sesuai jumlah blok yang hancur** (1 blok = 1 durability). Jika durability tidak cukup, aksi dibatalkan.
3.  **Drop Consolidation (Anti-Lag):**
    *   Jangan drop 100 item ke lantai (bisa lag).
    *   Langsung masukkan ke inventory pemain. Jika penuh, drop dalam **1 tumpukan besar** atau berikan pesan "Inventory Penuh".
4.  **XP Orb Consolidation:**
    *   Gabungkan semua XP drop dari semua blok menjadi 1 orb XP besar di lokasi pemain.
5.  **WorldGuard Support:**
    *   Otomatis mati di region yang dilindungi (misal: area spawn) melalui flag custom `swift-harvest`.

---

### 4. Struktur Konfigurasi (`config.yml`)
Buat konfigurasi yang bersih dan mudah dipahami.

```yaml
settings:
  # Apakah harus sneak untuk aktivasi?
  require-sneak: true
  # Maksimum blok yang bisa dihancurkan sekali jalan (Safety Limit)
  max-blocks: 64
  # Delay cooldown setelah penggunaan (dalam tick)
  cooldown: 10
  # Tampilkan partikel preview?
  preview-particles: true

# Konfigurasi VeinMiner
veinminer:
  enabled: true
  # Tool yang diperbolehkan
  allowed-tools:
    - WOODEN_PICKAXE
    - STONE_PICKAXE
    - IRON_PICKAXE
    - GOLDEN_PICKAXE
    - DIAMOND_PICKAXE
    - NETHERITE_PICKAXE
  # Blok yang bisa di-vein (bisa custom)
  valid-blocks:
    - COAL_ORE
    - IRON_ORE
    - GOLD_ORE
    - DIAMOND_ORE
    - REDSTONE_ORE
    - LAPIS_ORE
    - EMERALD_ORE
    - NETHER_QUARTZ_ORE
    - ANCIENT_DEBRIS

# Konfigurasi Timber
timber:
  enabled: true
  allowed-tools:
    - WOODEN_AXE
    - STONE_AXE
    - IRON_AXE
    - GOLDEN_AXE
    - DIAMOND_AXE
    - NETHERITE_AXE
  # Apakah daun ikut hancur? (False disarankan untuk performa)
  break-leaves: false
  # Apakah sapling harus ada agar pohon bisa ditebang? (Opsional)
  require-sapling: false

# Ekonomi & Job (Soft Depend)
integration:
  # Apakah memberikan money dari JobsReborn per blok?
  jobs-reborn: true
  # Apakah memberikan exp McMMO?
  mcmmo: true
```

---

### 5. Aspek Teknis & Keamanan (Penting!)

1.  **Algoritma Iteratif (Bukan Rekursif):**
    *   **Jangan gunakan rekursif** untuk mencari blok terhubung. Jika pohon terlalu besar, server akan crash (`StackOverflowError`).
    *   **Gunakan Queue (BFS - Breadth First Search):** Ini lebih aman dan stabil untuk server.
2.  **Async Processing:**
    *   Lakukan pencarian blok (calculation) di thread async (di luar main thread) agar tidak menyebabkan *lag spike* saat pemain menambang.
    *   Lakukan penghancuran blok (block break) di main thread (karena API Minecraft mengharuskan demikian).
3.  **Event Cancellation:**
    *   Pastikan event `BlockBreakEvent` asli dibatalkan (`setCancelled(true)`) agar blok tidak drop 2 kali (sekali dari vanilla, sekali dari plugin).
4.  **NBT Data Preservation:**
    *   Jika ada blok dengan NBT (misal: Spawner atau Custom Items), pastikan tidak ikut hancur kecuali dikonfigurasi.

---

### 6. Roadmap Pengembangan

1.  **Fase 1 (Core Logic):**
    *   Buat event listener `BlockBreakEvent`.
    *   Implementasi algoritma pencarian blok terhubung (BFS).
    *   Fitur dasar VeinMiner & Timber.
2.  **Fase 2 (Safety & Cost):**
    *   Implementasi pengurangan durability alat yang benar.
    *   Implementasi batas maksimum blok (`max-blocks`).
    *   Pencegahan inventory penuh.
3.  **Fase 3 (Visual & Polish):**
    *   Tambahkan partikel preview.
    *   Tambahkan konfigurasi world blacklist (misal: tidak bisa veinminer di world_nether).
4.  **Fase 4 (Integration):**
    *   Hook ke JobsReborn, McMMO, Vault (jika ada fitur money).
5.  **Fase 5 (Release):**
    *   Dokumentasi, Upload Modrinth.

---

### 7. Strategi Publikasi di Modrinth

1.  **Halaman Proyek:**
    *   **Banner:** Buat gambar yang menunjukkan partikel preview (kotak-kotak di sekitar ore) agar terlihat canggih.
    *   **Deskripsi:** Tulis "Lag-Free Algorithm" dan "Smart Durability" di baris pertama. Ini adalah pain point admin server.
    *   **Tags:** `Utility`, `QoL`, `Survival`, `PaperMC`.
2.  **Dokumentasi:**
    *   Jelaskan cara mengaktifkan fitur (Sneak + Tool).
    *   Berikan contoh konfigurasi untuk server SMP biasa vs server Skyblock.
3.  **Video Pendek:**
    *   Rekam layar saat menebang pohon besar dalam 1 detik tanpa lag. Ini bukti performa terbaik.

### 8. Contoh Kode Logika (Java - BFS)
Ini contoh sederhana bagaimana mencari blok terhubung tanpa rekursif (Aman dari Crash):

```java
public List<Block> findConnectedBlocks(Block startBlock, Material targetMaterial, int maxBlocks) {
    List<Block> connected = new ArrayList<>();
    Queue<Block> queue = new LinkedList<>();
    Set<Block> visited = new HashSet<>();
    
    queue.add(startBlock);
    visited.add(startBlock);
    
    while (!queue.isEmpty() && connected.size() < maxBlocks) {
        Block current = queue.poll();
        
        if (current.getType() == targetMaterial) {
            connected.add(current);
        }
        
        // Cek 6 arah sekitar (atas, bawah, utara, selatan, timur, barat)
        for (BlockFace face : BlockFace.values()) {
            if (!face.isCartesian()) continue; // Skip diagonal
            
            Block neighbor = current.getRelative(face);
            if (!visited.contains(neighbor) && neighbor.getType() == targetMaterial) {
                visited.add(neighbor);
                queue.add(neighbor);
            }
        }
    }
    return connected;
}
```

### Kesimpulan
Konsep ini sangat solid. Kuncinya ada di **optimasi algoritma** (agar tidak lag) dan **kejelasan konfigurasi**. Jika kamu bisa membuat plugin ini lebih ringan dari kompetitor (seperti *OreExcavator*), plugin kamu akan cepat populer di Modrinth.
