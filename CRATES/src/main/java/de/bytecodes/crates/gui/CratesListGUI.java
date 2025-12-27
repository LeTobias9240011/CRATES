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

public class CratesListGUI {

    private final CratesPlugin plugin;
    private final Player player;
    private final Inventory inventory;

    public CratesListGUI(CratesPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(null, 54, "§b§lCRATES §8» Crates");
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

            for (Map<String, Object> crate : crates) {
                if (slot % 9 == 0 || slot % 9 == 8) {
                }

                String material = (String) crate.get("material");
                String crateName = (String) crate.get("name");
                
                Material mat = Material.CHEST;
                try {
                    mat = Material.valueOf(material.toUpperCase());
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Ungültiges Material: " + material);
                }

                ItemStack crateItem = new ItemStack(mat);
                ItemMeta meta = crateItem.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName("§a" + crateName);
                    List<String> lore = new ArrayList<>();
                    lore.add("§7Linksklick zum Bearbeiten");
                    lore.add("§7Rechtsklick zum Löschen");
                    meta.setLore(lore);
                    crateItem.setItemMeta(meta);
                }
                inventory.setItem(slot, crateItem);
                slot++;
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Fehler beim Laden der Crates: " + e.getMessage());
        }

        ItemStack addButton = createItem(Material.LIME_DYE, "§a§lCrate hinzufügen", "§7Klicke zum Erstellen");
        inventory.setItem(48, addButton);

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
