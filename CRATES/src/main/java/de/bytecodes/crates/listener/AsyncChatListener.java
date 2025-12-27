package de.bytecodes.crates.listener;

import de.bytecodes.crates.CratesPlugin;
import de.bytecodes.crates.gui.CrateEditorGUI;
import de.bytecodes.crates.gui.CreateCrateGUI;
import de.bytecodes.crates.gui.KeyEditorGUI;
import de.bytecodes.crates.gui.RewardsGUI;
import de.bytecodes.crates.util.CreateCrateMode;
import de.bytecodes.crates.util.RewardManager;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AsyncChatListener implements Listener {

    private final CratesPlugin plugin;
    private static final Map<UUID, String> playerInputMode = new HashMap<>();

    public AsyncChatListener(CratesPlugin plugin) {
        this.plugin = plugin;
    }

    public static void setInputMode(UUID playerUUID, String mode) {
        playerInputMode.put(playerUUID, mode);
    }

    public static String getInputMode(UUID playerUUID) {
        return playerInputMode.get(playerUUID);
    }

    public static void removeInputMode(UUID playerUUID) {
        playerInputMode.remove(playerUUID);
    }

    @EventHandler
    public void onAsyncChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        String inputMode = getInputMode(player.getUniqueId());
        if (inputMode != null) {
            event.setCancelled(true);
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                handleInputMode(player, inputMode, message);
            }, 1L);
            return;
        }

        if (RewardManager.isAddingReward(player.getUniqueId())) {
            event.setCancelled(true);
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                if (message.equalsIgnoreCase("confirm")) {
                    ItemStack item = player.getInventory().getItemInMainHand();
                    if (item != null && item.getType() != Material.AIR) {
                        RewardManager.setRewardItem(player.getUniqueId(), item.clone());
                        player.sendMessage("§b§lCRATES §7» §a✓ Item gespeichert!");
                        player.sendMessage("§b§lCRATES §7» §aGib jetzt die Chance ein (0-100):");
                        setInputMode(player.getUniqueId(), "add_reward_chance");
                    } else {
                        player.sendMessage("§b§lCRATES §7» §cBitte halte ein Item in der Hand!");
                    }
                } else if (message.equalsIgnoreCase("cancel")) {
                    RewardManager.endAddingReward(player.getUniqueId());
                    player.sendMessage("§b§lCRATES §7» §cReward-Erstellung abgebrochen.");
                } else {
                    player.sendMessage("§b§lCRATES §7» §eSchreibe §a'confirm' §ezum Speichern oder §c'cancel' §ezum Abbrechen!");
                }
            }, 1L);
            return;
        }
    }

    private void handleInputMode(Player player, String mode, String message) {
        if (message.equalsIgnoreCase("cancel")) {
            removeInputMode(player.getUniqueId());
            player.sendMessage("§b§lCRATES §7» §cEingabe abgebrochen.");
            return;
        }

        String[] modeParts = mode.split(":", 2);
        String modeType = modeParts[0];
        String modeParam = modeParts.length > 1 ? modeParts[1] : null;

        switch (modeType) {
            case "create_crate_name":
                CreateCrateMode.setName(player.getUniqueId(), message);
                player.sendMessage("§b§lCRATES §7» §a✓ Name gesetzt: §e" + message);
                removeInputMode(player.getUniqueId());
                new CreateCrateGUI(plugin, player).open();
                break;

            case "create_crate_material":
                try {
                    Material.valueOf(message.toUpperCase());
                    CreateCrateMode.setMaterial(player.getUniqueId(), message.toUpperCase());
                    player.sendMessage("§b§lCRATES §7» §a✓ Material gesetzt: §e" + message.toUpperCase());
                    removeInputMode(player.getUniqueId());
                    new CreateCrateGUI(plugin, player).open();
                } catch (IllegalArgumentException e) {
                    player.sendMessage("§b§lCRATES §7» §cUngültiges Material! Versuche z.B. CHEST, ENDER_CHEST, BARREL");
                }
                break;

            case "create_crate_keytype":
                CreateCrateMode.setKeyType(player.getUniqueId(), message);
                player.sendMessage("§b§lCRATES §7» §a✓ Schlüssel-Typ gesetzt: §e" + message);
                removeInputMode(player.getUniqueId());
                new CreateCrateGUI(plugin, player).open();
                break;

            case "create_key":
                player.sendMessage("§b§lCRATES §7» §a✓ Schlüssel werden automatisch verwaltet!");
                removeInputMode(player.getUniqueId());
                new KeyEditorGUI(plugin, player).open();
                break;

            case "change_crate_name":
                if (modeParam != null) {
                    try {
                        plugin.getDatabase().updateCrateName(modeParam, message);
                        player.sendMessage("§b§lCRATES §7» §a✓ Crate-Name geändert zu §e" + message);
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                        removeInputMode(player.getUniqueId());
                        new CrateEditorGUI(plugin, player, message).open();
                    } catch (Exception e) {
                        player.sendMessage("§b§lCRATES §7» §cFehler: " + e.getMessage());
                        removeInputMode(player.getUniqueId());
                    }
                } else {
                    player.sendMessage("§b§lCRATES §7» §cFehler: Keine Crate ausgewählt.");
                    removeInputMode(player.getUniqueId());
                }
                break;

            case "change_key_type":
                if (modeParam != null) {
                    try {
                        plugin.getDatabase().updateCrateKeyType(modeParam, message);
                        player.sendMessage("§b§lCRATES §7» §a✓ Schlüssel-Typ geändert zu §e" + message);
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                        removeInputMode(player.getUniqueId());
                        new CrateEditorGUI(plugin, player, modeParam).open();
                    } catch (Exception e) {
                        player.sendMessage("§b§lCRATES §7» §cFehler: " + e.getMessage());
                        removeInputMode(player.getUniqueId());
                    }
                } else {
                    player.sendMessage("§b§lCRATES §7» §cFehler: Keine Crate ausgewählt.");
                    removeInputMode(player.getUniqueId());
                }
                break;

            case "change_crate_material":
                if (modeParam != null) {
                    try {
                        Material.valueOf(message.toUpperCase());
                        plugin.getDatabase().updateCrateMaterial(modeParam, message.toUpperCase());
                        player.sendMessage("§b§lCRATES §7» §a✓ Material geändert zu §e" + message.toUpperCase());
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                        removeInputMode(player.getUniqueId());
                        new CrateEditorGUI(plugin, player, modeParam).open();
                    } catch (IllegalArgumentException e) {
                        player.sendMessage("§b§lCRATES §7» §cUngültiges Material! Versuche z.B. CHEST, ENDER_CHEST, BARREL");
                    } catch (Exception e) {
                        player.sendMessage("§b§lCRATES §7» §cFehler: " + e.getMessage());
                        removeInputMode(player.getUniqueId());
                    }
                } else {
                    player.sendMessage("§b§lCRATES §7» §cFehler: Keine Crate ausgewählt.");
                    removeInputMode(player.getUniqueId());
                }
                break;

            case "add_reward_chance":
                try {
                    double chance = Double.parseDouble(message);
                    if (chance < 0 || chance > 100) {
                        player.sendMessage("§b§lCRATES §7» §cChance muss zwischen 0 und 100 liegen!");
                        return;
                    }

                    RewardManager.setRewardChance(player.getUniqueId(), chance);
                    Map<String, Object> rewardData = RewardManager.getRewardData(player.getUniqueId());

                    if (rewardData != null) {
                        String crateName = (String) rewardData.get("crateName");
                        ItemStack item = (ItemStack) rewardData.get("item");

                        if (item == null) {
                            player.sendMessage("§b§lCRATES §7» §cKein Item gespeichert!");
                            RewardManager.endAddingReward(player.getUniqueId());
                            removeInputMode(player.getUniqueId());
                            return;
                        }

                        String itemData = itemStackToBase64(item);

                        plugin.getDatabase().addReward(crateName, itemData, chance, 1, 0);
                        player.sendMessage("§b§lCRATES §7» §a✓ Reward mit §e" + chance + "% §aChance hinzugefügt!");
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

                        RewardManager.endAddingReward(player.getUniqueId());
                        removeInputMode(player.getUniqueId());

                        new RewardsGUI(plugin, player, crateName).open();
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage("§b§lCRATES §7» §cBitte gib eine gültige Zahl ein (0-100)!");
                } catch (java.sql.SQLException e) {
                    player.sendMessage("§b§lCRATES §7» §cDatenbankfehler: " + e.getMessage());
                    RewardManager.endAddingReward(player.getUniqueId());
                    removeInputMode(player.getUniqueId());
                }
                break;

            case "edit_reward_chance":
                if (modeParam != null) {
                    try {
                        int rewardId = Integer.parseInt(modeParam);
                        double newChance = Double.parseDouble(message);
                        if (newChance < 0 || newChance > 100) {
                            player.sendMessage("§b§lCRATES §7» §cChance muss zwischen 0 und 100 liegen!");
                            return;
                        }
                        plugin.getDatabase().updateRewardChance(rewardId, newChance);
                        player.sendMessage("§b§lCRATES §7» §a✓ Chance geändert zu §e" + newChance + "%");
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                        removeInputMode(player.getUniqueId());
                    } catch (NumberFormatException e) {
                        player.sendMessage("§b§lCRATES §7» §cBitte gib eine gültige Zahl ein!");
                    } catch (Exception e) {
                        player.sendMessage("§b§lCRATES §7» §cFehler: " + e.getMessage());
                        removeInputMode(player.getUniqueId());
                    }
                }
                break;

            case "edit_reward_limit":
                if (modeParam != null) {
                    try {
                        int rewardId = Integer.parseInt(modeParam);
                        int newLimit = Integer.parseInt(message);
                        if (newLimit < 0) {
                            player.sendMessage("§b§lCRATES §7» §cLimit muss 0 oder größer sein!");
                            return;
                        }
                        plugin.getDatabase().updateRewardLimit(rewardId, newLimit);
                        if (newLimit == 0) {
                            player.sendMessage("§b§lCRATES §7» §a✓ Limit entfernt (unbegrenzt)");
                        } else {
                            player.sendMessage("§b§lCRATES §7» §a✓ Limit geändert zu §e" + newLimit);
                        }
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                        removeInputMode(player.getUniqueId());
                    } catch (NumberFormatException e) {
                        player.sendMessage("§b§lCRATES §7» §cBitte gib eine gültige Zahl ein!");
                    } catch (Exception e) {
                        player.sendMessage("§b§lCRATES §7» §cFehler: " + e.getMessage());
                        removeInputMode(player.getUniqueId());
                    }
                }
                break;

            default:
                player.sendMessage("§b§lCRATES §7» §cUnbekannter Eingabemodus.");
                removeInputMode(player.getUniqueId());
                break;
        }
    }

    private String itemStackToBase64(ItemStack item) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeObject(item);
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            plugin.getLogger().warning("Fehler beim Konvertieren von ItemStack: " + e.getMessage());
            return "";
        }
    }

    private ItemStack itemStackFromBase64(String data) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack item = (ItemStack) dataInput.readObject();
            dataInput.close();
            return item;
        } catch (Exception e) {
            plugin.getLogger().warning("Fehler beim Konvertieren von Base64: " + e.getMessage());
            return null;
        }
    }
}
