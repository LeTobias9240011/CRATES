package de.bytecodes.crates.gui;

import de.bytecodes.crates.CratesPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MainMenuGUI {

    private final CratesPlugin plugin;
    private final Player player;
    private final Inventory inventory;
    private final List<String> crateNames = new ArrayList<>();
    private static final Map<UUID, MainMenuGUI> openMenus = new HashMap<>();

    public MainMenuGUI(CratesPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(null, 54, "§b§lCRATES §8» Übersicht");
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

        int totalKeys = 0;
        try {
            Map<String, Integer> allKeys = plugin.getVirtualKeyManager().getAllKeys(player);
            for (int keys : allKeys.values()) {
                totalKeys += keys;
            }
        } catch (Exception e) {
        }
        
        ItemStack playerHead = createPlayerHead(player, "§b§lDein Profil",
                "§7Spieler: §f" + player.getName(),
                "",
                "§7Gesamt-Schlüssel: §e" + totalKeys,
                "",
                "§7Klicke auf eine Crate um",
                "§7sie zu öffnen oder die",
                "§7Belohnungen zu sehen!");
        inventory.setItem(4, playerHead);

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
                crateNames.add(name);

                Material material;
                try {
                    material = Material.valueOf(materialStr);
                } catch (Exception e) {
                    material = Material.CHEST;
                }

                int playerKeys = plugin.getVirtualKeyManager().getKeyAmount(player, name);
                int rewardCount = plugin.getDatabase().getRewards(name).size();

                ItemStack crateItem = createItem(material, colorize(name),
                        "§7Schlüssel-Typ: §f" + keyType,
                        "§7Belohnungen: §f" + rewardCount,
                        "",
                        "§7Deine Schlüssel: §a" + playerKeys,
                        "",
                        "§aLinksklick: §7Crate öffnen",
                        "§eRechtsklick: §7Vorschau");
                inventory.setItem(slot, crateItem);
                slot++;
            }

            if (crates.isEmpty()) {
                ItemStack noItem = createItem(Material.BARRIER, "§cKeine Crates vorhanden",
                        "§7Es wurden noch keine",
                        "§7Crates erstellt!");
                inventory.setItem(22, noItem);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Fehler beim Laden der Crates: " + e.getMessage());
        }

        ItemStack closeButton = createItem(Material.BARRIER, "§4Menu Schließen!", "§7 §8(§7Linksklick§8)");
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

    private ItemStack createPlayerHead(Player player, String name, String... lore) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(player);
            meta.setDisplayName(name);
            if (lore.length > 0) {
                List<String> loreList = new ArrayList<>();
                for (String line : lore) {
                    loreList.add(line);
                }
                meta.setLore(loreList);
            }
            head.setItemMeta(meta);
        }
        return head;
    }

    private String colorize(String text) {
        return text.replaceAll("&([0-9a-fk-or])", "§$1");
    }

    public void open() {
        player.openInventory(inventory);
        openMenus.put(player.getUniqueId(), this);
    }

    public String getCrateNameAtSlot(int slot) {
        int index = 0;
        int currentSlot = 10;
        for (String name : crateNames) {
            if (currentSlot % 9 == 0 || currentSlot % 9 == 8) {
                currentSlot++;
            }
            if (currentSlot == slot) {
                return name;
            }
            currentSlot++;
            index++;
            if (currentSlot >= 45) break;
        }
        return null;
    }

    public static MainMenuGUI getMenu(Player player) {
        return openMenus.get(player.getUniqueId());
    }

    public static void removeMenu(Player player) {
        openMenus.remove(player.getUniqueId());
    }
}
