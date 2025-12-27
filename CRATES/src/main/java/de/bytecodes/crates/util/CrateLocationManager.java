package de.bytecodes.crates.util;

import de.bytecodes.crates.CratesPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CrateLocationManager {

    private final CratesPlugin plugin;
    private final Map<String, String> locationToCrate = new HashMap<>();

    public CrateLocationManager(CratesPlugin plugin) {
        this.plugin = plugin;
        this.loadCrateLocations();
    }

    /**
     * Lade alle Crate-Locations aus der Datenbank
     */
    public void loadCrateLocations() {
        locationToCrate.clear();
        try {
            List<Map<String, Object>> crates = plugin.getDatabase().getAllCrates();

            for (Map<String, Object> crate : crates) {
                int crateId = (Integer) crate.get("id");
                String crateName = (String) crate.get("name");

                List<Map<String, Object>> locations = plugin.getDatabase().getCrateLocations(crateId);
                for (Map<String, Object> loc : locations) {
                    String worldName = (String) loc.get("world");
                    double x = (Double) loc.get("x");
                    double y = (Double) loc.get("y");
                    double z = (Double) loc.get("z");

                    if (worldName == null) {
                        continue;
                    }

                    if (Bukkit.getWorld(worldName) == null) {
                        continue;
                    }

                    Location location = new Location(
                            Bukkit.getWorld(worldName),
                            x,
                            y,
                            z
                    );

                    String key = locationToKey(location);
                    if (key != null) {
                        locationToCrate.put(key, crateName);
                        
                        plugin.getHologramManager().createHologram(
                                location.clone().add(0.5, 1.0, 0.5),
                                "§b§l" + crateName,
                                "§7Rechtsklick zum Öffnen",
                                "§7Linksklick für Vorschau"
                        );
                    }
                }
            }

            plugin.getLogger().info("§a" + locationToCrate.size() + " Crate-Locations geladen");
        } catch (Exception e) {
            plugin.getLogger().warning("Fehler beim Laden der Crate-Locations: " + e.getMessage());
        }
    }

    /**
     * Registriere eine Crate-Location
     */
    public void registerLocation(Location location, String crateName) {
        String key = locationToKey(location);
        if (key != null) {
            locationToCrate.put(key, crateName);
        }
    }

    /**
     * Entferne eine Crate-Location
     */
    public void unregisterLocation(Location location) {
        String key = locationToKey(location);
        if (key != null) {
            locationToCrate.remove(key);
            
            plugin.getHologramManager().removeHologram(location.clone().add(0.5, 1.5, 0.5));
            
            try {
                plugin.getDatabase().removeCrateLocationByCoords(
                        location.getWorld().getName(),
                        location.getBlockX(),
                        location.getBlockY(),
                        location.getBlockZ()
                );
            } catch (Exception e) {
                plugin.getLogger().warning("Fehler beim Entfernen der Crate-Location: " + e.getMessage());
            }
        }
    }

    /**
     * Hole den Crate-Namen an einer Location
     */
    public String getCrateAtLocation(Location location) {
        String key = locationToKey(location);
        return locationToCrate.get(key);
    }

    /**
     * Prüfe, ob eine Location eine Crate hat
     */
    public boolean isCrateLocation(Location location) {
        String key = locationToKey(location);
        return locationToCrate.containsKey(key);
    }

    /**
     * Hole alle registrierten Locations
     */
    public Map<String, String> getAllLocations() {
        return new HashMap<>(locationToCrate);
    }

    /**
     * Konvertiere Location zu String-Key
     */
    private String locationToKey(Location location) {
        if (location == null || location.getWorld() == null) {
            return null;
        }
        return location.getWorld().getName() + ":" + 
               location.getBlockX() + ":" + 
               location.getBlockY() + ":" + 
               location.getBlockZ();
    }
}
