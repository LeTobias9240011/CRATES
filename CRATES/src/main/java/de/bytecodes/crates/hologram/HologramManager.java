package de.bytecodes.crates.hologram;

import de.bytecodes.crates.CratesPlugin;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HologramManager {

    private final CratesPlugin plugin;
    private final Map<Location, List<ArmorStand>> holograms = new HashMap<>();

    public HologramManager(CratesPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Erstellt ein Hologramm am angegebenen Ort
     * @param location Der Ort, an dem das Hologramm platziert werden soll
     * @param lines Die anzuzeigenden Textzeilen
     */
    public void createHologram(Location location, String... lines) {
        if (location == null || location.getWorld() == null) {
            return;
        }

        List<ArmorStand> stands = new ArrayList<>();
        double yOffset = 0;

        for (String line : lines) {
            Location hologramLocation = location.clone().add(0, yOffset, 0);
            ArmorStand stand = (ArmorStand) location.getWorld().spawnEntity(hologramLocation, EntityType.ARMOR_STAND);

            stand.setCustomName(line);
            stand.setCustomNameVisible(true);
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setSmall(true);

            stands.add(stand);
        }

        holograms.put(location, stands);
    }

    /**
     * Entfernt ein Hologramm am angegebenen Ort
     * @param location Der Ort des Hologramms
     */
    public void removeHologram(Location location) {
        List<ArmorStand> stands = holograms.remove(location);
        if (stands != null) {
            for (ArmorStand stand : stands) {
                stand.remove();
            }
        }
    }

    /**
     * Aktualisiert den Text eines Hologramms
     * @param location Der Ort des Hologramms
     * @param lines Die neuen Textzeilen
     */
    public void updateHologram(Location location, String... lines) {
        removeHologram(location);
        createHologram(location, lines);
    }

    /**
     * Entfernt alle Hologramme
     */
    public void removeAllHolograms() {
        for (List<ArmorStand> stands : holograms.values()) {
            for (ArmorStand stand : stands) {
                stand.remove();
            }
        }
        holograms.clear();
    }
}
