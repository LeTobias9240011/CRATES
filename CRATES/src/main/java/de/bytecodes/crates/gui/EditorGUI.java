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

public class EditorGUI {

    private final CratesPlugin plugin;
    private final Player player;
    private final Inventory inventory;

    public EditorGUI(CratesPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(null, 45, "§b§lCRATES §8» Editor");
        setupInventory();
    }

    private void setupInventory() {
        ItemStack border = createItem(Material.GRAY_STAINED_GLASS_PANE, "§7");

        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, border);
        }

        for (int row = 1; row < 4; row++) {
            inventory.setItem(row * 9, border);
            inventory.setItem(row * 9 + 8, border);
        }

        for (int i = 36; i < 44; i++) {
            inventory.setItem(i, border);
        }

        int crateCount = 0;
        int totalRewards = 0;
        try {
            var crates = plugin.getDatabase().getAllCrates();
            crateCount = crates.size();
            for (var crate : crates) {
                String crateName = (String) crate.get("name");
                totalRewards += plugin.getDatabase().getRewards(crateName).size();
            }
        } catch (Exception e) {
        }

        ItemStack cratesButton = createItem(Material.CHEST, "§b§lCrates §8»", 
                "§7Verwalte alle Crates",
                "",
                "§7Aktuelle Crates: §e" + crateCount,
                "§7Gesamt Rewards: §e" + totalRewards,
                "",
                "§aKlicke zum Öffnen");
        inventory.setItem(11, cratesButton);

        ItemStack keysButton = createItem(Material.TRIPWIRE_HOOK, "§b§lKeys §8»",
                "§7Verwalte Schlüssel",
                "",
                "§7Schlüssel werden pro Crate verwaltet",
                "",
                "§aKlicke zum Öffnen");
        inventory.setItem(15, keysButton);

        String maintenanceStatus = plugin.isMaintenanceMode() ? "§c§lAN" : "§a§lAUS";
        ItemStack settingsButton = createItem(Material.COMPARATOR, "§b§lStatus §8»",
                "§7Wartungsmodus: " + maintenanceStatus,
                "",
                "§7Verwende §e/crates wartungen <on/off>");
        inventory.setItem(13, settingsButton);

        ItemStack reloadButton = createItem(Material.EMERALD, "§a§lReload §8»",
                "§7Lädt das Plugin neu",
                "",
                "§aKlicke zum Neuladen");
        inventory.setItem(29, reloadButton);

        ItemStack closeButton = createItem(Material.BARRIER, "§4Menu Schließen!", "§7 §8(§7Linksklick§8)");
        inventory.setItem(44, closeButton);
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
