package de.bytecodes.crates.util;

import de.bytecodes.crates.CratesPlugin;
import de.bytecodes.crates.model.Reward;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RewardManager {

    private final CratesPlugin plugin;
    private static final Map<UUID, Map<String, Object>> playersAddingReward = new HashMap<>();

    public RewardManager(CratesPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Starte das Hinzufügen eines Rewards für einen Spieler
     */
    public static void startAddingReward(UUID playerUUID, String crateName) {
        Map<String, Object> data = new HashMap<>();
        data.put("crateName", crateName);
        data.put("item", null);
        data.put("chance", null);
        playersAddingReward.put(playerUUID, data);
    }

    /**
     * Prüfe, ob ein Spieler einen Reward hinzufügt
     */
    public static boolean isAddingReward(UUID playerUUID) {
        return playersAddingReward.containsKey(playerUUID);
    }

    /**
     * Setze das Item für den Reward
     */
    public static void setRewardItem(UUID playerUUID, ItemStack item) {
        Map<String, Object> data = playersAddingReward.get(playerUUID);
        if (data != null) {
            data.put("item", item);
        }
    }

    /**
     * Setze die Chance für den Reward
     */
    public static void setRewardChance(UUID playerUUID, double chance) {
        Map<String, Object> data = playersAddingReward.get(playerUUID);
        if (data != null) {
            data.put("chance", chance);
        }
    }

    /**
     * Hole die Daten des Rewards
     */
    public static Map<String, Object> getRewardData(UUID playerUUID) {
        return playersAddingReward.get(playerUUID);
    }

    /**
     * Beende das Hinzufügen eines Rewards
     */
    public static void endAddingReward(UUID playerUUID) {
        playersAddingReward.remove(playerUUID);
    }
}
