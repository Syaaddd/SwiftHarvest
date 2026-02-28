# SwiftHarvest

**Mine Faster, Lag Less. The Ultimate Vein & Timber Solution.**

A modern, lightweight Minecraft server plugin for PaperMC that brings VeinMiner and Timber functionality with performance optimizations.

---

## Features

- **VeinMiner** - Mine entire ore veins with one swing (Pickaxe + Sneak + Break)
- **Timber** - Chop entire trees instantly (Axe + Sneak + Break)
- **Preview Particles** - See which blocks will be mined before breaking
- **Smart Durability** - Tool durability decreases proportionally to blocks broken
- **Drop Consolidation** - Items automatically go to inventory (no lag from 100+ drops)
- **XP Orb Consolidation** - Multiple XP drops merge into one orb
- **WorldGuard Support** - Respects region protection flags
- **Configurable** - Full control over tools, blocks, and limits

---

## Commands

| Command | Description |
|---------|-------------|
| `/swifthavert reload` | Reload configuration (admin) |

**Aliases:** `/sh`

---

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `swifthavert.use` | Use SwiftHarvest features | true |
| `swifthavert.veinminer` | Use VeinMiner | true |
| `swifthavert.timber` | Use Timber | true |
| `swifthavert.bypass` | Bypass world blacklist | op |
| `swifthavert.admin` | Admin commands | op |

---

## Usage

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

### Preview Particles
When enabled in config, aiming at a block while sneaking will show green particles indicating how many blocks will be affected.

---

## Configuration

Example `config.yml`:

```yaml
settings:
  require-sneak: true
  max-blocks: 64
  cooldown: 10
  preview-particles: true

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

timber:
  enabled: true
  allowed-tools:
    - WOODEN_AXE
    - STONE_AXE
    - IRON_AXE
    - GOLDEN_AXE
    - DIAMOND_AXE
    - NETHERITE_AXE
  break-leaves: true

worlds:
  blacklist:
    - world_nether
    - world_the_end
```

---

## Technical Details

- **Algorithm**: BFS (Breadth-First Search) - No recursion, prevents StackOverflow
- **Max Blocks**: Configurable limit (default: 64) prevents lag
- **Thread Safety**: Block calculation runs async, breaking on main thread
- **Drop System**: Consolidates drops to inventory before dropping

---

## Installation

1. Download the plugin JAR file
2. Place it in your server's `plugins` folder
3. Start the server
4. Configure `config.yml` to your needs
5. Use `/swifthavert reload` to reload config after changes

**Requirements:**
- PaperMC 1.21+ or Purpur
- Java 21

---

## Changelog

### Version 1.1.0 (Minor)
- Added sound effect when VeinMiner or Timber is activated
- Timber now breaks leaves by default
- Added PALE_OAK_LEAVES support for latest Minecraft
- Messages changed to English

### Version 1.0.0 (Major)
- Initial release
- VeinMiner functionality with BFS algorithm
- Timber functionality for tree chopping
- Preview particles system
- Smart durability cost
- Drop consolidation
- XP orb consolidation
- WorldGuard integration
- Configurable tool and block lists
- World blacklist support

---

## Support

- Issues: [GitHub Issues](https://github.com/Syaaddd/SwiftHarvest/issues)

---

**License:** MIT  
**Version:** 1.1.0
