package de.bytecodes.crates.gui;

import de.bytecodes.crates.CratesPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CrateEditorGUI {

    private final CratesPlugin plugin;
    private final Player player;
    private final String crateName;
    private final Inventory inventory;
    private static final Map<UUID, String> editingCrate = new HashMap<>();

    public CrateEditorGUI(CratesPlugin plugin, Player player, String crateName) {
        this.plugin = plugin;
        this.player = player;
        this.crateName = crateName;
        this.inventory = Bukkit.createInventory(null, 45, "§b§lCRATES §8» §e" + crateName);
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

        for (int i = 37; i < 44; i++) {
            inventory.setItem(i, border);
        }

        String keyType = "STANDARD";
        String material = "CHEST";
        int rewardCount = 0;
        try {
            Map<String, Object> crateData = plugin.getDatabase().getCrate(crateName);
            if (crateData != null) {
                keyType = (String) crateData.get("key_type");
                material = (String) crateData.get("material");
            }
            List<Map<String, Object>> rewards = plugin.getDatabase().getRewards(crateName);
            rewardCount = rewards.size();
        } catch (Exception e) {
            plugin.getLogger().warning("Fehler beim Laden der Crate-Daten: " + e.getMessage());
        }

        ItemStack nameButton = createItem(Material.NAME_TAG, "§b§lKisten Name", 
                "§7Aktuell: §e" + crateName, 
                "", 
                "§7Klicke zum Ändern");
        inventory.setItem(11, nameButton);

        ItemStack rewardsButton = createItem(Material.GOLD_INGOT, "§b§lRewards verwalten", 
                "§7Anzahl: §e" + rewardCount + " Rewards", 
                "", 
                "§7Klicke zum Bearbeiten");
        inventory.setItem(13, rewardsButton);

        ItemStack keyButton = createItem(Material.TRIPWIRE_HOOK, "§b§lKey Typ ändern", 
                "§7Aktuell: §e" + keyType, 
                "", 
                "§7Klicke zum Ändern");
        inventory.setItem(15, keyButton);

        Material mat = Material.CHEST;
        try {
            mat = Material.valueOf(material.toUpperCase());
        } catch (Exception e) {
        }
        ItemStack materialButton = createItem(mat, "§b§lMaterial ändern", 
                "§7Aktuell: §e" + material, 
                "", 
                "§7Klicke zum Ändern");
        inventory.setItem(20, materialButton);

        ItemStack setCrateButton = createItem(Material.ENDER_EYE, "§a§lCrate platzieren", 
                "§7Aktiviert den Platzierungs-Modus", 
                "", 
                "§7Klicke zum Aktivieren");
        inventory.setItem(22, setCrateButton);

        ItemStack deleteButton = createItem(Material.TNT, "§c§lCrate löschen", 
                "§7Löscht diese Crate komplett", 
                "", 
                "§cUnwiderruflich!");
        inventory.setItem(24, deleteButton);

        ItemStack backButton = createItem(Material.ARROW, "§cZurück §8»", "§7 §8(§7Linksklick§8)");
        inventory.setItem(36, backButton);

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
        editingCrate.put(player.getUniqueId(), crateName);
        player.openInventory(inventory);
    }

    public static String getEditingCrate(UUID playerUUID) {
        return editingCrate.get(playerUUID);
    }

    public static void removeEditingCrate(UUID playerUUID) {
        editingCrate.remove(playerUUID);
    }

    public String getCrateName() {
        return crateName;
    }
}
