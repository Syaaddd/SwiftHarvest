# SwiftHarvest — Feature Development Plan

Dokumen ini berisi daftar fitur & perbaikan yang akan dikembangkan untuk plugin SwiftHarvest. Disusun berdasarkan kategori dan prioritas, lengkap dengan penjelasan teknis implementasi.

**Versi saat ini:** 1.2.0  
**Target versi akhir:** 3.0.0

---

## 📋 Status Fitur Saat Ini

Sebelum masuk ke pengembangan baru, berikut fitur yang sudah ada:

| Fitur | Status | Catatan |
|-------|--------|---------|
| VeinMiner (BFS 6-arah) | ✅ Done | `BlockScanner.findConnectedBlocks` |
| Timber (log + leaves) | ✅ Done | `BlockScanner.findConnectedTimber` |
| Preview particles | ✅ Done | `PlayerInteractListener` |
| Durability (1 durability = 1 block) | ✅ Done | `DurabilityManager` |
| Cooldown system | ✅ Done | `CooldownManager` |
| Drop consolidation | ✅ Done | `DropManager.mergeDrops` |
| WorldGuard integration | ✅ Done | `WorldGuardHook` |
| World blacklist | ✅ Done | Config-based |
| Sound effect | ✅ Done | `ENTITY_EXPERIENCE_ORB_PICKUP` |

---

## 🎯 Kategori Pengembangan

### A. Core Feature Improvements
Perbaikan fitur inti yang sudah ada.

### B. New Gameplay Features
Fitur baru yang menambah cara main.

### C. Quality of Life
Fitur kecil yang bikin plugin lebih nyaman dipakai.

### D. Integrations
Dukungan ke plugin lain yang populer.

### E. Performance & Technical
Optimasi internal codebase.

### F. Configuration & UX
Kemudahan pengaturan untuk server owner.

---

## A. Core Feature Improvements

### A.1 Fortune & Silk Touch Support 🔴 HIGH PRIORITY

**Masalah sekarang:**
Di `BlockBreakListener.java:98-103`, drop didapat dari `b.getDrops(tool)`, tapi tidak ada handling khusus untuk enchantment. Artinya:
- Fortune di pickaxe tidak menambah drop diamond/ore
- Silk Touch tidak memberikan blok mentah (misal diamond ore → diamond, seharusnya ore block)
- Ini exploit besar — player bisa vein mine diamond dengan fortune tapi tidak dapat bonus, atau harus lepas enchant

**Implementasi:**
```java
// Di BlockBreakListener.handleVeinMiner()
for (Block b : connectedBlocks) {
    // Gunakan breakNaturally(tool) sebagai ganti getDrops
    // Ini otomatis honor Fortune & Silk Touch
    Collection<ItemStack> drops = b.getDrops(tool, player);
    
    // Bonus Fortune detection manual (kalau perlu custom multiplier)
    int fortuneLevel = tool.getEnchantmentLevel(Enchantment.FORTUNE);
    boolean silkTouch = tool.getEnchantmentLevel(Enchantment.SILK_TOUCH) > 0;
    
    // Override drops untuk ore tertentu jika perlu custom logic
    for (ItemStack drop : drops) {
        allDrops.add(drop.clone());
    }
    b.setType(Material.AIR);
}
```

**Konfigurasi baru:**
```yaml
veinminer:
  honor-fortune: true
  honor-silk-touch: true
  # Opsional: limit fortune multiplier biar tidak OP
  max-fortune-multiplier: 3.0
```

**Effort:** 1-2 hari  
**File affected:** `BlockBreakListener.java`, `config.yml`

---

### A.2 Diagonal Block Detection 🔴 HIGH PRIORITY

**Masalah sekarang:**
`BlockScanner.java:11-15` hanya scan 6 arah (N/S/E/W/Up/Down). Padahal ore vein di Minecraft sering diagonal. Akibatnya vein yang bentuknya diagonal hanya ter-mining sebagian — player kecewa.

**Implementasi:**
```java
// Tambah 6 diagonal directions
private static final BlockFace[] DIRECTIONS_6 = { ... }; // existing

private static final BlockFace[] DIRECTIONS_26 = {
    BlockFace.UP, BlockFace.DOWN,
    BlockFace.NORTH, BlockFace.SOUTH,
    BlockFace.EAST, BlockFace.WEST,
    // Diagonals
    BlockFace.NORTH_EAST, BlockFace.NORTH_WEST,
    BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST,
    BlockFace.UP_NORTH, BlockFace.UP_SOUTH,
    BlockFace.UP_EAST, BlockFace.UP_WEST,
    BlockFace.DOWN_NORTH, BlockFace.DOWN_SOUTH,
    BlockFace.DOWN_EAST, BlockFace.DOWN_WEST,
    BlockFace.NORTH_EAST.up(), BlockFace.NORTH_WEST.up(),
    BlockFace.SOUTH_EAST.up(), BlockFace.SOUTH_WEST.up(),
    BlockFace.NORTH_EAST.down(), BlockFace.NORTH_WEST.down(),
    BlockFace.SOUTH_EAST.down(), BlockFace.SOUTH_WEST.down()
};
```

**Konfigurasi:**
```yaml
veinminer:
  scan-mode: "cardinal" # cardinal (6-arah) / diagonal (26-arah) / auto
```

**Catatan:** Mode `auto` pakai cardinal default, tapi switch ke diagonal kalau vein terdeteksi < 50% dari expected size (berdasarkan ore density).

**Effort:** 1 hari  
**File affected:** `BlockScanner.java`, `config.yml`

---

### A.3 Animated Block Breaking 🔴 HIGH PRIORITY

**Masalah sekarang:**
`BlockBreakListener.java:98-103` langsung set semua block jadi `AIR` dalam satu tick. Kalau vein 64 block, player lihat semua hilang instant — tidak natural dan bikin "lag visual".

**Implementasi:**
```java
// Di BlockBreakListener, ganti instant break dengan scheduler
List<Block> connectedBlocks = BlockScanner.findConnectedBlocks(...);

// Sort blocks by distance from player (biar efek ripple)
connectedBlocks.sort(Comparator.comparingDouble(
    b -> b.getLocation().distanceSquared(player.getLocation())
));

// Break secara bertahap
int blocksPerTick = config.getBlocksPerTick(); // default 4
AtomicInteger index = new AtomicInteger(0);

BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
    int current = index.getAndAdd(blocksPerTick);
    int end = Math.min(current + blocksPerTick, connectedBlocks.size());
    
    for (int i = current; i < end; i++) {
        Block b = connectedBlocks.get(i);
        b.getWorld().playSound(b.getLocation(), Sound.BLOCK_STONE_BREAK, 0.5f, 1.2f);
        b.getWorld().spawnParticle(Particle.BLOCK_CRACK, 
            b.getLocation().add(0.5, 0.5, 0.5), 10, 
            b.getBlockData());
        b.setType(Material.AIR);
    }
    
    if (end >= connectedBlocks.size()) {
        this.cancel();
        // Drop items & durability baru diproses di sini
        processDrops(player, tool, allDrops);
    }
}, 0L, 1L); // setiap tick
```

**Konfigurasi:**
```yaml
settings:
  animation:
    enabled: true
    blocks-per-tick: 4 # jumlah blok dihancurin per tick
    style: "ripple" # ripple / wave / random / instant
```

**Effort:** 2-3 hari  
**File affected:** `BlockBreakListener.java`, `config.yml`, baru: `AnimationManager.java`

---

### A.4 Timber Tree Detection Algorithm 🔴 HIGH PRIORITY

**Masalah sekarang:**
`BlockScanner.findConnectedTimber()` pakai BFS sederhana yang gampang "bleed" — kalau ada 2 pohon deket banget (dalam radius 1 block), BFS bisa loncat ke pohon sebelahnya dan hancurin dua pohon sekaligus. Ini exploit besar.

**Implementasi baru:**
```java
public static List<Block> findConnectedTimber(Block startBlock, ...) {
    // Step 1: Scan UP dari start block untuk dapat full trunk
    List<Block> trunk = findTrunk(startBlock, logTypes, maxBlocks);
    
    // Step 2: Cari leaves di sekitar trunk (radius 5-7 block tergantung tree type)
    Set<Block> leaves = new HashSet<>();
    if (breakLeaves) {
        for (Block log : trunk) {
            Set<Block> nearbyLeaves = findNearbyLeaves(log, leafTypes, 6);
            leaves.addAll(nearbyLeaves);
        }
    }
    
    // Step 3: Combine trunk + leaves
    List<Block> result = new ArrayList<>(trunk);
    result.addAll(leaves);
    return result;
}

private static List<Block> findTrunk(Block start, Set<Material> logs, int max) {
    // BFS UP only, kemudian expand horizontal hanya untuk log yang adjacent
    // Tapi limit horizontal expansion biar tidak bleed ke pohon lain
    // Heuristic: kalau horizontal neighbor punya log di bawahnya (trunk lain), skip
}
```

**Heuristic anti-bleed:**
- Kalau 2 log horizontal adjacent, cek apakah keduanya punya log di bawahnya → skip (indikasi 2 pohon berbeda)
- Limit horizontal expansion ke max 3 block dari trunk utama

**Effort:** 2-3 hari  
**File affected:** `BlockScanner.java`

---

### A.5 Per-Tier Tool Configuration 🟡 MEDIUM PRIORITY

**Masalah sekarang:**
Semua pickaxe punya limit yang sama (64 block). Wood pickaxe sama dengan netherite — ini tidak immersive.

**Konfigurasi baru:**
```yaml
veinminer:
  tool-tiers:
    WOODEN_PICKAXE:
      max-blocks: 8
      durability-multiplier: 1.5
      cooldown-multiplier: 2.0
    STONE_PICKAXE:
      max-blocks: 16
      durability-multiplier: 1.2
    IRON_PICKAXE:
      max-blocks: 32
    DIAMOND_PICKAXE:
      max-blocks: 64
      durability-multiplier: 0.8
    NETHERITE_PICKAXE:
      max-blocks: 128
      durability-multiplier: 0.6
      cooldown-multiplier: 0.5
  use-tiers: true # false = flat max-blocks (backwards compatible)
```

**Implementasi:**
```java
// Di SwiftHarvestConfig
public int getMaxBlocksForTool(Material toolType) {
    if (!useTiers) return maxBlocks; // fallback ke flat
    return toolTierConfig.getOrDefault(toolType, new ToolTier()).getMaxBlocks();
}
```

**Effort:** 1 hari  
**File affected:** `SwiftHarvestConfig.java`, `BlockBreakListener.java`, `config.yml`

---

### A.6 Activation Modes 🟡 MEDIUM PRIORITY

**Masalah sekarang:**
Cuma ada 1 mode aktivasi: sneak + break. Ini tidak fleksibel — banyak player suka toggle (on/off) atau hold button.

**Mode yang ditambah:**
```yaml
settings:
  activation-mode: "sneak" # sneak / toggle / hold / right-click
```

**Implementasi:**

**Sneak mode** (default, existing):
- Sneak + break → activate

**Toggle mode:**
- `/sh toggle` → enable/disable SwiftHarvest per player
- Data disimpan di PlayerData (lihat C.1)
- Setelah toggle on, break biasa langsung vein mine

**Hold mode:**
- Tahan tombol tertentu (misal Q atau F) → vein mine aktif selama hold
- Pakai `PlayerSwapHandItemsEvent` atau keybind via packet (agak kompleks)
- Alternatif simple: tahan sneak > 1 detik → activate

**Right-click mode:**
- Right-click block dengan tool → activate (tidak perlu break)
- Pakai `PlayerInteractEvent` ACTION_RIGHT_CLICK_BLOCK

**Effort:** 2 hari  
**File affected:** `BlockBreakListener.java`, `PlayerInteractListener.java`, baru: `PlayerDataManager.java`, `config.yml`

---

## B. New Gameplay Features

### B.1 Auto-Replant (Timber) 🟡 MEDIUM PRIORITY

**Konsep:**
Setelah chop tree, plugin otomatis tanam 1 sapling di posisi asli log paling bawah. Server survival friendly.

**Implementasi:**
```java
// Di BlockBreakListener.handleTimber()
Block bottomLog = findBottomLog(connectedBlocks); // log dengan Y terkecil
Material saplingType = logToSapling(bottomLog.getType()); // OAK_LOG -> OAK_SAPLING

if (config.isAutoReplant()) {
    // Cek apakah player punya sapling di inventory
    ItemStack sapling = findSaplingInInventory(player, saplingType);
    if (sapling != null) {
        sapling.setAmount(sapling.getAmount() - 1);
        // Tanam setelah animation selesai (kalau animation on)
        bottomLog.setType(saplingType);
    } else {
        player.sendMessage(config.getMessage("no-sapling"));
    }
}

// Mapping log -> sapling
private Material logToSapling(Material log) {
    return switch (log) {
        case OAK_LOG -> Material.OAK_SAPLING;
        case BIRCH_LOG -> Material.BIRCH_SAPLING;
        case SPRUCE_LOG -> Material.SPRUCE_SAPLING;
        case JUNGLE_LOG -> Material.JUNGLE_SAPLING;
        case ACACIA_LOG -> Material.ACACIA_SAPLING;
        case DARK_OAK_LOG -> Material.DARK_OAK_SAPLING;
        case CHERRY_LOG -> Material.CHERRY_SAPLING;
        case PALE_OAK_LOG -> Material.PALE_OAK_SAPLING; // 1.21+
        default -> null;
    };
}
```

**Konfigurasi:**
```yaml
timber:
  auto-replant:
    enabled: true
    require-sapling-in-inv: true # false = free sapling
    consume-sapling: true
```

**Effort:** 1 hari  
**File affected:** `BlockBreakListener.java`, `SwiftHarvestConfig.java`, `config.yml`

---

### B.2 XP Orb Consolidation & Bonus 🟡 MEDIUM PRIORITY

**Masalah sekarang:**
README mengklaim "XP Orb Consolidation", tapi di code tidak ada implementasi. Tiap block yang di-vein bisa drop XP terpisah — 64 block = 64 orb XP = lag.

**Implementasi:**
```java
// Collect semua XP dari block yang di-break
int totalXP = 0;
for (Block b : connectedBlocks) {
    totalXP += getXPFromBlock(b.getType());
}

// Spawn 1 orb gabungan dengan total XP
ExperienceOrb orb = (ExperienceOrb) player.getWorld().spawn(
    player.getLocation(), 
    ExperienceOrb.class
);
orb.setExperience(totalXP);

// Bonus XP (optional, biar player termotivasi vein mine)
if (config.isXPBonusEnabled()) {
    int bonus = (int) (totalXP * config.getXpBonusMultiplier());
    orb.setExperience(totalXP + bonus);
}
```

**Konfigurasi:**
```yaml
settings:
  xp:
    consolidate: true
    bonus:
      enabled: false
      multiplier: 1.2 # 20% bonus XP
```

**Effort:** 0.5 hari  
**File affected:** `BlockBreakListener.java`, baru: `XPManager.java`, `config.yml`

---

### B.3 Player Statistics 🟡 MEDIUM PRIORITY

**Konsep:**
Track usage per player. Jadi bahan feedback dan engagement.

**Data yang di-track:**
- Total blocks broken (vein + timber)
- Blocks broken per session
- Vein activations count
- Timber activations count
- Favorite ore (paling sering di-vein)
- Total durability consumed
- Longest vein (biggest single activation)
- Total XP gained dari vein/timber

**Storage:** SQLite (ringan, file-based, no server dependency)

**Struktur database:**
```sql
CREATE TABLE player_stats (
    uuid TEXT PRIMARY KEY,
    total_blocks INTEGER DEFAULT 0,
    total_activations INTEGER DEFAULT 0,
    vein_blocks INTEGER DEFAULT 0,
    timber_blocks INTEGER DEFAULT 0,
    favorite_ore TEXT,
    longest_vein INTEGER DEFAULT 0,
    last_seen INTEGER
);

CREATE TABLE session_stats (
    uuid TEXT,
    session_start INTEGER,
    session_end INTEGER,
    blocks_broken INTEGER,
    activations INTEGER
);
```

**Command:**
```
/sh stats                -> lihat stats sendiri
/sh stats <player>       -> lihat stats player lain (permission)
/sh stats top 10         -> leaderboard
/sh stats reset <player> -> admin reset (permission)
```

**Display:** Kirim sebagai chat message yang di-format atau pakai GUI (chest inventory).

**Effort:** 3-4 hari  
**File affected:** baru: `database/DatabaseManager.java`, `database/StatsDAO.java`, `stats/PlayerStats.java`, `command/StatsCommand.java`

---

### B.4 Achievement System 🟢 LOW PRIORITY

**Konsep:**
Reward player dengan achievement/badge saat capai milestone.

**Achievement examples:**
```yaml
achievements:
  first-vein:
    name: "&aFirst Vein"
    description: "Vein mine for the first time"
    trigger: "vein_activation"
    requirement: 1
  diamond-hunter:
    name: "&bDiamond Hunter"
    description: "Vein mine 100 diamond ores"
    trigger: "diamond_vein"
    requirement: 100
  tree-master:
    name: "&6Tree Master"
    description: "Chop 50 trees"
    trigger: "timber_activation"
    requirement: 50
  speed-demon:
    name: "&cSpeed Demon"
    description: "Vein mine 50 blocks in one go"
    trigger: "single_vein_size"
    requirement: 50
  ancient-seeker:
    name: "&4Ancient Seeker"
    description: "Vein mine Ancient Debris"
    trigger: "ancient_debris_vein"
    requirement: 1
```

**Notification:**
```
🏆 Achievement Unlocked: Diamond Hunter!
   You've vein mined 100 diamond ores.
```

**Effort:** 2 hari  
**File affected:** baru: `achievement/AchievementManager.java`, `achievement/Achievement.java`, `config.yml`

---

### B.5 Combo System 🟢 LOW PRIORITY

**Konsep:**
Kalau player vein mine beberapa kali berturut-turut dalam waktu singkat, kasih bonus effect.

**Implementasi:**
```java
// Di PlayerDataManager
private Map<UUID, ComboData> combos = new ConcurrentHashMap<>();

class ComboData {
    int comboCount;
    long lastActivation;
}

// Di BlockBreakListener.handleVeinMiner()
long now = System.currentTimeMillis();
ComboData combo = combos.get(player.getUniqueId());
if (combo != null && now - combo.lastActivation < 5000) { // 5 detik window
    combo.comboCount++;
    applyComboBonus(player, combo.comboCount);
} else {
    combo = new ComboData(1, now);
}
combos.put(player.getUniqueId(), combo);

// Bonus per combo level:
// 2x combo: +10% drop
// 3x combo: +20% drop, speed particle
// 5x combo: +50% drop, flame particle
// 10x combo: +100% drop, rare drop bonus
```

**Konfigurasi:**
```yaml
settings:
  combo:
    enabled: true
    window-seconds: 5
    max-combo: 10
    bonuses:
      2: {drop-multiplier: 1.1, particle: "none"}
      3: {drop-multiplier: 1.2, particle: "speed"}
      5: {drop-multiplier: 1.5, particle: "flame"}
      10: {drop-multiplier: 2.0, particle: "totem"}
```

**Effort:** 1-2 hari  
**File affected:** baru: `combo/ComboManager.java`, `BlockBreakListener.java`, `config.yml`

---

### B.6 Rare Ore Celebration 🟢 LOW PRIORITY

**Konsep:**
Kalau player vein mine ore langka (ancient debris, diamond, emerald), broadcast ke server + efek khusus.

**Implementasi:**
```java
// Di BlockBreakListener.handleVeinMiner()
if (isRareOre(blockType) && connectedBlocks.size() >= config.getRareOreMinSize()) {
    // Broadcast ke server
    String message = config.getMessage("rare-ore-broadcast")
        .replace("%player%", player.getName())
        .replace("%ore%", formatOreName(blockType))
        .replace("%amount%", String.valueOf(connectedBlocks.size()));
    Bukkit.broadcast(message, "swiftharvest.notify.rare");
    
    // Effect khusus ke player
    player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
    player.spawnParticle(Particle.TOTEM, player.getLocation().add(0, 1, 0), 50, 0.5, 0.5, 0.5);
}
```

**Konfigurasi:**
```yaml
celebrations:
  rare-ores:
    enabled: true
    ores: [ANCIENT_DEBRIS, DIAMOND_ORE, EMERALD_ORE, DEEPSLATE_DIAMOND_ORE, DEEPSLATE_EMERALD_ORE]
    min-vein-size: 3
    broadcast-to-server: true
```

**Effort:** 0.5 hari  
**File affected:** `BlockBreakListener.java`, `config.yml`, `messages.yml`

---

## C. Quality of Life

### C.1 Player Data Persistence 🟡 MEDIUM PRIORITY

**Konsep:**
Simpan data per player (toggle state, stats, preferences) biar survive restart.

**Storage:** JSON file per player (di `plugins/SwiftHarvest/playerdata/<uuid>.json`) atau SQLite (sama dengan B.3).

**Data yang disimpan:**
```json
{
  "uuid": "...",
  "name": "Syaaddd",
  "veinminer-enabled": true,
  "timber-enabled": true,
  "activation-mode": "sneak",
  "stats": {...},
  "achievements": [...],
  "last-seen": 1730000000
}
```

**Effort:** 1-2 hari  
**File affected:** baru: `data/PlayerDataManager.java`, `data/PlayerData.java`

---

### C.2 Toggle Command 🟡 MEDIUM PRIORITY

**Command:**
```
/sh toggle              -> toggle semua (vein + timber)
/sh toggle veinminer    -> toggle veinminer aja
/sh toggle timber       -> toggle timber aja
/sh toggle particles    -> toggle preview particles
```

**Implementasi:**
Cek `PlayerData.isEnabled()` di `BlockBreakListener` sebelum process.

**Effort:** 0.5 hari (setelah C.1)  
**File affected:** `SwiftHarvestCommand.java`, `BlockBreakListener.java`

---

### C.3 Tool Info / Hover Display 🟢 LOW PRIORITY

**Konsep:**
Saat player hold tool dan sneak, tampilkan info di actionbar:
```
⛏ Iron Pickaxe | VeinMiner: ON | Max: 32 | Cooldown: Ready
```

**Implementasi:**
```java
// PlayerInteractListener, saat sneak + hold tool
@EventHandler
public void onSneakToggle(PlayerToggleSneakEvent event) {
    if (event.isSneaking()) {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (isSwiftHarvestTool(tool)) {
            String info = buildToolInfo(player, tool);
            player.sendActionBar(info);
        }
    }
}
```

**Effort:** 0.5 hari  
**File affected:** `PlayerInteractListener.java`

---

### C.4 Better Messages / Placeholders 🟢 LOW PRIORITY

**Konsep:**
Sekarang messages sudah ada tapi minim placeholder. Tambah:

```
%player%           -> nama player
%amount%           -> jumlah block di-vein
%ore%              -> tipe ore
%tool%             -> nama tool
%durability%       -> sisa durability
%durability_max%   -> max durability
%cooldown%         -> sisa cooldown (detik)
%blocks_per_sec%   -> speed (animated breaking)
%combo%            -> combo count
%world%            -> nama world
```

**messages.yml example:**
```yaml
veinminer-started: "&a⛏ Vein mined &e%amount%&a %ore% blocks in %time%ms!"
timber-started: "&a🌲 Chopped &e%amount%&a blocks of wood!"
cooldown: "&c⏱ Wait &e%seconds%&c seconds before using again."
low-durability: "&c⚠ Your &e%tool%&c is about to break!"
full-inventory: "&6⚠ Inventory full! Some items dropped on the ground."
rare-ore-broadcast: "&6✨ &e%player%&6 just vein mined &e%amount% %ore%&6!"
```

**Effort:** 1 hari  
**File affected:** `MessageUtils.java`, `messages.yml`, semua listener

---

### C.5 Anti-Exploit System 🟡 MEDIUM PRIORITY

**Konsep:**
Cegah penyalahgunaan:
- Ore density check — kalau vein 64 block tapi density < 70% (banyak stone), mungkin exploit
- Crouch-spam protection — jangan sampai auto-clicker vein mine terus
- Region deny stacking — kalau WorldGuard deny, jangan coba scan vein

**Implementasi:**
```java
// Di BlockBreakListener.handleVeinMiner()
// Density check
List<Block> connected = BlockScanner.findConnectedBlocks(...);
int totalScanned = BlockScanner.getTotalScanned();
double density = (double) connected.size() / totalScanned;
if (density < config.getMinDensity()) {
    // Not a real vein, cancel
    return false;
}

// Anti-spam: track activations per minute
int recentActivations = getRecentActivations(player, 60_000); // last minute
if (recentActivations > config.getMaxActivationsPerMinute()) {
    player.sendMessage(config.getMessage("anti-spam"));
    return false;
}
```

**Konfigurasi:**
```yaml
anti-exploit:
  enabled: true
  min-vein-density: 0.7 # minimum 70% density
  max-activations-per-minute: 10
  log-suspicious: true
```

**Effort:** 1-2 hari  
**File affected:** baru: `antispam/AntiExploitManager.java`, `BlockScanner.java` (track total scanned), `BlockBreakListener.java`, `config.yml`

---

## D. Integrations

### D.1 PlaceholderAPI Integration 🟡 MEDIUM PRIORITY

**Placeholders yang ditambah:**
```
%swiftharvest_veinminer_enabled%    -> true/false
%swiftharvest_timber_enabled%       -> true/false
%swiftharvest_blocks_mined%         -> total blocks mined
%swiftharvest_vein_activations%     -> total vein activations
%swiftharvest_timber_activations%   -> total timber activations
%swiftharvest_favorite_ore%         -> ore paling sering di-vein
%swiftharvest_cooldown%             -> sisa cooldown (detik)
%swiftharvest_combo%                -> combo count saat ini
%swiftharvest_rank%                 -> rank di leaderboard
```

**Implementasi:**
```java
public class SwiftHarvestPlaceholder extends PlaceholderExpansion {
    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        return switch (identifier) {
            case "blocks_mined" -> 
                String.valueOf(statsDao.getTotalBlocks(player.getUniqueId()));
            case "cooldown" -> 
                String.valueOf(cooldownManager.getRemainingCooldown(player));
            // ...
        };
    }
    
    @Override
    public String getIdentifier() { return "swiftharvest"; }
    @Override
    public String getAuthor() { return "Syaaddd"; }
    @Override
    public String getVersion() { return "3.0.0"; }
    @Override
    public boolean persist() { return true; }
}
```

**plugin.yml:**
```yaml
softdepend:
  - WorldGuard
  - PlaceholderAPI
```

**Effort:** 1 hari  
**File affected:** baru: `integration/PlaceholderAPIHook.java`, `plugin.yml`

---

### D.2 McMMO Integration 🟢 LOW PRIORITY

**Konsep:**
Kasih McMMO XP saat vein mine/timber, sesuai dengan tipe block.

**Implementasi:**
```java
// Di BlockBreakListener.handleVeinMiner()
if (mcmmoEnabled) {
    for (Block b : connectedBlocks) {
        McMMOAPI.getInstance().getXpGainHandler().addXp(
            player, 
            PrimarySkillType.MINING, // atau UNARMED, dll
            getXpForBlock(b.getType())
        );
    }
}
```

**Konfigurasi:**
```yaml
integrations:
  mcmmo:
    enabled: true
    xp-multiplier: 1.0 # 1.0 = sama kayak mine biasa
    apply-to-veinminer: true
    apply-to-timber: true # untuk WOODCUTING skill
```

**Effort:** 0.5-1 hari  
**File affected:** baru: `integration/McMMOHook.java`, `BlockBreakListener.java`, `config.yml`

---

### D.3 GriefPrevention Integration 🟡 MEDIUM PRIORITY

**Konsep:**
Respect GriefPrevention claims. Player tidak bisa vein mine di claim orang lain.

**Implementasi:**
```java
public class GriefPreventionHook {
    public static boolean canBreak(Player player, Block block) {
        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(block.getLocation(), true, null);
        if (claim == null) return true; // no claim, allowed
        String reason = claim.allowBreak(player, block.getType(), block.getLocation(), null);
        return reason == null; // null = allowed
    }
}
```

**Effort:** 0.5 hari  
**File affected:** baru: `integration/GriefPreventionHook.java`, `BlockBreakListener.java`, `plugin.yml`

---

### D.4 CoreProtect Logging 🟢 LOW PRIORITY

**Konsep:**
Log semua block yang di-vein ke CoreProtect, biar rollback bisa track.

**Implementasi:**
```java
// Di BlockBreakListener
if (coreProtectEnabled) {
    for (Block b : connectedBlocks) {
        CoreProtectAPI api = getCoreProtectAPI();
        api.logRemoval(player.getName(), b.getLocation(), b.getType(), b.getBlockData());
    }
}
```

**Effort:** 0.5 hari  
**File affected:** baru: `integration/CoreProtectHook.java`, `BlockBreakListener.java`

---

### D.5 Vault Integration (Optional) 🟢 LOW PRIORITY

**Catatan:** Untuk plugin gratis, Vault tidak urgent. Tapi kalau mau kasih reward random atau cost (walau gratis), Vault berguna.

**Possible uses:**
- Reward random cash saat vein mine rare ore
- Cost per activation (walupun gratis, server owner bisa set cost via config)

**Implementasi:**
```java
// Contoh: reward random cash
if (vaultEnabled && config.isCashRewardEnabled()) {
    double reward = calculateReward(blockType, connectedBlocks.size());
    economy.depositPlayer(player, reward);
    player.sendMessage("💰 +" + formatMoney(reward));
}
```

**Effort:** 1 hari (kalau diimplementasikan)

---

## E. Performance & Technical

### E.1 Async Block Breaking 🟡 MEDIUM PRIORITY

**Masalah sekarang:**
Block breaking terjadi di main thread. Kalau vein 64 block, semua proses di main thread → lag spike.

**Implementasi:**
```java
// Step 1: BFS scan di async thread (sudah ada, tapi perlu diperkuat)
Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
    List<Block> connected = BlockScanner.findConnectedBlocks(...);
    
    // Step 2: Breaking di main thread, tapi bertahap
    Bukkit.getScheduler().runTask(plugin, () -> {
        // Process max N blocks per tick
        int batchSize = config.getBatchSize();
        breakInBatches(connected, batchSize, player, tool);
    });
});

private void breakInBatches(List<Block> blocks, int batchSize, Player p, ItemStack tool) {
    AtomicInteger index = new AtomicInteger(0);
    BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
        int start = index.getAndAdd(batchSize);
        int end = Math.min(start + batchSize, blocks.size());
        
        for (int i = start; i < end; i++) {
            Block b = blocks.get(i);
            b.setType(Material.AIR);
        }
        
        if (end >= blocks.size()) {
            this.cancel();
            finalizeBreak(p, tool, blocks);
        }
    }, 0L, 1L);
}
```

**Effort:** 1-2 hari  
**File affected:** `BlockBreakListener.java`, baru: `AsyncBreakingManager.java`

---

### E.2 BlockScanner Optimization 🟡 MEDIUM PRIORITY

**Optimasi yang dilakukan:**

1. **Ganti HashSet<Block> dengan HashSet<Long>** (encode XYZ ke long):
```java
private static long encode(Block b) {
    return ((long) b.getX() << 40) | ((long) b.getZ() << 16) | b.getY();
}

// Memory: Block object = ~40 bytes, Long = 24 bytes
// Untuk 1000 block: 40KB -> 24KB (40% lebih kecil)
```

2. **Early exit kalau block count > max:**
```java
if (connected.size() >= maxBlocks) return; // stop scanning
```

3. **Pre-allocate collections dengan initial capacity:**
```java
List<Block> connected = new ArrayList<>(maxBlocks);
Queue<Block> queue = new ArrayDeque<>(maxBlocks);
Set<Long> visited = new HashSet<>(maxBlocks * 2);
```

4. **Cache Block data untuk repeated access:**
```java
// Daripada current.getType() berkali-kali, cache sekali
Material currentType = current.getType();
```

**Effort:** 1 hari  
**File affected:** `BlockScanner.java`

---

### E.3 Chunk-Based Scanning 🟢 LOW PRIORITY

**Konsep:**
Scan vein per chunk. Kalau vein cross chunk, lanjut di chunk lain. Ini kurangi memory dan cocok untuk vein super besar.

**Implementasi:**
```java
public static List<Block> findConnectedBlocksChunked(Block start, Material type, int max) {
    // Group visited blocks by chunk
    Map<ChunkCoord, Set<Long>> chunkVisited = new HashMap<>();
    // Scan chunk by chunk, unload data dari chunk yang sudah selesai
    // ...
}
```

**Effort:** 2 hari  
**Catatan:** Implementasi kompleks, priority rendah karena max-blocks limit biasanya cukup.

---

### E.4 Debug & Diagnostics 🟡 MEDIUM PRIORITY

**Konsep:**
Tool untuk server owner troubleshoot masalah.

**Commands:**
```
/sh debug                  -> generate debug report (zip file)
/sh debug performance      -> show performance stats
/sh debug scan <radius>    -> scan area, show vein candidates
/sh debug test <block>     -> test vein mine pada block tertentu
```

**Debug report berisi:**
- Server version
- Plugin version
- Java version
- TPS last 5 minutes
- Memory usage
- List of loaded hooks
- Config file dump
- Latest log
- Performance metrics

**Effort:** 2 hari  
**File affected:** baru: `debug/DebugManager.java`, `debug/PerformanceMonitor.java`, `SwiftHarvestCommand.java`

---

## F. Configuration & UX

### F.1 GUI Configuration Editor 🟡 MEDIUM PRIORITY

**Konsep:**
Server owner bisa edit config via in-game chest GUI, tidak perlu touch file config.yml.

**Command:**
```
/sh gui
```

**GUI layout (chest inventory):**
```
┌─────────────────────────────────────────────┐
│  ⚙ SwiftHarvest Configuration               │
├─────────────────────────────────────────────┤
│ [VeinMiner]  [Timber]  [Visuals] [Stats]   │
│ [Worlds]     [Economy] [Advanced] [About]   │
│                                             │
│              [Save]  [Reset]  [Cancel]      │
└─────────────────────────────────────────────┘
```

**Click VeinMiner:**
```
┌─────────────────────────────────────────────┐
│  ⛏ VeinMiner Settings                       │
├─────────────────────────────────────────────┤
│ [Enabled: ✓]      [Max Blocks: 64]         │
│ [Scan Mode: 6]    [Honor Fortune: ✓]       │
│ [Honor Silk: ✓]   [Tools: 6 items]         │
│ [Blocks: 18 items]                          │
│                                             │
│              [Save]  [Back]                 │
└─────────────────────────────────────────────┘
```

**Implementasi:**
```java
public class ConfigGUI {
    public void openMain(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "SwiftHarvest Config");
        gui.setItem(10, createItem(Material.DIAMOND_PICKAXE, "VeinMiner", "Configure vein miner"));
        gui.setItem(11, createItem(Material.DIAMOND_AXE, "Timber", "Configure timber"));
        gui.setItem(12, createItem(Material.BLAZE_POWDER, "Visuals", "Particles & animations"));
        // ... dst
        
        player.openInventory(gui);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Handle clicks, update config
    }
}
```

**Effort:** 3-4 hari  
**File affected:** baru: `gui/ConfigGUI.java`, `gui/GUIItem.java`, `SwiftHarvestCommand.java`

---

### F.2 Setup Wizard 🟢 LOW PRIORITY

**Konsep:**
Saat plugin pertama kali di-install, jalankan wizard yang guided server owner setup basic config.

**Flow:**
```
[1/5] Welcome to SwiftHarvest! Let's set things up.
[2/5] Enable VeinMiner? (yes/no)
[3/5] Enable Timber? (yes/no)
[4/5] Require sneak to activate? (yes/no)
[5/5] Setup complete! Use /sh gui for advanced config.
```

**Trigger:** First-time join player dengan permission `swiftharvest.admin`.

**Effort:** 1 hari  
**File affected:** baru: `setup/SetupWizard.java`, `SwiftHarvest.java`

---

### F.3 Config Migration System 🟢 LOW PRIORITY

**Konsep:**
Saat plugin update, config lama otomatis di-migrate ke format baru tanpa hilang setting yang sudah di-custom.

**Implementasi:**
```java
public class ConfigMigrator {
    public void migrate(int fromVersion, int toVersion) {
        for (int v = fromVersion; v < toVersion; v++) {
            switch (v) {
                case 1 -> migrateV1toV2();
                case 2 -> migrateV2toV3();
                // ...
            }
        }
    }
    
    private void migrateV1toV2() {
        // Rename old keys, add new ones with default values
        // Preserve user customization
    }
}
```

**Effort:** 1 hari  
**File affected:** baru: `config/ConfigMigrator.java`

---

### F.4 Multi-Language Support (i18n) 🟡 MEDIUM PRIORITY

**Konsep:**
Plugin bisa di-switch bahasanya. Default English, tapi tersedia bahasa lain.

**Struktur:**
```
plugins/SwiftHarvest/
  lang/
    en.yml (default)
    id.yml (Indonesian)
    es.yml (Spanish)
    de.yml (German)
    zh.yml (Chinese)
    ja.yml (Japanese)
    ...
```

**Konfigurasi:**
```yaml
settings:
  language: "en" # en, id, es, de, zh, ja
```

**Implementasi:**
```java
public class LanguageManager {
    private Map<String, String> messages;
    
    public void loadLanguage(String lang) {
        File file = new File(plugin.getDataFolder(), "lang/" + lang + ".yml");
        if (!file.exists()) {
            plugin.saveResource("lang/" + lang + ".yml", false);
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        messages = yaml.getKeys(false).stream()
            .collect(Collectors.toMap(k -> k, yaml::getString));
    }
    
    public String get(String key) {
        return messages.getOrDefault(key, key);
    }
}
```

**Effort:** 2 hari (framework + English + Indonesian, bahasa lain via kontribusi komunitas)  
**File affected:** baru: `lang/LanguageManager.java`, `MessageUtils.java` (refactor), `lang/*.yml`

---

## 📊 Prioritas & Timeline

### Priority Legend
- 🔴 **HIGH** — Fitur wajib, langsung impact ke user experience
- 🟡 **MEDIUM** — Fitur penting, bikin plugin lebih competitive
- 🟢 **LOW** — Nice-to-have, polish

### Recommended Urutan Implementasi

#### Release 1.3.0 (Week 1-2) — "Performance Update"
- A.1 Fortune & Silk Touch 🔴
- A.2 Diagonal Detection 🔴
- E.2 BlockScanner Optimization 🟡
- A.4 Timber Tree Detection 🔴

**Alasan:** Fix masalah exploit & performanya dulu. Ini fondasi.

#### Release 1.4.0 (Week 3-4) — "Quality of Life Update"
- B.1 Auto-Replant 🟡
- B.2 XP Orb Consolidation 🟡
- B.6 Rare Ore Celebration 🟢
- C.5 Anti-Exploit System 🟡

**Alasan:** Features yang bikin gameplay lebih baik.

#### Release 1.5.0 (Week 5-6) — "Visual Update"
- A.3 Animated Block Breaking 🔴
- C.2 Toggle Command 🟡
- C.4 Better Messages 🟢
- C.3 Tool Info Display 🟢

**Alasan:** Visual & UX improvement.

#### Release 1.6.0 (Week 7-8) — "Stats & Persistence Update"
- B.3 Player Statistics 🟡
- C.1 Player Data Persistence 🟡
- E.4 Debug & Diagnostics 🟡

**Alasan:** Data & tracking.

#### Release 1.7.0 (Week 9-10) — "Integration Update"
- D.1 PlaceholderAPI 🟡
- D.2 McMMO 🟢
- D.3 GriefPrevention 🟡
- D.4 CoreProtect 🟢
- D.5 Vault (optional) 🟢

**Alasan:** Kompatibilitas dengan plugin lain.

#### Release 2.0.0 (Week 11-14) — "Configuration Update"
- F.1 GUI Config 🟡
- F.2 Setup Wizard 🟢
- F.3 Config Migration 🟢
- F.4 Multi-Language 🟡
- A.5 Per-Tier Tool 🟡
- A.6 Activation Modes 🟡

**Alasan:** Big UX overhaul, jadi major version.

#### Release 2.5.0 (Week 15-16) — "Engagement Update"
- B.4 Achievement System 🟢
- B.5 Combo System 🟢
- E.1 Async Block Breaking 🟡

**Alasan:** Polish akhir.

#### Release 3.0.0 (Week 17+) — "Performance 2.0"
- E.3 Chunk-Based Scanning 🟢
- Public API (kalau butuh)
- Whatever else comes up

---

## 🎨 Contoh Hasil Akhir (v3.0.0)

Server owner install SwiftHarvest dan dapat:
- ⛏ VeinMiner dengan Fortune & Silk Touch support
- 🌲 Timber dengan auto-replant
- 🎆 Animated breaking dengan particle themes
- 📊 Player stats & leaderboard
- 🏆 Achievement system
- 🔌 Integrasi dengan 5+ plugin populer
- 🌐 GUI config, multi-language
- 🛡️ Anti-exploit protection
- 🚀 Super cepat (optimized BFS, async breaking)

---

## 📝 Notes

- Semua effort di-estimate untuk 1 developer
- Priority bisa geser tergantung feedback user
- Tiap release harus: (1) test di Paper 1.21+, (2) backwards compatible, (3) document di changelog
- Bug fix selalu prioritas di atas fitur baru

---

**Document Version:** 1.0  
**Last Updated:** September 2026  
**Status:** Ready to execute  
**Next step:** Mulai dari Release 1.3.0 (A.1, A.2, A.4, E.2)
