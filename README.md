# BuildCore

BuildCore is a comprehensive Minecraft plugin designed for Spigot servers (compatible with versions 1.8.x to 1.21.x) that simplifies build location management, teleportation, and server administration. Developed by PingLagger, this plugin provides an intuitive GUI for handling builds, essential admin commands, and a customizable scoreboard to keep players informed.

## Key Features

- **Fully Configurable Build Locations**: Save, manage, and teleport to custom build spots with unique names and item materials (default: PAPER).
- **Easy-to-Use GUI**: Paginated inventory menu for selecting, viewing, and managing builds efficiently.
- **Teleportation Commands**: Quick access to player and coordinate teleports for seamless navigation.
- **Gamemode Switching**: Simple commands to toggle between Creative, Survival, and Spectator modes.
- **Dynamic Scoreboard**: Displays server info, online players, current world, and build location with a blue-and-white theme for better visibility.
- **Full Customization**: All messages, commands, and settings are configurable via `config.yml`. No hard-coded values!
- **Permission System**: Granular permissions for secure usage (e.g., `buildcore.create`, `buildcore.teleport`).

## Commands

- `/builds create <name> [material]` - Save your current location as a build (requires `buildcore.create`).
- `/builds delete <name>` - Remove a saved build (requires `buildcore.delete`).
- `/builds` - Open the build selection GUI (requires `buildcore.menu`).
- `/builds teleport <name>` - Teleport to a saved build.
- `/tp <player>` - Teleport to another player (requires `buildcore.teleport`).
- `/tphere <player>` - Bring a player to your location (requires `buildcore.teleport`).
- `/tppos <x> <y> <z>` - Teleport to specific coordinates (requires `buildcore.teleport`).
- `/gmc` - Switch to Creative mode (requires `buildcore.gamemode`).
- `/gms` - Switch to Survival mode (requires `buildcore.gamemode`).
- `/gmsp` - Switch to Spectator mode (requires `buildcore.gamemode`).

## Permissions

- `buildcore.create` (default: op) - Create builds.
- `buildcore.delete` (default: op) - Delete builds.
- `buildcore.menu` (default: true) - Open the GUI.
- `buildcore.teleport` (default: op) - Use teleport commands.
- `buildcore.gamemode` (default: op) - Change gamemodes.

## Installation

1. Download the latest JAR from Releases.
2. Place `BuildCore.jar` in your server's `plugins` folder.
3. Restart the server or use `/reload confirm`.
4. Edit `plugins/BuildCore/config.yml` for customizations (generated on first load).
5. Grant permissions via your permissions plugin (e.g., LuckPerms).

## Configuration

The plugin generates a `config.yml` with sections for messages, builds storage, and scoreboard. Example scoreboard lines:
```
scoreboard:
  enabled: true
  title: "&9BuildCore"
  lines:
    - ""
    - "&bServer"
    - "&f{server}"
    - ""
    - "&bOnline"
    - "&f{online}"
    - ""
    - "&bWorld"
    - "&f{world}"
    - ""
    - "&bLocation"
    - "&f{location}"
```

## Support & Contributions

- **Issues/Bugs**: Report on the Issues tab.
- **Features**: Suggest new ideas via Discussions.
- **Contribute**: Fork the repo, make changes, and submit a PR.
- **License**: MIT License - Free to use and modify.

For more details, check the [wiki](https://github.com/PingLagger/BuildCore/wiki) or join the Discord community.

⭐ Star this repo if you find it useful!  
📦 Built with Spigot API 1.8.8-R0.1-SNAPSHOT for broad compatibility.
