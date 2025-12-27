package de.bytecodes.crates.gui;

import de.bytecodes.crates.CratesPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RewardsGUI {

    private final CratesPlugin plugin;
    private final Player player;
    private final String crateName;
    private final Inventory inventory;
    private final List<Map<String, Object>> rewardsList = new ArrayList<>();
    private static final Map<UUID, RewardsGUI> openRewardsGUIs = new HashMap<>();
    private static final Map<UUID, Boolean> deleteMode = new HashMap<>();

    public RewardsGUI(CratesPlugin plugin, Player player, String crateName) {
        this.plugin = plugin;
        this.player = player;
        this.crateName = crateName;
        this.inventory = Bukkit.createInventory(null, 54, "§b§lCRATES §8» " + crateName + " §2Rewards");
        setupInventory();
    }

    private void setupInventory() {
        inventory.clear();
        rewardsList.clear();

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

        try {
            List<Map<String, Object>> rewards = plugin.getDatabase().getRewards(crateName);
            rewardsList.addAll(rewards);
            int slot = 10;

            boolean isDeleteMode = deleteMode.getOrDefault(player.getUniqueId(), false);

            for (Map<String, Object> reward : rewards) {
                if (slot % 9 == 0 || slot % 9 == 8) {
                }

                String itemData = (String) reward.get("item_data");
                double chance = (Double) reward.get("chance");
                int limit = (int) reward.get("limit_per_player");
                int rewardId = (int) reward.get("id");

                ItemStack rewardItem = itemStackFromBase64(itemData);
                if (rewardItem == null || rewardItem.getType() == Material.AIR) {
                    rewardItem = new ItemStack(Material.BARRIER);
                }

                ItemMeta meta = rewardItem.getItemMeta();
                if (meta != null) {
                    List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
                    lore.add("");
                    lore.add("§7§m────────────────");
                    lore.add("§7ID: §f" + rewardId);
                    lore.add("§7Chance: §e" + String.format("%.2f", chance) + "%");
                    if (limit > 0) {
                        lore.add("§7Limit: §c" + limit + "x §7pro Spieler");
                    } else {
                        lore.add("§7Limit: §aUnbegrenzt");
                    }
                    lore.add("");
                    if (isDeleteMode) {
                        lore.add("§c§lKlicken zum Löschen!");
                    } else {
                        lore.add("§7Linksklick: §eChance bearbeiten");
                        lore.add("§7Rechtsklick: §eLimit bearbeiten");
                    }
                    meta.setLore(lore);
                    rewardItem.setItemMeta(meta);
                }

                inventory.setItem(slot, rewardItem);
                slot++;
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Fehler beim Laden der Belohnungen: " + e.getMessage());
        }

        ItemStack backButton = createItem(Material.ARROW, "§cZurück §8»", "§7 §8(§7Linksklick§8)");
        inventory.setItem(45, backButton);

        ItemStack addButton = createItem(Material.LIME_DYE, "§a§lReward hinzufügen",
                "§7Halte ein Item in der Hand",
                "§7und klicke hier!");
        inventory.setItem(48, addButton);

        boolean isDeleteMode = deleteMode.getOrDefault(player.getUniqueId(), false);
        if (isDeleteMode) {
            ItemStack deleteButton = createItem(Material.RED_WOOL, "§c§lLösch-Modus: AN",
                    "§7Klicke auf Rewards um sie zu löschen!",
                    "",
                    "§eKlicken zum Deaktivieren");
            inventory.setItem(50, deleteButton);
        } else {
            ItemStack deleteButton = createItem(Material.GREEN_WOOL, "§a§lLösch-Modus: AUS",
                    "§7Aktiviere um Rewards zu löschen",
                    "",
                    "§eKlicken zum Aktivieren");
            inventory.setItem(50, deleteButton);
        }

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
        openRewardsGUIs.put(player.getUniqueId(), this);
    }

    public void refresh() {
        setupInventory();
    }

    public String getCrateName() {
        return crateName;
    }

    public List<Map<String, Object>> getRewardsList() {
        return rewardsList;
    }

    public Map<String, Object> getRewardAtSlot(int slot) {
        int index = 0;
        int currentSlot = 10;
        for (Map<String, Object> reward : rewardsList) {
            if (currentSlot % 9 == 0 || currentSlot % 9 == 8) {
                currentSlot++;
            }
            if (currentSlot == slot) {
                return reward;
            }
            currentSlot++;
            index++;
            if (currentSlot >= 45) break;
        }
        return null;
    }

    public static RewardsGUI getRewardsGUI(Player player) {
        return openRewardsGUIs.get(player.getUniqueId());
    }

    public static void removeRewardsGUI(Player player) {
        openRewardsGUIs.remove(player.getUniqueId());
        deleteMode.remove(player.getUniqueId());
    }

    public static void toggleDeleteMode(Player player) {
        boolean current = deleteMode.getOrDefault(player.getUniqueId(), false);
        deleteMode.put(player.getUniqueId(), !current);
    }

    public static boolean isDeleteMode(Player player) {
        return deleteMode.getOrDefault(player.getUniqueId(), false);
    }
}
