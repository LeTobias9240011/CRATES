package de.bytecodes.crates.util;

import de.bytecodes.crates.CratesPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class CrateOpener {

    private final CratesPlugin plugin;
    private final Random random = new Random();
    private final Set<UUID> openingCrate = new HashSet<>();

    public CrateOpener(CratesPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Prüfe, ob ein Spieler gerade eine Crate öffnet
     */
    public boolean isOpening(Player player) {
        return openingCrate.contains(player.getUniqueId());
    }

    /**
     * Öffne eine Crate und gebe einen zufälligen Reward
     */
    public void openCrate(Player player, String crateName) {
        if (isOpening(player)) {
            player.sendMessage("§b§lCRATES §7» §cDu öffnest bereits eine Crate!");
            return;
        }

        try {
            int keyAmount = plugin.getVirtualKeyManager().getKeyAmount(player, crateName);
            if (keyAmount <= 0) {
                player.sendMessage("§b§lCRATES §7» §cDu hast keine Schlüssel für diese Crate!");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return;
            }

            List<Map<String, Object>> rewards = plugin.getDatabase().getRewards(crateName);
            if (rewards.isEmpty()) {
                player.sendMessage("§b§lCRATES §7» §cDiese Crate hat keine Rewards!");
                return;
            }

            openingCrate.add(player.getUniqueId());

            plugin.getVirtualKeyManager().removeKey(player, crateName, 1);

            Map<String, Object> selectedReward = selectReward(rewards, player);
            if (selectedReward == null) {
                player.sendMessage("§b§lCRATES §7» §cKein verfügbarer Reward gefunden!");
                openingCrate.remove(player.getUniqueId());
                plugin.getVirtualKeyManager().giveKey(player, crateName, 1);
                return;
            }

            openWithAnimation(player, crateName, selectedReward);

        } catch (Exception e) {
            plugin.getLogger().warning("Fehler beim Öffnen der Crate: " + e.getMessage());
            e.printStackTrace();
            player.sendMessage("§b§lCRATES §7» §cFehler beim Öffnen der Crate!");
            openingCrate.remove(player.getUniqueId());
        }
    }

    /**
     * Öffne die Crate mit einer Animation
     */
    private void openWithAnimation(Player player, String crateName, Map<String, Object> selectedReward) {
        Location loc = player.getLocation();

        new BukkitRunnable() {
            int ticks = 0;
            double angle = 0;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    openingCrate.remove(player.getUniqueId());
                    cancel();
                    return;
                }

                if (ticks >= totalTicks) {
                    giveReward(player, crateName, selectedReward);
                    openingCrate.remove(player.getUniqueId());
                    cancel();
                    return;
                }

                Location playerLoc = player.getLocation().add(0, 1, 0);

                if (ticks < 20) {
                    angle += 0.5;
                    double radius = 1.5;
                    for (int i = 0; i < 3; i++) {
                        double x = Math.cos(offsetAngle) * radius;
                        double z = Math.sin(offsetAngle) * radius;
                        Location particleLoc = playerLoc.clone().add(x, 0, z);
                        player.getWorld().spawnParticle(Particle.END_ROD, particleLoc, 1, 0, 0, 0, 0);
                    }
                    if (ticks % 4 == 0) {
                        player.playSound(playerLoc, Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5f, 0.5f + (ticks / 40f));
                    }
                }
                else if (ticks < 40) {
                    angle += 0.3;
                    double progress = (ticks - 20) / 20.0;
                    double radius = 1.5 - progress;
                    double y = progress * 2;
                    for (int i = 0; i < 2; i++) {
                        double offsetAngle = angle + (i * 3.14159);
                        double x = Math.cos(offsetAngle) * radius;
                        double z = Math.sin(offsetAngle) * radius;
                        Location particleLoc = playerLoc.clone().add(x, y, z);
                        player.getWorld().spawnParticle(Particle.SPELL_WITCH, particleLoc, 2, 0.1, 0.1, 0.1, 0);
                    }
                    if (ticks % 3 == 0) {
                        player.playSound(playerLoc, Sound.BLOCK_NOTE_BLOCK_PLING, 0.6f, (float)(0.8f + (progress * 0.8f)));
                    }
                }
                else {
                    double progress = (ticks - 40) / 20.0;
                    if (ticks == 40) {
                        player.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, playerLoc, 50, 0.5, 0.5, 0.5, 0.15);
                        player.getWorld().spawnParticle(Particle.FLAME, playerLoc, 30, 0.3, 0.3, 0.3, 0.1);
                        player.playSound(playerLoc, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0f, 1.0f);
                    }
                    if (ticks % 2 == 0) {
                        player.getWorld().spawnParticle(Particle.CRIT_MAGIC, playerLoc.clone().add(0, progress, 0), 10, 0.5, 0.2, 0.5, 0.1);
                    }
                    if (ticks % 5 == 0) {
                        player.playSound(playerLoc, Sound.BLOCK_NOTE_BLOCK_BELL, 0.8f, (float)(1.5f + (progress * 0.5f)));
                    }
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /**
     * Gebe dem Spieler den Reward
     */
    private void giveReward(Player player, String crateName, Map<String, Object> selectedReward) {
        try {
            String itemData = (String) selectedReward.get("item_data");
            ItemStack rewardItem = itemStackFromBase64(itemData);

            if (rewardItem != null) {
                if (player.getInventory().firstEmpty() == -1) {
                    player.getWorld().dropItemNaturally(player.getLocation(), rewardItem);
                    player.sendMessage("§b§lCRATES §7» §eDein Inventar war voll, der Reward wurde gedroppt!");
                } else {
                    player.getInventory().addItem(rewardItem);
                }

                Location rewardLoc = player.getLocation().add(0, 1, 0);
                player.playSound(rewardLoc, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                player.playSound(rewardLoc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8f, 1.2f);
                
                player.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, rewardLoc, 50, 1.0, 1.0, 1.0, 0.1);
                player.getWorld().spawnParticle(Particle.COMPOSTER, rewardLoc, 30, 0.5, 0.5, 0.5, 0.05);
                
                String itemName = rewardItem.hasItemMeta() && rewardItem.getItemMeta().hasDisplayName() 
                        ? rewardItem.getItemMeta().getDisplayName() 
                        : "§e" + rewardItem.getType().name().replace("_", " ");
                player.sendTitle("§a§l✓ GEWONNEN!", "§e" + rewardItem.getAmount() + "x " + itemName, 10, 60, 20);

                player.sendMessage("§b§lCRATES §7» §a✓ Du hast §e" + rewardItem.getAmount() + "x " + itemName + " §aerhalten!");

                try {
                    Map<String, Object> crateData = plugin.getDatabase().getCrate(crateName);
                    if (crateData != null) {
                        int crateId = (Integer) crateData.get("id");
                        int rewardId = (Integer) selectedReward.get("id");
                        plugin.getDatabase().addRewardHistory(player.getUniqueId().toString(), crateId, rewardId);
                    }
                } catch (Exception e) {
                }
            } else {
                player.sendMessage("§b§lCRATES §7» §cFehler beim Geben des Rewards!");
                plugin.getVirtualKeyManager().giveKey(player, crateName, 1);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Fehler beim Geben des Rewards: " + e.getMessage());
            player.sendMessage("§b§lCRATES §7» §cFehler beim Geben des Rewards!");
        }
    }

    /**
     * Wähle einen zufälligen Reward basierend auf Chance und Spieler-Limit
     */
    private Map<String, Object> selectReward(List<Map<String, Object>> rewards, Player player) {
        List<Map<String, Object>> availableRewards = new java.util.ArrayList<>();
        
        for (Map<String, Object> reward : rewards) {
            int limitPerPlayer = (Integer) reward.get("limit_per_player");
            if (limitPerPlayer > 0) {
                try {
                    int rewardId = (Integer) reward.get("id");
                    int count = plugin.getDatabase().getRewardCountForPlayer(player.getUniqueId().toString(), rewardId);
                    if (count >= limitPerPlayer) {
                    }
                } catch (Exception e) {
                }
            }
            availableRewards.add(reward);
        }

        if (availableRewards.isEmpty()) {
            return null;
        }

        double totalChance = 0;
        for (Map<String, Object> reward : availableRewards) {
            totalChance += (Double) reward.get("chance");
        }

        if (totalChance <= 0) {
            return availableRewards.get(0);
        }

        double randomValue = this.random.nextDouble() * totalChance;
        double currentChance = 0;

        for (Map<String, Object> reward : availableRewards) {
            currentChance += (Double) reward.get("chance");
            if (randomValue <= currentChance) {
                return reward;
            }
        }

        return availableRewards.get(0);
    }

    /**
     * Konvertiere Base64 String zu ItemStack
     */
    private ItemStack itemStackFromBase64(String data) {
        try {
            java.io.ByteArrayInputStream inputStream = new java.io.ByteArrayInputStream(
                    org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder.decodeLines(data)
            );
            org.bukkit.util.io.BukkitObjectInputStream dataInput = new org.bukkit.util.io.BukkitObjectInputStream(inputStream);
            ItemStack item = (ItemStack) dataInput.readObject();
            dataInput.close();
            return item;
        } catch (Exception e) {
            plugin.getLogger().warning("Fehler beim Konvertieren von Base64: " + e.getMessage());
            return null;
        }
    }
}
