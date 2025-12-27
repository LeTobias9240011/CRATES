package de.bytecodes.crates.gui;

import de.bytecodes.crates.CratesPlugin;
import de.bytecodes.crates.util.CreateCrateMode;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CreateCrateGUI {

    private final CratesPlugin plugin;
    private final Player player;
    private final Inventory inventory;

    public CreateCrateGUI(CratesPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(null, 45, "§b§lCRATES §8» Crate erstellen");
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

        for (int i = 36; i < 45; i++) {
            inventory.setItem(i, border);
        }

        Map<String, Object> data = CreateCrateMode.getData(player.getUniqueId());
        String name = data != null && data.get("name") != null ? (String) data.get("name") : "§c(nicht gesetzt)";
        String material = data != null && data.get("material") != null ? (String) data.get("material") : "§c(nicht gesetzt)";
        String keyType = data != null && data.get("keyType") != null ? (String) data.get("keyType") : "§c(nicht gesetzt)";

        ItemStack nameButton = createItem(Material.NAME_TAG, "§b§lCrate Name", "§7Aktuell: §e" + name, "§7Klicke zum Ändern");
        inventory.setItem(11, nameButton);

        ItemStack materialButton = createItem(Material.CHEST, "§b§lMaterial", "§7Aktuell: §e" + material, "§7Klicke zum Ändern");
        inventory.setItem(13, materialButton);

        ItemStack keyTypeButton = createItem(Material.TRIPWIRE_HOOK, "§b§lSchlüssel-Typ", "§7Aktuell: §e" + keyType, "§7Klicke zum Ändern");
        inventory.setItem(15, keyTypeButton);

        ItemStack createButton = createItem(Material.LIME_DYE, "§a§lCrate erstellen", "§7Klicke zum Erstellen");
        inventory.setItem(31, createButton);

        ItemStack backButton = createItem(Material.ARROW, "§cZurück §8»", "§7 §8(§7Linksklick§8)");
        inventory.setItem(37, backButton);

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
        CreateCrateMode.startCreation(player.getUniqueId());
        player.openInventory(inventory);
    }

    public void refresh() {
        setupInventory();
    }
}
