package de.bytecodes.crates.command;

import de.bytecodes.crates.CratesPlugin;
import de.bytecodes.crates.gui.MainMenuGUI;
import de.bytecodes.crates.gui.EditorGUI;
import de.bytecodes.crates.gui.SetCrateGUI;
import de.bytecodes.crates.util.SetCrateMode;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CratesCommand implements CommandExecutor, TabCompleter {

    private final CratesPlugin plugin;

    public CratesCommand(CratesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cDieser Befehl kann nur von Spielern ausgeführt werden!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            new MainMenuGUI(plugin, player).open();
            return true;
        }

        String subcommand = args[0].toLowerCase();

        if (subcommand.equals("keys") || subcommand.equals("schlüssel")) {
            plugin.getVirtualKeyManager().showKeys(player);
            return true;
        }

        if (subcommand.equals("help") || subcommand.equals("hilfe")) {
            showHelp(player);
            return true;
        }

        if (!player.hasPermission("crates.admin")) {
            player.sendMessage("§b§lCRATES §7» §cDu hast keine Berechtigung, diesen Befehl zu verwenden!");
            return true;
        }

        switch (subcommand) {
            case "editor":
                new EditorGUI(plugin, player).open();
                break;

            case "givekey":
                if (args.length < 3) {
                    player.sendMessage("§b§lCRATES §7» §cVerwendung: /crates givekey <spieler> <crate> [anzahl]");
                    return true;
                }
                handleGiveKey(player, args);
                break;

            case "removekey":
                if (args.length < 3) {
                    player.sendMessage("§b§lCRATES §7» §cVerwendung: /crates removekey <spieler> <crate> [anzahl]");
                    return true;
                }
                handleRemoveKey(player, args);
                break;

            case "setcrate":
                SetCrateMode.enableMode(player.getUniqueId());
                player.sendMessage("§b§lCRATES §7» §aSetCrate-Modus aktiviert!");
                player.sendMessage("§b§lCRATES §7» §aPlatziere einen Block für das Crate-Menü.");
                player.sendMessage("§b§lCRATES §7» §7Schreibe §c/crates cancel §7zum Abbrechen.");
                break;

            case "menu":
            case "open":
                new MainMenuGUI(plugin, player).open();
                break;

            case "wartungen":
            case "maintenance":
                if (args.length < 2) {
                    player.sendMessage("§b§lCRATES §7» §cVerwendung: /crates wartungen <on/off>");
                    return true;
                }
                handleMaintenance(player, args);
                break;

            case "preview":
                if (args.length < 2) {
                    player.sendMessage("§b§lCRATES §7» §cVerwendung: /crates preview <crate>");
                    return true;
                }
                handlePreview(player, args[1]);
                break;

            case "reload":
                handleReload(player);
                break;

            case "list":
                handleList(player);
                break;

            case "delete":
                if (args.length < 2) {
                    player.sendMessage("§b§lCRATES §7» §cVerwendung: /crates delete <crate>");
                    return true;
                }
                handleDelete(player, args[1]);
                break;

            case "giveall":
                if (args.length < 2) {
                    player.sendMessage("§b§lCRATES §7» §cVerwendung: /crates giveall <crate> [anzahl]");
                    return true;
                }
                handleGiveAll(player, args);
                break;

            case "cancel":
                if (SetCrateMode.isInMode(player.getUniqueId())) {
                    SetCrateMode.disableMode(player.getUniqueId());
                    player.sendMessage("§b§lCRATES §7» §cSetCrate-Modus abgebrochen.");
                } else {
                    player.sendMessage("§b§lCRATES §7» §cDu bist in keinem Modus, der abgebrochen werden kann.");
                }
                break;

            default:
                player.sendMessage("§b§lCRATES §7» §cUnbekannter Unterbefehl: " + subcommand);
                player.sendMessage("§b§lCRATES §7» §aVerwende §e/crates help §afür eine Liste aller Befehle.");
                break;
        }

        return true;
    }

    private void showHelp(Player player) {
        player.sendMessage("");
        player.sendMessage("§b§lCRATES §7» §aHilfe");
        player.sendMessage("§7━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage("§e/crates §7- Öffnet das Hauptmenü");
        player.sendMessage("§e/crates keys §7- Zeigt deine Schlüssel");
        player.sendMessage("§e/crates help §7- Zeigt diese Hilfe");

        if (player.hasPermission("crates.admin")) {
            player.sendMessage("");
            player.sendMessage("§c§lAdmin-Befehle:");
            player.sendMessage("§e/crates editor §7- Öffnet den Editor");
            player.sendMessage("§e/crates givekey <spieler> <crate> [anzahl] §7- Gibt Schlüssel");
            player.sendMessage("§e/crates removekey <spieler> <crate> [anzahl] §7- Entfernt Schlüssel");
            player.sendMessage("§e/crates giveall <crate> [anzahl] §7- Gibt allen Schlüssel");
            player.sendMessage("§e/crates setcrate §7- Platziert einen Crate-Block");
            player.sendMessage("§e/crates menu §7- Öffnet das Übersichts-Menü");
            player.sendMessage("§e/crates preview <crate> §7- Vorschau einer Crate");
            player.sendMessage("§e/crates list §7- Listet alle Crates");
            player.sendMessage("§e/crates delete <crate> §7- Löscht eine Crate");
            player.sendMessage("§e/crates wartungen <on/off> §7- Wartungsmodus");
            player.sendMessage("§e/crates reload §7- Lädt das Plugin neu");
        }
        player.sendMessage("§7━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage("");
    }

    private void handleGiveKey(Player player, String[] args) {
        String targetName = args[1];
        String crateName = args[2];
        int amount = 1;

        if (args.length > 3) {
            try {
                amount = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                player.sendMessage("§b§lCRATES §7» §cUngültige Anzahl!");
                return;
            }
        }

        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage("§b§lCRATES §7» §cSpieler nicht gefunden!");
            return;
        }

        try {
            if (plugin.getDatabase().getCrate(crateName) == null) {
                player.sendMessage("§b§lCRATES §7» §cCrate nicht gefunden!");
                return;
            }

            plugin.getVirtualKeyManager().giveKey(target, crateName, amount);
            player.sendMessage("§b§lCRATES §7» §a✓ Gab §e" + amount + "x §aSchlüssel an §e" + target.getName());
            target.sendMessage("§b§lCRATES §7» §aDu hast §e" + amount + "x §aSchlüssel für §e" + crateName + " §aerhalten!");
        } catch (Exception e) {
            player.sendMessage("§b§lCRATES §7» §cFehler: " + e.getMessage());
        }
    }

    private void handleRemoveKey(Player player, String[] args) {
        String targetName = args[1];
        String crateName = args[2];
        int amount = 1;

        if (args.length > 3) {
            try {
                amount = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                player.sendMessage("§b§lCRATES §7» §cUngültige Anzahl!");
                return;
            }
        }

        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage("§b§lCRATES §7» §cSpieler nicht gefunden!");
            return;
        }

        try {
            if (plugin.getDatabase().getCrate(crateName) == null) {
                player.sendMessage("§b§lCRATES §7» §cCrate nicht gefunden!");
                return;
            }

            plugin.getVirtualKeyManager().removeKey(target, crateName, amount);
            player.sendMessage("§b§lCRATES §7» §a✓ Entfernte §e" + amount + "x §aSchlüssel von §e" + target.getName());
        } catch (Exception e) {
            player.sendMessage("§b§lCRATES §7» §cFehler: " + e.getMessage());
        }
    }

    private void handleMaintenance(Player player, String[] args) {
        String status = args[1].toLowerCase();
        if (status.equals("on") || status.equals("an")) {
            plugin.setMaintenanceMode(true);
            player.sendMessage("§b§lCRATES §7» §aWartungsmodus: §c§lAN");
            player.sendMessage("§b§lCRATES §7» §aAlle Crates sind jetzt deaktiviert!");
            Bukkit.broadcastMessage("§b§lCRATES §7» §cCrates sind jetzt im Wartungsmodus!");
        } else if (status.equals("off") || status.equals("aus")) {
            plugin.setMaintenanceMode(false);
            player.sendMessage("§b§lCRATES §7» §aWartungsmodus: §a§lAUS");
            player.sendMessage("§b§lCRATES §7» §aAlle Crates sind jetzt aktiviert!");
            Bukkit.broadcastMessage("§b§lCRATES §7» §aCrates sind wieder verfügbar!");
        } else {
            player.sendMessage("§b§lCRATES §7» §cVerwendung: /crates wartungen <on/off>");
        }
    }

    private void handleReload(Player player) {
        try {
            plugin.getHologramManager().removeAllHolograms();

            plugin.getCrateLocationManager().loadCrateLocations();

            player.sendMessage("§b§lCRATES §7» §a✓ Plugin erfolgreich neu geladen!");
            plugin.getLogger().info("Plugin wurde neu geladen von " + player.getName());
        } catch (Exception e) {
            player.sendMessage("§b§lCRATES §7» §cFehler beim Neuladen: " + e.getMessage());
            plugin.getLogger().warning("Fehler beim Neuladen: " + e.getMessage());
        }
    }

    private void handlePreview(Player player, String crateName) {
        try {
            if (plugin.getDatabase().getCrate(crateName) == null) {
                player.sendMessage("§b§lCRATES §7» §cCrate nicht gefunden!");
                return;
            }
            new de.bytecodes.crates.gui.PreviewGUI(plugin, player, crateName).open();
        } catch (Exception e) {
            player.sendMessage("§b§lCRATES §7» §cFehler: " + e.getMessage());
        }
    }

    private void handleSetCrate(Player player, String crateName) {
        try {
            if (plugin.getDatabase().getCrate(crateName) == null) {
                player.sendMessage("§b§lCRATES §7» §cCrate nicht gefunden!");
                return;
            }
            SetCrateMode.enableMode(player.getUniqueId());
            player.sendMessage("§b§lCRATES §7» §aSetCrate-Modus aktiviert!");
            player.sendMessage("§b§lCRATES §7» §aPlatziere einen Block, um die Crate §e" + crateName + " §azu setzen.");
        } catch (Exception e) {
            player.sendMessage("§b§lCRATES §7» §cFehler: " + e.getMessage());
        }
    }

    private void handleList(Player player) {
        try {
            List<Map<String, Object>> crates = plugin.getDatabase().getAllCrates();
            if (crates.isEmpty()) {
                player.sendMessage("§b§lCRATES §7» §cKeine Crates vorhanden.");
                return;
            }

            player.sendMessage("");
            player.sendMessage("§b§lCRATES §7» §aAlle Crates:");
            player.sendMessage("§7━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

            for (Map<String, Object> crate : crates) {
                String name = (String) crate.get("name");
                String material = (String) crate.get("material");
                String keyType = (String) crate.get("key_type");

                List<Map<String, Object>> rewards = plugin.getDatabase().getRewards(name);
                int rewardCount = rewards.size();

                player.sendMessage("§e" + name + " §7- Material: §f" + material + " §7- Key: §f" + keyType + " §7- Rewards: §a" + rewardCount);
            }

            player.sendMessage("§7━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            player.sendMessage("§7Gesamt: §e" + crates.size() + " Crates");
            player.sendMessage("");
        } catch (Exception e) {
            player.sendMessage("§b§lCRATES §7» §cFehler: " + e.getMessage());
        }
    }

    private void handleDelete(Player player, String crateName) {
        try {
            if (plugin.getDatabase().getCrate(crateName) == null) {
                player.sendMessage("§b§lCRATES §7» §cCrate nicht gefunden!");
                return;
            }

            plugin.getDatabase().deleteCrateCompletely(crateName);
            player.sendMessage("§b§lCRATES §7» §a✓ Crate §e" + crateName + " §aerfolgreich gelöscht!");

            plugin.getHologramManager().removeAllHolograms();
            plugin.getCrateLocationManager().loadCrateLocations();
        } catch (Exception e) {
            player.sendMessage("§b§lCRATES §7» §cFehler: " + e.getMessage());
        }
    }

    private void handleGiveAll(Player player, String[] args) {
        String crateName = args[1];
        int amount = 1;

        if (args.length > 2) {
            try {
                amount = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                player.sendMessage("§b§lCRATES §7» §cUngültige Anzahl!");
                return;
            }
        }

        try {
            if (plugin.getDatabase().getCrate(crateName) == null) {
                player.sendMessage("§b§lCRATES §7» §cCrate nicht gefunden!");
                return;
            }

            int count = 0;
            for (Player target : Bukkit.getOnlinePlayers()) {
                plugin.getVirtualKeyManager().giveKey(target, crateName, amount);
                target.sendMessage("§b§lCRATES §7» §aDu hast §e" + amount + "x §aSchlüssel für §e" + crateName + " §aerhalten!");
                count++;
            }

            player.sendMessage("§b§lCRATES §7» §a✓ Gab §e" + amount + "x §aSchlüssel an §e" + count + " §aSpieler!");
        } catch (Exception e) {
            player.sendMessage("§b§lCRATES §7» §cFehler: " + e.getMessage());
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subcommands = new ArrayList<>(Arrays.asList("help", "keys"));

            if (sender.hasPermission("crates.admin")) {
                subcommands.addAll(Arrays.asList(
                    "editor", "givekey", "removekey", "setcrate",
                    "preview", "reload", "wartungen", "list", "delete", "giveall"
                ));
            }

            return subcommands.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && sender.hasPermission("crates.admin")) {
            String sub = args[0].toLowerCase();

            if (sub.equals("givekey") || sub.equals("removekey")) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }

            if (sub.equals("setcrate") || sub.equals("preview") || sub.equals("delete") || sub.equals("giveall")) {
                try {
                    return plugin.getDatabase().getAllCrates().stream()
                            .map(c -> (String) c.get("name"))
                            .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                } catch (Exception e) {
                    return completions;
                }
            }

            if (sub.equals("wartungen") || sub.equals("maintenance")) {
                return Arrays.asList("on", "off").stream()
                        .filter(s -> s.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        if (args.length == 3 && sender.hasPermission("crates.admin")) {
            String sub = args[0].toLowerCase();

            if (sub.equals("givekey") || sub.equals("removekey")) {
                try {
                    return plugin.getDatabase().getAllCrates().stream()
                            .map(c -> (String) c.get("name"))
                            .filter(n -> n.toLowerCase().startsWith(args[2].toLowerCase()))
                            .collect(Collectors.toList());
                } catch (Exception e) {
                    return completions;
                }
            }
        }

        return completions;
    }
}
