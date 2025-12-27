package de.bytecodes.crates.gui;

import de.bytecodes.crates.CratesPlugin;
import de.bytecodes.crates.util.SetCrateMode;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SetCrateGUI {

    private final CratesPlugin plugin;
    private final Player player;
    private final Inventory inventory;

    public SetCrateGUI(CratesPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(null, 54, "§b§lCRATES §8» Crate platzieren");
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

        ItemStack infoItem = createItem(Material.BOOK, "§b§lCrate platzieren",
                "§7Wähle eine Crate aus,",
                "§7um sie in der Welt zu platzieren.",
                "",
                "§7Danach platzierst du einen Block",
                "§7an der gewünschten Position.");
        inventory.setItem(4, infoItem);

        try {
            List<Map<String, Object>> crates = plugin.getDatabase().getAllCrates();
            int slot = 10;

            for (Map<String, Object> crate : crates) {
                if (slot % 9 == 0 || slot % 9 == 8) {
                    slot++;
                }
                if (slot >= 45) break;

                String name = (String) crate.get("name");
                String materialStr = (String) crate.get("material");
                String keyType = (String) crate.get("key_type");

                Material material;
                try {
                    material = Material.valueOf(materialStr);
                } catch (Exception e) {
                    material = Material.CHEST;
                }

                int rewardCount = plugin.getDatabase().getRewards(name).size();
                int crateId = (int) crate.get("id");
                int locationCount = plugin.getDatabase().getCrateLocations(crateId).size();

                ItemStack crateItem = createItem(material, "§e§l" + name,
                        "§7Schlüssel-Typ: §f" + keyType,
                        "§7Rewards: §f" + rewardCount,
                        "§7Platziert: §f" + locationCount + "x",
                        "",
                        "§aKlicken zum Auswählen!");
                inventory.setItem(slot, crateItem);
                slot++;
            }

            if (crates.isEmpty()) {
                ItemStack noItem = createItem(Material.BARRIER, "§cKeine Crates vorhanden",
                        "§7Erstelle zuerst eine Crate",
                        "§7im Editor!");
                inventory.setItem(22, noItem);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Fehler beim Laden der Crates: " + e.getMessage());
        }

        ItemStack closeButton = createItem(Material.BARRIER, "§4Abbrechen", "§7 §8(§7Linksklick§8)");
        inventory.setItem(49, closeButton);
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
