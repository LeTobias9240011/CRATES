package de.bytecodes.crates.gui;

import de.bytecodes.crates.CratesPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PreviewGUI {

    private final CratesPlugin plugin;
    private final Player player;
    private final String crateName;
    private final Inventory inventory;
    private int currentPage = 0;
    private static final Map<UUID, PreviewGUI> openPreviews = new HashMap<>();

    public PreviewGUI(CratesPlugin plugin, Player player, String crateName) {
        this.plugin = plugin;
        this.player = player;
        this.crateName = crateName;
        this.inventory = Bukkit.createInventory(null, 54, "§b§lCRATES §8» Preview");
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

        ItemStack backButton = createItem(Material.ARROW, "§cZurück §8»", "§7 §8(§7Linksklick§8)");
        inventory.setItem(45, backButton);

        try {
            List<Map<String, Object>> rewards = plugin.getDatabase().getRewards(crateName);
            int totalRewards = rewards.size();
            int slot = 10;

            for (int i = currentPage * itemsPerPage; i < Math.min((currentPage + 1) * itemsPerPage, rewards.size()); i++) {
                if (slot % 9 == 0 || slot % 9 == 8) {
                }

                Map<String, Object> reward = rewards.get(i);
                String itemData = (String) reward.get("item_data");
                double chance = (double) reward.get("chance");
                int limit = (int) reward.get("limit_per_player");

                ItemStack rewardItem = itemStackFromBase64(itemData);
                if (rewardItem == null || rewardItem.getType() == Material.AIR) {
                    rewardItem = new ItemStack(Material.BARRIER);
                }

                ItemMeta meta = rewardItem.getItemMeta();
                if (meta != null) {
                    List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
                    lore.add("");
                    lore.add("§7§m────────────────");
                    lore.add("§7Chance: §e" + String.format("%.2f", chance) + "%");
                    if (limit > 0) {
                        int playerReceived = plugin.getDatabase().getRewardCountForPlayer(player.getUniqueId().toString(), (int) reward.get("id"));
                        lore.add("§7Limit: §c" + playerReceived + "§7/§a" + limit);
                    } else {
                        lore.add("§7Limit: §aUnbegrenzt");
                    }
                    meta.setLore(lore);
                    rewardItem.setItemMeta(meta);
                }

                inventory.setItem(slot, rewardItem);
                slot++;
            }

            int totalPages = (int) Math.ceil((double) totalRewards / itemsPerPage);
            ItemStack playerHead = createPlayerHead(player, "§b§l" + crateName + " §8» §7Vorschau",
                    "§7Rewards: §e" + totalRewards,
                    "§7Seite: §e" + (currentPage + 1) + "§7/§e" + Math.max(1, totalPages),
                    "",
                    "§7Schlüssel: §a" + plugin.getVirtualKeyManager().getKeyAmount(player, crateName));
            inventory.setItem(4, playerHead);
        } catch (Exception e) {
            plugin.getLogger().warning("Fehler beim Laden der Belohnungen: " + e.getMessage());
        }

        ItemStack prevButton = createItem(Material.STONE_BUTTON, "§b§lVorherige Seite §8»", "§8(§7Linksklick§8)");
        inventory.setItem(47, prevButton);

        ItemStack nextButton = createItem(Material.STONE_BUTTON, "§b§lNächste Seite §8»", "§8(§7Linksklick§8)");
        inventory.setItem(49, nextButton);

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

    private ItemStack itemStackFromBase64(String data) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack item = (ItemStack) dataInput.readObject();
            dataInput.close();
            return item;
        } catch (Exception e) {
            plugin.getLogger().warning("Fehler beim Konvertieren von Reward-Item: " + e.getMessage());
            return null;
        }
    }

    public void open() {
        player.openInventory(inventory);
        openPreviews.put(player.getUniqueId(), this);
    }

    public void nextPage() {
        currentPage++;
        setupInventory();
        open();
    }

    public void previousPage() {
        if (currentPage > 0) {
            currentPage--;
            setupInventory();
            open();
        }
    }

    public static PreviewGUI getPreview(Player player) {
        return openPreviews.get(player.getUniqueId());
    }
}
