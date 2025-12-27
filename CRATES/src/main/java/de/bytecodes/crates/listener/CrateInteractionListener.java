package de.bytecodes.crates.listener;

import de.bytecodes.crates.CratesPlugin;
import de.bytecodes.crates.gui.*;
import de.bytecodes.crates.util.SetCrateMode;
import de.bytecodes.crates.util.CreateCrateMode;
import de.bytecodes.crates.util.RewardManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.block.Action;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.Sound;

import java.util.Map;

public class CrateInteractionListener implements Listener {

    private final CratesPlugin plugin;

    public CrateInteractionListener(CratesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (plugin.isMaintenanceMode() && !player.hasPermission("crates.admin")) {
            event.setCancelled(true);
            player.sendMessage("§b§lCRATES §7» §cCrates sind derzeit im Wartungsmodus deaktiviert!");
            return;
        }

        if (SetCrateMode.isInMode(player.getUniqueId())) {
            return;
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (event.getClickedBlock() != null) {
                String crateName = plugin.getCrateLocationManager().getCrateAtLocation(event.getClickedBlock().getLocation());
                if (crateName != null) {
                    event.setCancelled(true);
                    new MainMenuGUI(plugin, player).open();
                }
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        if (!SetCrateMode.isInMode(player.getUniqueId())) {
            return;
        }

        try {
            plugin.getCrateLocationManager().registerLocation(event.getBlock().getLocation(), "MENU_BLOCK");

            plugin.getHologramManager().createHologram(
                    event.getBlock().getLocation().clone().add(0.5, 1.0, 0.5),
                    "§b§lCRATES",
                    "§7Klicken zum Öffnen"
            );

            player.sendMessage("§b§lCRATES §7» §aCrate-Block erfolgreich platziert!");
            player.sendMessage("§b§lCRATES §7» §7Spieler können jetzt auf diesen Block klicken!");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

            SetCrateMode.disableMode(player.getUniqueId());

        } catch (Exception e) {
            player.sendMessage("§b§lCRATES §7» §cFehler beim Platzieren des Crate-Blocks: " + e.getMessage());
            e.printStackTrace();
            SetCrateMode.disableMode(player.getUniqueId());
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getPlayer();
        CrateEditorGUI.removeEditingCrate(player.getUniqueId());
        RewardsGUI.removeRewardsGUI(player);
        MainMenuGUI.removeMenu(player);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        if (title.contains("CRATES") && title.contains("Übersicht")) {
            handleMainMenuClick(event, player);
        }
        else if (title.contains("CRATES") && title.contains("Preview")) {
            handlePreviewClick(event, player);
        }
        else if (title.equals("§b§lCRATES §8» Editor")) {
            handleEditorMenuClick(event, player);
        }
        else if (title.contains("CRATES") && title.contains("Keys")) {
            handleKeyEditorClick(event, player);
        }
        else if (title.equals("§b§lCRATES §8» Crates")) {
            handleCratesListClick(event, player);
        }
        else if (title.contains("CRATES") && title.contains("erstellen")) {
            handleCreateCrateClick(event, player);
        }
        else if (title.startsWith("§b§lCRATES §8» §e")) {
            handleCrateEditorClick(event, player, title);
        }
        else if (title.contains("CRATES") && title.contains("Rewards")) {
            handleRewardsClick(event, player, title);
        }
    }

    private void handleMainMenuClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        if (clicked.getType() == Material.BARRIER) {
            player.closeInventory();
        } else if (clicked.getType() == Material.GRAY_STAINED_GLASS_PANE) {
            return;
        } else if (clicked.getType() == Material.PLAYER_HEAD) {
            plugin.getVirtualKeyManager().showKeys(player);
        } else if (clicked.hasItemMeta() && clicked.getItemMeta().hasDisplayName()) {
            MainMenuGUI menu = MainMenuGUI.getMenu(player);
            if (menu != null) {
                String crateName = menu.getCrateNameAtSlot(event.getSlot());
                if (crateName != null) {
                    if (event.isLeftClick()) {
                        player.closeInventory();
                        plugin.getCrateOpener().openCrate(player, crateName);
                    } else if (event.isRightClick()) {
                        new PreviewGUI(plugin, player, crateName).open();
                    }
                }
            }
        }
    }

    private void handlePreviewClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        if (clicked.getType() == Material.BARRIER) {
            player.closeInventory();
        } else if (clicked.getType() == Material.ARROW) {
            new MainMenuGUI(plugin, player).open();
        } else if (clicked.getType() == Material.STONE_BUTTON) {
            if (clicked.getItemMeta() != null && clicked.getItemMeta().getDisplayName() != null) {
                String name = clicked.getItemMeta().getDisplayName();
                PreviewGUI gui = PreviewGUI.getPreview(player);
                if (gui != null) {
                    if (name.contains("Vorherige")) {
                        gui.previousPage();
                    } else if (name.contains("Nächste")) {
                        gui.nextPage();
                    }
                }
            }
        }
    }

    private void handleEditorMenuClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        if (clicked.getType() == Material.BARRIER) {
            player.closeInventory();
        } else if (clicked.getType() == Material.CHEST) {
            new CratesListGUI(plugin, player).open();
        } else if (clicked.getType() == Material.TRIPWIRE_HOOK) {
            new KeyEditorGUI(plugin, player).open();
        } else if (clicked.getType() == Material.EMERALD) {
            player.closeInventory();
            plugin.getHologramManager().removeAllHolograms();
            plugin.getCrateLocationManager().loadCrateLocations();
            player.sendMessage("§b§lCRATES §7» §a✓ Plugin erfolgreich neu geladen!");
        }
    }

    private void handleKeyEditorClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        if (clicked.getType() == Material.BARRIER) {
            player.closeInventory();
        } else if (clicked.getType() == Material.ARROW) {
            new EditorGUI(plugin, player).open();
        } else if (clicked.getType() == Material.LIME_DYE) {
            player.closeInventory();
            player.sendMessage("§b§lCRATES §7» §aSchlüssel werden automatisch pro Crate verwaltet!");
            player.sendMessage("§b§lCRATES §7» §aVerwende §e/crates givekey <spieler> <crate> [anzahl]");
        } else if (clicked.getType() == Material.RED_DYE) {
            player.sendMessage("§b§lCRATES §7» §aSchlüssel können mit §e/crates removekey §aentfernt werden");
        }
    }

    private void handleCratesListClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        if (clicked.getType() == Material.BARRIER) {
            player.closeInventory();
        } else if (clicked.getType() == Material.ARROW) {
            new EditorGUI(plugin, player).open();
        } else if (clicked.getType() == Material.LIME_DYE) {
            new CreateCrateGUI(plugin, player).open();
        } else if (clicked.getType() == Material.GRAY_STAINED_GLASS_PANE) {
            return;
        } else if (clicked.hasItemMeta() && clicked.getItemMeta().hasDisplayName()) {
            String displayName = clicked.getItemMeta().getDisplayName();
            String crateName = displayName.replaceAll("§[0-9a-fk-or]", "");

            if (event.isRightClick()) {
                try {
                    plugin.getDatabase().deleteCrateCompletely(crateName);
                    player.sendMessage("§b§lCRATES §7» §aCrate §e" + crateName + " §aerfolgreich gelöscht!");
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                    new CratesListGUI(plugin, player).open();
                } catch (Exception e) {
                    player.sendMessage("§b§lCRATES §7» §cFehler beim Löschen: " + e.getMessage());
                }
            } else {
                new CrateEditorGUI(plugin, player, crateName).open();
            }
        }
    }

    private void handleCreateCrateClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        if (clicked.getType() == Material.BARRIER) {
            CreateCrateMode.endCreation(player.getUniqueId());
            player.closeInventory();
        } else if (clicked.getType() == Material.ARROW) {
            CreateCrateMode.endCreation(player.getUniqueId());
            new CratesListGUI(plugin, player).open();
        } else if (clicked.getType() == Material.NAME_TAG) {
            player.closeInventory();
            AsyncChatListener.setInputMode(player.getUniqueId(), "create_crate_name");
            player.sendMessage("§b§lCRATES §7» §aGib den Namen der Crate in den Chat ein:");
        } else if (clicked.getType() == Material.CHEST) {
            player.closeInventory();
            AsyncChatListener.setInputMode(player.getUniqueId(), "create_crate_material");
            player.sendMessage("§b§lCRATES §7» §aGib das Material der Crate ein (z.B. CHEST, ENDER_CHEST):");
        } else if (clicked.getType() == Material.TRIPWIRE_HOOK) {
            player.closeInventory();
            AsyncChatListener.setInputMode(player.getUniqueId(), "create_crate_keytype");
            player.sendMessage("§b§lCRATES §7» §aGib den Schlüssel-Typ ein (z.B. STANDARD, VIP):");
        } else if (clicked.getType() == Material.LIME_DYE) {
            Map<String, Object> data = CreateCrateMode.getData(player.getUniqueId());
            if (data != null && data.get("name") != null && data.get("material") != null && data.get("keyType") != null) {
                try {
                    String name = (String) data.get("name");
                    String material = (String) data.get("material");
                    String keyType = (String) data.get("keyType");

                    try {
                        Material.valueOf(material.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        player.sendMessage("§b§lCRATES §7» §cUngültiges Material: " + material);
                        return;
                    }

                    plugin.getDatabase().addCrate(name, material.toUpperCase(), keyType);
                    player.sendMessage("§b§lCRATES §7» §aCrate §e" + name + " §aerfolgreich erstellt!");
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                    CreateCrateMode.endCreation(player.getUniqueId());
                    new CratesListGUI(plugin, player).open();
                } catch (Exception e) {
                    player.sendMessage("§b§lCRATES §7» §cFehler: " + e.getMessage());
                }
            } else {
                player.sendMessage("§b§lCRATES §7» §cBitte fülle alle Felder aus!");
            }
        }
    }

    private void handleCrateEditorClick(InventoryClickEvent event, Player player, String title) {
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        String crateName = title.replace("§b§lCRATES §8» §e", "");

        if (clicked.getType() == Material.BARRIER) {
            player.closeInventory();
        } else if (clicked.getType() == Material.ARROW) {
            new CratesListGUI(plugin, player).open();
        } else if (clicked.getType() == Material.NAME_TAG) {
            player.closeInventory();
            CrateEditorGUI.removeEditingCrate(player.getUniqueId());
            AsyncChatListener.setInputMode(player.getUniqueId(), "change_crate_name:" + crateName);
            player.sendMessage("§b§lCRATES §7» §aGib den neuen Namen ein:");
        } else if (clicked.getType() == Material.GOLD_INGOT) {
            new RewardsGUI(plugin, player, crateName).open();
        } else if (clicked.getType() == Material.TRIPWIRE_HOOK) {
            player.closeInventory();
            AsyncChatListener.setInputMode(player.getUniqueId(), "change_key_type:" + crateName);
            player.sendMessage("§b§lCRATES §7» §aGib den neuen Schlüssel-Typ ein:");
        } else if (clicked.getType() == Material.ENDER_EYE) {
            player.closeInventory();
            SetCrateMode.enableMode(player.getUniqueId());
            player.sendMessage("§b§lCRATES §7» §aSetCrate-Modus aktiviert!");
            player.sendMessage("§b§lCRATES §7» §aPlatziere einen Block, um die Crate §e" + crateName + " §azu setzen.");
        } else if (clicked.getType() == Material.TNT) {
            try {
                plugin.getDatabase().deleteCrateCompletely(crateName);
                player.sendMessage("§b§lCRATES §7» §aCrate §e" + crateName + " §aerfolgreich gelöscht!");
                player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.0f);
                new CratesListGUI(plugin, player).open();
            } catch (Exception e) {
                player.sendMessage("§b§lCRATES §7» §cFehler beim Löschen: " + e.getMessage());
            }
        } else if (clicked.hasItemMeta() && clicked.getItemMeta().hasDisplayName()) {
            String displayName = clicked.getItemMeta().getDisplayName();
            if (displayName.contains("Material")) {
                player.closeInventory();
                AsyncChatListener.setInputMode(player.getUniqueId(), "change_crate_material:" + crateName);
                player.sendMessage("§b§lCRATES §7» §aGib das neue Material ein (z.B. CHEST, ENDER_CHEST):");
            }
        }
    }

    private void handleRewardsClick(InventoryClickEvent event, Player player, String title) {
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        String crateName = title.replace("§b§lCRATES §8» ", "").replace(" §2Rewards", "");
        RewardsGUI rewardsGUI = RewardsGUI.getRewardsGUI(player);

        if (clicked.getType() == Material.BARRIER) {
            player.closeInventory();
        } else if (clicked.getType() == Material.ARROW) {
            new CrateEditorGUI(plugin, player, crateName).open();
        } else if (clicked.getType() == Material.LIME_DYE) {
            ItemStack handItem = player.getInventory().getItemInMainHand();
            if (handItem == null || handItem.getType() == Material.AIR) {
                player.sendMessage("§b§lCRATES §7» §cDu musst ein Item in der Hand halten!");
                return;
            }
            RewardManager.startAddingReward(player.getUniqueId(), crateName);
            player.closeInventory();
            player.sendMessage("§b§lCRATES §7» §aSchreibe die Chance in % in den Chat (z.B. §e10.5§a):");
        } else if (clicked.getType() == Material.RED_WOOL || clicked.getType() == Material.GREEN_WOOL) {
            RewardsGUI.toggleDeleteMode(player);
            if (rewardsGUI != null) {
                rewardsGUI.refresh();
            }
            if (RewardsGUI.isDeleteMode(player)) {
                player.sendMessage("§b§lCRATES §7» §cLösch-Modus aktiviert! Klicke auf Rewards zum Löschen.");
            } else {
                player.sendMessage("§b§lCRATES §7» §aLösch-Modus deaktiviert.");
            }
        } else if (clicked.getType() == Material.GRAY_STAINED_GLASS_PANE) {
            return;
        } else {
            int slot = event.getSlot();
            if (rewardsGUI != null) {
                Map<String, Object> reward = rewardsGUI.getRewardAtSlot(slot);
                if (reward != null) {
                    int rewardId = (int) reward.get("id");

                    if (RewardsGUI.isDeleteMode(player)) {
                        try {
                            plugin.getDatabase().removeReward(rewardId);
                            player.sendMessage("§b§lCRATES §7» §aReward wurde gelöscht!");
                            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 0.5f);
                            rewardsGUI.refresh();
                        } catch (Exception e) {
                            player.sendMessage("§b§lCRATES §7» §cFehler beim Löschen: " + e.getMessage());
                        }
                    } else if (event.isLeftClick()) {
                        AsyncChatListener.setInputMode(player.getUniqueId(), "edit_reward_chance:" + rewardId);
                        player.closeInventory();
                        player.sendMessage("§b§lCRATES §7» §aGib die neue Chance in % ein:");
                    } else if (event.isRightClick()) {
                        AsyncChatListener.setInputMode(player.getUniqueId(), "edit_reward_limit:" + rewardId);
                        player.closeInventory();
                        player.sendMessage("§b§lCRATES §7» §aGib das neue Limit ein (0 = unbegrenzt):");
                    }
                }
            }
        }
    }
}
