package de.bytecodes.crates.util;

import de.bytecodes.crates.CratesPlugin;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VirtualKeyManager {

    private final CratesPlugin plugin;
    private final Map<UUID, Map<String, Integer>> playerKeys = new HashMap<>();

    public VirtualKeyManager(CratesPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Gib einem Spieler virtuelle Schlüssel
     */
    public void giveKey(Player player, String crateName, int amount) {
        try {
            plugin.getDatabase().giveKey(player.getUniqueId().toString(), crateName, amount);
            loadPlayerKeys(player);
        } catch (Exception e) {
            plugin.getLogger().warning("Fehler beim Geben von Schlüsseln: " + e.getMessage());
        }
    }

    /**
     * Entferne virtuelle Schlüssel von einem Spieler
     */
    public void removeKey(Player player, String crateName, int amount) {
        try {
            plugin.getDatabase().removeKey(player.getUniqueId().toString(), crateName, amount);
            loadPlayerKeys(player);
        } catch (Exception e) {
            plugin.getLogger().warning("Fehler beim Entfernen von Schlüsseln: " + e.getMessage());
        }
    }

    /**
     * Hole die Anzahl der Schlüssel eines Spielers für eine Crate
     */
    public int getKeyAmount(Player player, String crateName) {
        try {
            return plugin.getDatabase().getKeyAmount(player.getUniqueId().toString(), crateName);
        } catch (Exception e) {
            plugin.getLogger().warning("Fehler beim Abrufen der Schlüsselanzahl: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Lade alle Schlüssel eines Spielers
     */
    public void loadPlayerKeys(Player player) {
        try {
            Map<String, Integer> keys = plugin.getDatabase().getAllPlayerKeys(player.getUniqueId().toString());
            playerKeys.put(player.getUniqueId(), keys);
        } catch (Exception e) {
            plugin.getLogger().warning("Fehler beim Laden der Spielerschlüssel: " + e.getMessage());
        }
    }

    /**
     * Prüfe, ob ein Spieler genug Schlüssel für eine Crate hat
     */
    public boolean hasEnoughKeys(Player player, String crateName, int amount) {
        return getKeyAmount(player, crateName) >= amount;
    }

    /**
     * Zeige die Schlüssel eines Spielers an
     */
    public void showKeys(Player player) {
        try {
            player.sendMessage("§b§lCRATES §7» §aDeine Schlüssel:");
            player.sendMessage("");
            
            Map<String, Integer> keys = plugin.getDatabase().getAllPlayerKeys(player.getUniqueId().toString());
            
            if (keys.isEmpty()) {
                player.sendMessage("§7Du hast keine Schlüssel.");
            } else {
                for (Map.Entry<String, Integer> entry : keys.entrySet()) {
                    player.sendMessage("  §e" + entry.getKey() + "§7: §a" + entry.getValue() + " Schlüssel");
                }
            }
            player.sendMessage("");
        } catch (Exception e) {
            player.sendMessage("§cFehler beim Laden der Schlüssel: " + e.getMessage());
        }
    }

    /**
     * Hole alle Schlüssel eines Spielers
     */
    public Map<String, Integer> getAllKeys(Player player) {
        try {
            return plugin.getDatabase().getAllPlayerKeys(player.getUniqueId().toString());
        } catch (Exception e) {
            plugin.getLogger().warning("Fehler beim Abrufen aller Schlüssel: " + e.getMessage());
            return new HashMap<>();
        }
    }
}
