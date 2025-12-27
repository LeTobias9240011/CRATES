package de.bytecodes.crates.gui;

import de.bytecodes.crates.CratesPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KeyEditorGUI {

    private final CratesPlugin plugin;
    private final Player player;
    private final Inventory inventory;

    public KeyEditorGUI(CratesPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(null, 54, "§b§lCRATES §8» Keys");
        setupInventory();
    }

    private void setupInventory() {
        ItemStack border = createItem(Material.GRAY_STAINED_GLASS_PANE, "§7");

        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, border);
        }

        for (int row = 1; row < 6; row++) {
            inventory.setItem(row * 9, border);
            inventory.setItem(row * 9 + 8, border);
        }

        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, border);
        }

        try {
            List<Map<String, Object>> crates = plugin.getDatabase().getAllCrates();
            int slot = 10;

            for (Map<String, Object> crate : crates) {
                if (slot % 9 == 0 || slot % 9 == 8) {
                    slot++;
                }
                if (slot >= 45) break;

                String crateName = (String) crate.get("name");
                String keyType = (String) crate.get("key_type");
                int playerKeys = plugin.getVirtualKeyManager().getKeyAmount(player, crateName);

                ItemStack keyItem = createItem(Material.TRIPWIRE_HOOK, "§e" + crateName,
                        "§7Schlüssel-Typ: §f" + keyType,
                        "",
                        "§7Deine Schlüssel: §a" + playerKeys,
                        "",
                        "§7Befehle:",
                        "§e/crates givekey <spieler> " + crateName + " [anzahl]",
                        "§e/crates removekey <spieler> " + crateName + " [anzahl]");
                inventory.setItem(slot, keyItem);
                slot++;
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Fehler beim Laden der Schlüssel: " + e.getMessage());
        }

        ItemStack infoButton = createItem(Material.BOOK, "§b§lSchlüssel-Info",
                "§7Schlüssel werden automatisch",
                "§7pro Crate verwaltet.",
                "",
                "§7Verwende die Befehle:",
                "§e/crates givekey <spieler> <crate> [anzahl]",
                "§e/crates removekey <spieler> <crate> [anzahl]",
                "§e/crates giveall <crate> [anzahl]");
        inventory.setItem(4, infoButton);

        ItemStack backButton = createItem(Material.ARROW, "§cZurück §8»", "§7 §8(§7Linksklick§8)");
        inventory.setItem(45, backButton);

        ItemStack closeButton = createItem(Material.BARRIER, "§4Menu Schließen!", "§7 §8(§7Linksklick§8)");
        inventory.setItem(53, closeButton);
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore.length > 0) {
                List<String> loreList = new ArrayList<>();
                for (String line : lore) {
                    loreList.add(line);
                }
                meta.setLore(loreList);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    public void open() {
        player.openInventory(inventory);
    }
}
