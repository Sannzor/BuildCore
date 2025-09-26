package me.pinglagger.buildCore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

public final class BuildCore extends JavaPlugin implements CommandExecutor, Listener {

    private Map<String, BuildLocation> builds = new HashMap<>();
    private Map<UUID, String> playerBuilds = new HashMap<>();
    private FileConfiguration config;

    @Override
    public void onEnable() {
        config = getConfig();
        saveDefaultConfig();
        loadBuilds();
        getCommand("builds").setExecutor(this);
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        saveBuilds();
    }

    private void loadBuilds() {
        builds.clear();
        ConfigurationSection sec = config.getConfigurationSection("builds");
        if (sec != null) {
            for (String key : sec.getKeys(false)) {
                ConfigurationSection b = sec.getConfigurationSection(key);
                World w = getServer().getWorld(b.getString("world"));
                double x = b.getDouble("x");
                double y = b.getDouble("y");
                double z = b.getDouble("z");
                Material mat = Material.valueOf(b.getString("material"));
                Location loc = new Location(w, x, y, z);
                builds.put(key, new BuildLocation(loc, mat));
            }
        }
    }

    private void saveBuilds() {
        config.set("builds", null);
        for (Map.Entry<String, BuildLocation> entry : builds.entrySet()) {
            String path = "builds." + entry.getKey();
            BuildLocation bl = entry.getValue();
            config.set(path + ".world", bl.loc.getWorld().getName());
            config.set(path + ".x", bl.loc.getX());
            config.set(path + ".y", bl.loc.getY());
            config.set(path + ".z", bl.loc.getZ());
            config.set(path + ".material", bl.material.name());
        }
        saveConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Player p = (Player) sender;
        String cmdName = cmd.getName();
        if (cmdName.equals("builds")) {
            if (args.length == 0) {
                if (!p.hasPermission("buildcore.menu")) {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.no-permission")));
                    return true;
                }
                openGUI(p, 1);
                return true;
            }
            String sub = args[0].toLowerCase();
            if (sub.equals("create")) {
                if (!p.hasPermission("buildcore.create")) {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.no-permission")));
                    return true;
                }
                if (args.length < 2) {
                    p.sendMessage("Usage: /builds create <name> [material]");
                    return true;
                }
                String name = args[1];
                Material mat = Material.PAPER;
                if (args.length > 2) {
                    mat = Material.matchMaterial(args[2].toUpperCase());
                    if (mat == null) {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.invalid-material").replace("{material}", args[2])));
                        return true;
                    }
                }
                if (builds.containsKey(name)) {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.build-exists").replace("{name}", name)));
                    return true;
                }
                Location loc = p.getLocation();
                builds.put(name, new BuildLocation(loc, mat));
                saveBuilds();
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.build-created").replace("{name}", name)));
            } else if (sub.equals("delete")) {
                if (!p.hasPermission("buildcore.delete")) {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.no-permission")));
                    return true;
                }
                if (args.length < 2) {
                    p.sendMessage("Usage: /builds delete <name>");
                    return true;
                }
                String name = args[1];
                if (!builds.containsKey(name)) {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.build-not-found").replace("{name}", name)));
                    return true;
                }
                builds.remove(name);
                saveBuilds();
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.build-deleted").replace("{name}", name)));
            } else if (sub.equals("teleport")) {
                if (args.length < 2) {
                    p.sendMessage("Usage: /builds teleport <name>");
                    return true;
                }
                teleport(p, args[1]);
            } else {
                p.sendMessage("Unknown subcommand. Use /builds for menu.");
            }
            return true;
        } else if (cmdName.equals("tp")) {
            if (!p.hasPermission("buildcore.teleport")) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.no-permission")));
                return true;
            }
            if (args.length != 1) {
                p.sendMessage("Usage: /tp <player>");
                return true;
            }
            Player target = getServer().getPlayer(args[0]);
            if (target == null) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.player-not-found").replace("{player}", args[0])));
                return true;
            }
            p.teleport(target);
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.teleport-to-player").replace("{player}", target.getName())));
        } else if (cmdName.equals("tphere")) {
            if (!p.hasPermission("buildcore.teleport")) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.no-permission")));
                return true;
            }
            if (args.length != 1) {
                p.sendMessage("Usage: /tphere <player>");
                return true;
            }
            Player target = getServer().getPlayer(args[0]);
            if (target == null) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.player-not-found").replace("{player}", args[0])));
                return true;
            }
            target.teleport(p);
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.teleport-here").replace("{player}", target.getName())));
        } else if (cmdName.equals("tppos")) {
            if (!p.hasPermission("buildcore.teleport")) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.no-permission")));
                return true;
            }
            if (args.length != 3) {
                p.sendMessage("Usage: /tppos <x> <y> <z>");
                return true;
            }
            try {
                double x = Double.parseDouble(args[0]);
                double y = Double.parseDouble(args[1]);
                double z = Double.parseDouble(args[2]);
                p.teleport(new Location(p.getWorld(), x, y, z));
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.teleport-pos").replace("{x}", args[0]).replace("{y}", args[1]).replace("{z}", args[2])));
            } catch (NumberFormatException e) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.invalid-coordinates")));
            }
        } else if (cmdName.equals("gmc")) {
            if (!p.hasPermission("buildcore.gamemode")) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.no-permission")));
                return true;
            }
            p.setGameMode(GameMode.CREATIVE);
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.gamemode-creative")));
        } else if (cmdName.equals("gms")) {
            if (!p.hasPermission("buildcore.gamemode")) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.no-permission")));
                return true;
            }
            p.setGameMode(GameMode.SURVIVAL);
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.gamemode-survival")));
        } else if (cmdName.equals("gmsp")) {
            if (!p.hasPermission("buildcore.gamemode")) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.no-permission")));
                return true;
            }
            p.setGameMode(GameMode.SPECTATOR);
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.gamemode-spectator")));
        }
        return true;
    }

    private void openGUI(Player p, int page) {
        String title = config.getString("messages.gui-title").replace("{page}", String.valueOf(page));
        Inventory inv = Bukkit.createInventory(null, 54, title);
        List<String> buildNames = new ArrayList<>(builds.keySet());
        int perPage = 45;
        int start = (page - 1) * perPage;
        int end = Math.min(start + perPage, buildNames.size());
        for (int i = start; i < end; i++) {
            String name = buildNames.get(i);
            BuildLocation bl = builds.get(name);
            ItemStack item = new ItemStack(bl.material);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.GREEN + name);
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "World: " + bl.loc.getWorld().getName());
            lore.add(ChatColor.GRAY + "X: " + bl.loc.getBlockX());
            lore.add(ChatColor.GRAY + "Y: " + bl.loc.getBlockY());
            lore.add(ChatColor.GRAY + "Z: " + bl.loc.getBlockZ());
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(i - start, item);
        }
        if (page > 1) {
            ItemStack prev = new ItemStack(Material.ARROW);
            ItemMeta pm = prev.getItemMeta();
            pm.setDisplayName(ChatColor.YELLOW + "Previous Page");
            prev.setItemMeta(pm);
            inv.setItem(45, prev);
        }
        if (end < buildNames.size()) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta nm = next.getItemMeta();
            nm.setDisplayName(ChatColor.YELLOW + "Next Page");
            next.setItemMeta(nm);
            inv.setItem(53, next);
        }
        p.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getView().getTitle().startsWith("Build Locations")) {
            e.setCancelled(true);
            ItemStack item = e.getCurrentItem();
            if (item == null) return;
            if (item.getType() == Material.ARROW) {
                String name = item.getItemMeta().getDisplayName();
                int currentPage = getCurrentPage(e.getView().getTitle());
                if (name.contains("Previous")) {
                    openGUI((Player) e.getWhoClicked(), currentPage - 1);
                } else {
                    openGUI((Player) e.getWhoClicked(), currentPage + 1);
                }
            } else {
                String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
                teleport((Player) e.getWhoClicked(), name);
            }
        }
    }

    private int getCurrentPage(String title) {
        String[] parts = title.split(" ");
        return Integer.parseInt(parts[parts.length - 1]);
    }

    private void teleport(Player p, String name) {
        if (!builds.containsKey(name)) {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.build-not-found").replace("{name}", name)));
            return;
        }
        BuildLocation bl = builds.get(name);
        p.teleport(bl.loc);
        playerBuilds.put(p.getUniqueId(), name);
        updateScoreboard(p);
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.teleport-success").replace("{name}", name)));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (config.getBoolean("scoreboard.enabled")) {
            updateScoreboard(e.getPlayer());
        }
    }

    private void updateScoreboard(Player p) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();
        Objective obj = board.registerNewObjective("buildcore", "dummy");
        obj.setDisplayName(ChatColor.translateAlternateColorCodes('&', config.getString("scoreboard.title")));
        List<String> lines = config.getStringList("scoreboard.lines");
        int score = lines.size();
        for (String line : lines) {
            String replaced = line.replace("{server}", getServer().getName())
                    .replace("{online}", String.valueOf(getServer().getOnlinePlayers().size()))
                    .replace("{world}", p.getWorld().getName())
                    .replace("{location}", playerBuilds.getOrDefault(p.getUniqueId(), "Wilderness"));
            Score s = obj.getScore(ChatColor.translateAlternateColorCodes('&', replaced));
            s.setScore(score--);
        }
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        p.setScoreboard(board);
    }

    static class BuildLocation {
        Location loc;
        Material material;
        BuildLocation(Location l, Material m) {
            loc = l;
            material = m;
        }
    }
}
