# ⚡ SwiftHarvest

![Minecraft](https://img.shields.io/badge/Minecraft-1.21+-brightgreen.svg)
![PaperMC](https://img.shields.io/badge/PaperMC-1.21.11+-brightgreen.svg)
![Java](https://img.shields.io/badge/Java-21-orange.svg)
![Version](https://img.shields.io/badge/version-1.3.0-blue.svg)
![License](https://img.shields.io/badge/License-MIT-blue.svg)

> **Mine Faster, Lag Less.** — The Ultimate VeinMiner & Timber Solution for PaperMC.

A modern, lightweight Minecraft server plugin that brings **VeinMiner** and **Timber** functionality with performance-first design, rich configurability, and smooth visual feedback. 100% free & open source.

---

## 📑 Daftar Isi

1. [Features](#-features)
2. [Installation](#-installation)
3. [Usage](#-usage)
4. [Configuration](#-configuration)
5. [Commands](#-commands)
6. [Permissions](#-permissions)
7. [Plugin Integrations](#-plugin-integrations)
8. [Build from Source](#-build-from-source)
9. [Changelog](#-changelog)
10. [License & Support](#--license--support)

---

## ✨ Features

### ⛏️ Core

| Feature | Description |
|---------|-------------|
| **VeinMiner** | Mine entire ore veins with one swing (Pickaxe + Activation + Break) |
| **Timber** | Chop entire trees instantly (Axe + Activation + Break) |
| **Smart Durability** | Tool durability decreases proportionally to blocks broken, with **per-tier multipliers** |
| **Drop Consolidation** | Items auto-merged and go to inventory first — no lag from 100+ entity drops |
| **Fortune & Silk Touch** | Fully respects vanilla enchantment mechanics |
| **Fortune Cap** | Configurable `max-fortune-multiplier` to prevent OP drops (default: 3.0×) |
| **Cooldown System** | Per-usage cooldown with **per-tier cooldown multiplier** |
| **Low Durability Guard** | Blocks activation if tool doesn't have enough durability — prevents accidental tool breakage |

### 🎨 Visual & Feedback

| Feature | Description |
|---------|-------------|
| **Preview Particles** | Green dust particles show which blocks *will* be mined before you break |
| **Animated Block Breaking** | Blocks break in visually satisfying patterns (configurable style) |
| **Animation Styles** | **Ripple** (from player outward), **Wave** (bottom-up), **Random** (shuffled) |
| **Per-Block SFX** | Break sound + particle effect played for each block during animation |
| **Activation Sound** | Satisfying XP-orb sound on successful VeinMiner/Timber trigger |
| **Block Highlight** | Particle highlight on the targeted block |

### 🧭 Advanced

| Feature | Description |
|---------|-------------|
| **Diagonal Detection** | Scan veins in **10 directions** (6 cardinal + 4 horizontal diagonals) |
| **Strict Tree Detection** | Trunk-based **anti-bleed algorithm** — prevents chopping neighboring trees by accident |
| **Configurable Radius** | `max-horizontal-radius` controls how far strict timber spreads from trunk (default: 2) |
| **Per-Tier Tool Config** | Different limits per material tier for **both VeinMiner AND Timber** |
| **Tier Multipliers** | Each tier: `max-blocks`, `durability-multiplier`, `cooldown-multiplier` |
| **3 Activation Modes** | **Sneak**, **Toggle** (`/sh toggle`), or **Hold** (hold sneak → threshold → active) |
| **Hold Threshold** | Configurable `hold-ticks` for hold mode (default: 10 ticks) |
| **World Blacklist** | Disable features per-world (e.g., Nether, End) |
| **Sapling Requirement** | Optional `require-sapling` for timber validation |
| **Leaf Breaking** | Toggle whether timber also breaks leaves (default: `true`) |
| **Full Messages Customization** | All player-facing strings editable in `messages.yml` with color codes |

### 📊 Supported Blocks

#### VeinMiner Ores (18 types)

```
Coal, Iron, Gold, Diamond, Redstone, Lapis, Emerald,
Nether Quartz, Ancient Debris, Copper,
Deepslate variants of all above + Glowstone
```

#### Timber Logs (11 types)

```
Oak, Spruce, Birch, Jungle, Acacia, Dark Oak,
Mangrove, Cherry, Pale Oak,
Crimson Stem, Warped Stem
```

#### Timber Leaves (9 types)

```
Oak, Spruce, Birch, Jungle, Acacia, Dark Oak,
Mangrove, Cherry, Pale Oak
```

---

## 📦 Installation

> **Requirements**
> - **PaperMC** 1.21.11+ (or Purpur fork)
> - **Java** 21

### Steps

1. Download the latest JAR from [Releases](https://github.com/Syaaddd/SwiftHarvest/releases)
2. Place `SwiftHarvest-<version>.jar` into your server's `plugins/` folder
3. **Start / restart** the server
4. Edit `plugins/SwiftHarvest/config.yml` to your liking
5. Edit `plugins/SwiftHarvest/messages.yml` for custom messages (optional)
6. Use `/swifthavert reload` to apply config changes (no restart needed for config edits)

> ⚠️ **Note**: A full server restart is required after **first install** or **JAR upgrade**. Only use `/swifthavert reload` for config/message changes.

---

## 🎮 Usage

### VeinMiner

1. Hold a **Pickaxe** (any allowed tier)
2. **Activate** based on your mode (see below)
3. **Break** an ore block
4. All connected ores of the same type instantly mine! 💎

### Timber

1. Hold an **Axe** (any allowed tier)
2. **Activate** based on your mode
3. **Break** a log block
4. The entire tree chops down! 🪓

### Activation Modes

| Mode | How to Use | Config Value |
|------|-----------|--------------|
| **Sneak** (default) | Hold **Shift** + break block | `sneak` |
| **Toggle** | Run `/swifthavert toggle` → break normally | `toggle` |
| **Hold** | Hold **Shift** for N ticks → activates until released | `hold` |

> **Tip**: In **Hold mode**, a subtle click sound plays when the activation threshold is reached, so you know it's active.

---

## ⚙️ Configuration

### File Structure

```
plugins/SwiftHarvest/
├── config.yml      # Main configuration
└── messages.yml    # Customizable messages with color codes
```

### Full `config.yml` Reference

```yaml
# ── Global Settings ──
settings:
  require-sneak: true          # Require sneak to activate? (sneak mode only)
  max-blocks: 64               # Fallback max blocks when per-tier is disabled
  cooldown: 10                 # Cooldown in ticks (20 ticks = 1 second)
  preview-particles: true      # Show green preview particles on target?
  activation-mode: "sneak"     # sneak | toggle | hold
  hold-ticks: 10               # Ticks required to activate hold mode

# ── VeinMiner ──
veinminer:
  enabled: true
  allowed-tools:
    - WOODEN_PICKAXE
    - STONE_PICKAXE
    - IRON_PICKAXE
    - GOLDEN_PICKAXE
    - DIAMOND_PICKAXE
    - NETHERITE_PICKAXE
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
    - COPPER_ORE
    - DEEPSLATE_COAL_ORE
    - DEEPSLATE_IRON_ORE
    - DEEPSLATE_GOLD_ORE
    - DEEPSLATE_DIAMOND_ORE
    - DEEPSLATE_REDSTONE_ORE
    - DEEPSLATE_LAPIS_ORE
    - DEEPSLATE_EMERALD_ORE
    - DEEPSLATE_COPPER_ORE
    - GLOWSTONE
  scan-mode: "cardinal"        # cardinal (6-dir) | diagonal (10-dir)
  honor-fortune: true          # Respect Fortune enchantment
  honor-silk-touch: true       # Respect Silk Touch enchantment
  max-fortune-multiplier: 3.0  # Cap fortune output (0 = unlimited)
  use-tiers: false             # Enable per-tier overrides
  tool-tiers:                  # Per-tier: max-blocks, durability-mult, cooldown-mult
    WOODEN_PICKAXE:
      max-blocks: 8
      durability-multiplier: 1.5
      cooldown-multiplier: 2.0
    STONE_PICKAXE:
      max-blocks: 16
      durability-multiplier: 1.2
      cooldown-multiplier: 1.5
    IRON_PICKAXE:
      max-blocks: 32
      durability-multiplier: 1.0
      cooldown-multiplier: 1.0
    GOLDEN_PICKAXE:
      max-blocks: 16
      durability-multiplier: 2.0
      cooldown-multiplier: 1.5
    DIAMOND_PICKAXE:
      max-blocks: 64
      durability-multiplier: 0.8
      cooldown-multiplier: 0.8
    NETHERITE_PICKAXE:
      max-blocks: 128
      durability-multiplier: 0.6
      cooldown-multiplier: 0.5

# ── Timber ──
timber:
  enabled: true
  allowed-tools:
    - WOODEN_AXE
    - STONE_AXE
    - IRON_AXE
    - GOLDEN_AXE
    - DIAMOND_AXE
    - NETHERITE_AXE
  break-leaves: true           # Also break leaves when chopping
  require-sapling: false       # Optional: require sapling nearby
  detection: "strict"          # strict (anti-bleed) | loose (original BFS)
  max-horizontal-radius: 2     # How far from trunk to consider "same tree"
  use-tiers: false             # Enable per-tier overrides
  tool-tiers:                  # Same structure as veinminer tiers
    WOODEN_AXE:
      max-blocks: 16
      durability-multiplier: 1.5
      cooldown-multiplier: 2.0
    STONE_AXE:
      max-blocks: 32
      durability-multiplier: 1.2
      cooldown-multiplier: 1.5
    IRON_AXE:
      max-blocks: 64
      durability-multiplier: 1.0
      cooldown-multiplier: 1.0
    GOLDEN_AXE:
      max-blocks: 32
      durability-multiplier: 2.0
      cooldown-multiplier: 1.5
    DIAMOND_AXE:
      max-blocks: 128
      durability-multiplier: 0.8
      cooldown-multiplier: 0.8
    NETHERITE_AXE:
      max-blocks: 256
      durability-multiplier: 0.6
      cooldown-multiplier: 0.5

# ── Animation ──
animation:
  enabled: true                # Enable animated block breaking
  blocks-per-tick: 4           # Higher = faster but more intensive
  style: "ripple"              # ripple | wave | random

# ── World Blacklist ──
worlds:
  blacklist:
    - world_nether
    - world_the_end
```

### `messages.yml` Reference

```yaml
prefix: '&7[&bSwiftHarvest&7] '
messages:
  cooldown: '&cWait %seconds% seconds before using again!'
  full-inventory: '&cInventory full! Items will be dropped.'
  no-permission: '&cYou do not have permission to use this.'
  disabled-world: '&cThis feature is disabled in this world.'
  veinminer-started: '&aVeinMiner activated! Mining %amount% blocks.'
  timber-started: '&aTimber activated! Chopping %amount% blocks.'
  low-durability: '&cNot enough durability on tool!'
  config-reloaded: '&aConfiguration reloaded successfully.'
  toggle-on: '&aSwiftHarvest &2enabled&a!'
  toggle-off: '&cSwiftHarvest &4disabled&c.'
```

All messages support **Bukkit color codes** (`&a`, `&c`, `&b`, etc.) and **placeholders** (`%seconds%`, `%amount%`).

---

## 📋 Commands

| Command | Aliases | Description | Permission |
|---------|---------|-------------|------------|
| `/swifthavert reload` | `/sh reload` | Reload config & messages, clear cooldowns | `swifthavert.admin` |
| `/swifthavert toggle` | `/sh toggle` | Toggle SwiftHarvest on/off (toggle mode only) | `swifthavert.use` |
| `/swifthavert` | `/sh` | Show help menu | — |

---

## 🔐 Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `swifthavert.*` | All permissions (parent) | — |
| `swifthavert.admin` | Admin commands (reload) | `op` |
| `swifthavert.use` | Use SwiftHarvest features | `true` |
| `swifthavert.veinminer` | Use VeinMiner specifically | `true` |
| `swifthavert.timber` | Use Timber specifically | `true` |
| `swifthavert.bypass` | Bypass world blacklist | `op` |

> Permission hierarchy: `swifthavert.*` → includes `admin` + `use`

---

## 🔌 Plugin Integrations

| Plugin | Status | Details |
|--------|--------|---------|
| **WorldGuard** | ✅ Implemented | Registers custom flag `swift-harvest`. Respects region protection with per-player permission query. Gracefully degrades if not installed. |
| GriefPrevention | 📋 Planned | — |
| PlaceholderAPI | 📋 Planned | — |
| McMMO | 📋 Planned | — |

### WorldGuard Integration Details

SwiftHarvest registers a **custom region flag**: `swift-harvest`

- **Default**: `ALLOW` (SwiftHarvest works unless explicitly denied)
- **Usage**: Deny in spawn/protected regions: `/region flag spawn swift-harvest deny`
- **No crash if missing**: Plugin loads fine without WorldGuard; integration auto-enables when detected

---

## 🔧 Build from Source

### Prerequisites

- **JDK** 21+
- **Maven** 3.9+

### Build

```bash
# Clone & build
git clone https://github.com/Syaaddd/SwiftHarvest.git
cd SwiftHarvest
mvn clean package

# Output: target/SwiftHarvest-1.3.0.jar
```

Or use the included scripts:

| Platform | Command |
|----------|---------|
| Windows | `build.bat` |
| Linux / macOS | `./build.sh` |

---

## 📖 Changelog

### v1.3.0 — Core Improvements

- ✨ Fortune & Silk Touch enchantment support with configurable fortune cap
- ✨ Diagonal block detection (**10 directions**: 6 cardinal + 4 horizontal diagonals)
- ✨ Animated block breaking: **Ripple**, **Wave**, **Random** styles
- ✨ Strict timber detection — **trunk-based anti-bleed algorithm** with configurable radius
- ✨ **Per-tier tool configuration** for both VeinMiner AND Timber (max-blocks, durability multiplier, cooldown multiplier)
- ✨ **3 activation modes**: Sneak / Toggle / Hold (with threshold)
- ✨ Low durability guard — warns player instead of breaking tool mid-use
- ✨ Fully customizable messages via `messages.yml`
- 🔧 Optimized **BlockScanner** with Long position encoding (~40% memory reduction vs BlockPos objects)
- 🔧 WorldGuard integration with custom `swift-harvest` region flag

### v1.2.0

- Updated for Paper/Minecraft **1.21.11**
- Migrated tool durability to modern `Damageable` API
- Added **Pale Oak** log support
- Added **sound effect** on activation
- Timber breaks **leaves** by default
- Messages changed to English

### v1.0.0 — Initial Release

- VeinMiner with BFS algorithm
- Timber tree chopping
- Preview particles system
- Smart durability cost
- Drop consolidation (merge + inventory-first)
- WorldGuard integration
- Configurable tool and block lists
- World blacklist

---

## 📄 License & Support

**[MIT License](LICENSE)** — free to use, modify, and distribute.

| Resource | Link |
|----------|------|
| 🐛 Report Issues | [GitHub Issues](https://github.com/Syaaddd/SwiftHarvest/issues) |
| 💬 Discussions | [GitHub Discussions](https://github.com/Syaaddd/SwiftHarvest/discussions) |
| 📥 Releases | [GitHub Releases](https://github.com/Syaaddd/SwiftHarvest/releases) |

Pull requests are welcome! See [contributing guidelines](CONTRIBUTING.md) for details.

---

<p align="center">
  <strong>SwiftHarvest</strong> — Made with ⚡ by <a href="https://github.com/Syaaddd">Syaaddd</a>
</p>
