# SwiftHarvest

![Minecraft](https://img.shields.io/badge/Minecraft-1.21+-brightgreen.svg)
![PaperMC](https://img.shields.io/badge/PaperMC-1.21.11+-brightgreen.svg)
![Java](https://img.shields.io/badge/Java-21-orange.svg)
![License](https://img.shields.io/badge/License-MIT-blue.svg)

**Mine Faster, Lag Less. The Ultimate Vein & Timber Solution.**

A modern, lightweight Minecraft server plugin for PaperMC that brings VeinMiner and Timber functionality with performance optimizations. 100% free and open source.

---

##  Features

### Core
- **VeinMiner** — Mine entire ore veins with one swing (Pickaxe + Sneak + Break)
- **Timber** — Chop entire trees instantly (Axe + Sneak + Break)
- **Smart Durability** — Tool durability decreases proportionally to blocks broken
- **Drop Consolidation** — Items automatically go to inventory (no lag from 100+ drops)
- **Fortune & Silk Touch Support** — Respects vanilla enchantment mechanics
- **WorldGuard Support** — Respects region protection flags

### Visual
- **Preview Particles** — See which blocks will be mined before breaking
- **Animated Block Breaking** — Blocks break with smooth ripple/wave/random animations
- **Sound Effects** — Satisfying sound on activation

### Advanced
- **Diagonal Detection** — Scan veins in 10 directions (including diagonals)
- **Strict Tree Detection** — Anti-bleed algorithm prevents accidentally chopping neighboring trees
- **Per-Tier Tool Config** — Different limits per tool tier (e.g., Netherite = 128 blocks, Wood = 8)
- **Multiple Activation Modes** — Sneak, Toggle, or Hold-to-activate

---

## 📦 Installation

1. Download the latest JAR from the [Releases](https://github.com/Syaaddd/SwiftHarvest/releases) page
2. Place it in your server's `plugins` folder
3. Start the server
4. Configure `config.yml` to your needs
5. Use `/sh reload` to reload config after changes

**Requirements:**
- PaperMC 1.21.11+ or Purpur
- Java 21

---

## 🎮 Usage

### VeinMiner
1. Hold a **Pickaxe**
2. **Sneak** (hold Shift)
3. **Break** an ore block
4. All connected ores of the same type will break!

### Timber
1. Hold an **Axe**
2. **Sneak** (hold Shift)
3. **Break** a log block
4. The entire tree will be chopped!

### Activation Modes
| Mode | How to Use |
|------|------------|
| **Sneak** (default) | Hold Shift + break block |
| **Toggle** | Run `/sh toggle` to enable/disable |
| **Hold** | Hold Shift for ~1 second to activate |

---

## ⚙️ Configuration

Full `config.yml` example:

```yaml
settings:
  require-sneak: true
  max-blocks: 64
  cooldown: 10
  preview-particles: true
  activation-mode: "sneak"    # sneak | toggle | hold
  hold-ticks: 10

veinminer:
  enabled: true
  scan-mode: "cardinal"       # cardinal (6-arah) | diagonal (10-arah)
  honor-fortune: true
  honor-silk-touch: true
  use-tiers: false            # enable per-tool limits
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

timber:
  enabled: true
  break-leaves: true
  detection: "strict"         # strict (anti-bleed) | loose (original BFS)
  max-horizontal-radius: 2
  allowed-tools:
    - WOODEN_AXE
    - STONE_AXE
    - IRON_AXE
    - GOLDEN_AXE
    - DIAMOND_AXE
    - NETHERITE_AXE

animation:
  enabled: false
  blocks-per-tick: 4
  style: "ripple"             # ripple | wave | random

worlds:
  blacklist:
    - world_nether
    - world_the_end
```

---

## 📋 Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/sh reload` | Reload configuration | `swifthavert.admin` |
| `/sh toggle` | Toggle SwiftHarvest on/off | `swifthavert.use` |

---

##  Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `swifthavert.*` | All permissions | — |
| `swifthavert.use` | Use SwiftHarvest features | `true` |
| `swifthavert.veinminer` | Use VeinMiner | `true` |
| `swifthavert.timber` | Use Timber | `true` |
| `swifthavert.admin` | Admin commands | `op` |
| `swifthavert.bypass` | Bypass world blacklist | `op` |

---

## 🔧 Build from Source

```bash
# Prerequisites: JDK 21 + Maven 3.9+
mvn clean package

# Output: target/SwiftHarvest-<version>.jar
```

Or use the included build scripts:
- **Windows:** `build.bat`
- **Linux/Mac:** `./build.sh`

---

## 🔌 Plugin Integrations

| Plugin | Support |
|--------|---------|
| WorldGuard | Region protection |
| GriefPrevention | *(planned)* |
| PlaceholderAPI | *(planned)* |
| McMMO | *(planned)* |

---

## 📖 Changelog

### v1.3.0 — Core Improvements
- Fortune & Silk Touch support
- Diagonal block detection (10 directions)
- Animated block breaking (ripple/wave/random)
- Strict timber detection (anti-bleed algorithm)
- Per-tier tool configuration
- 3 activation modes: Sneak / Toggle / Hold
- Optimized BlockScanner (Long encoding, ~40% memory reduction)

### v1.2.0
- Updated for Paper/Minecraft 1.21.11
- Migrated tool durability to modern `Damageable` API
- Added Pale Oak log support
- Added sound effect on activation
- Timber breaks leaves by default
- Messages changed to English

### v1.0.0 — Initial Release
- VeinMiner with BFS algorithm
- Timber tree chopping
- Preview particles system
- Smart durability cost
- Drop consolidation
- WorldGuard integration
- Configurable tool and block lists

---

## 📄 License

MIT License — see [LICENSE](LICENSE) for details.

## 🤝 Support

- Report issues: [GitHub Issues](https://github.com/Syaaddd/SwiftHarvest/issues)
- Pull requests are welcome!
