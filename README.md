# Nether-Portal-Finder

A client-side Fabric mod for Minecraft 1.21.1 that helps you find nearby Nether portals.

## Features

- `/findportal` command to scan for nearby Nether portals
- Detects both lit portals (with nether_portal blocks) and unlit portal frames
- Saves discovered portals to a persistent registry (JSON)
- Configurable portal sizes and scan settings via Mod Menu (using Cloth Config)
- Scans within render distance for optimal performance

## Building

This mod requires access to the Fabric Maven repository. To build:

```bash
./gradlew build
```

The compiled mod JAR will be in `build/libs/`.

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 1.21.1
2. Place the mod JAR in your `.minecraft/mods` folder
3. (Optional) Install [Mod Menu](https://modrinth.com/mod/modmenu) to access the configuration screen

## Configuration

Configuration is stored in `.minecraft/config/findportal.toml`:

- `showUnlitPortals`: Whether to detect unlit portal frames (default: true)
- `validPortalSizes`: List of valid portal sizes to detect (default: ["2x3", "3x3", "4x5", "23x23"])
- `pruneDays`: Days after which old portal records are pruned (default: 30)
- `maxScanRadius`: Maximum scan radius in blocks (default: 256)

## Usage

Use the `/findportal` command in-game to:
1. Scan loaded chunks for nearby portals
2. If found, displays portal location, size, and distance
3. If no portal in loaded area, searches saved registry for nearest known portal
4. All discovered portals are saved to `.minecraft/config/findportal/portals.json`

## License

MIT License - see LICENSE file for details.
