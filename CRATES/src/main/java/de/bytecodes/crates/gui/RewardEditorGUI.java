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

public class RewardEditorGUI {

    private final CratesPlugin plugin;
    private final Player player;
    private final String crateName;
    private final int rewardId;
    private final Inventory inventory;

    public RewardEditorGUI(CratesPlugin plugin, Player player, String crateName, int rewardId) {
        this.plugin = plugin;
        this.player = player;
        this.crateName = crateName;
        this.rewardId = rewardId;
        this.inventory = Bukkit.createInventory(null, 54, "§b§lCRATES §8» " + crateName + " §2Rewards");
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

        for (int i = 46; i < 52; i++) {
            inventory.setItem(i, border);
        }

        ItemStack chanceButton = createItem(Material.OAK_SIGN, "§b§lChance setzen");
        inventory.setItem(11, chanceButton);

        ItemStack itemButton = createItem(Material.LIME_STAINED_GLASS_PANE, "§a§lItem hinzufügen");
        inventory.setItem(13, itemButton);

        ItemStack weightButton = createItem(Material.ANVIL, "§b§lGewicht setzen");
        inventory.setItem(15, weightButton);

        ItemStack limitButton = createItem(Material.STRUCTURE_VOID, "§b§lLimit setzen");
        inventory.setItem(22, limitButton);

        ItemStack removeButton = createItem(Material.RED_DYE, "§4§lReward löschen");
        inventory.setItem(24, removeButton);

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
