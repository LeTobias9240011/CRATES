package de.bytecodes.crates.util;

import de.bytecodes.crates.CratesPlugin;
import de.bytecodes.crates.model.Crate;
import de.bytecodes.crates.model.CrateLocation;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.sql.SQLException;
import java.util.*;

public class CrateManager {

    private final CratesPlugin plugin;
    private final Map<Location, String> crateLocations = new HashMap<>();

    public CrateManager(CratesPlugin plugin) {
        this.plugin = plugin;
        loadCrateLocations();
    }

    /**
     * Lade alle Crate-Standorte aus der Datenbank
     */
    private void loadCrateLocations() {
        try {
            plugin.getLogger().info("§aCrate-Standorte aus Datenbank geladen");
        } catch (Exception e) {
            plugin.getLogger().warning("Fehler beim Laden der Crate-Standorte: " + e.getMessage());
        }
    }

    /**
     * Registriere einen Crate-Standort
     */
    public void registerCrateLocation(Location location, String crateName) throws SQLException {
        crateLocations.put(location, crateName);
        plugin.getLogger().info("§aCrate-Standort registriert: " + crateName + " bei " + location);
    }

    /**
     * Hebe die Registrierung eines Crate-Standorts auf
     */
    public void unregisterCrateLocation(Location location) throws SQLException {
        String crateName = crateLocations.remove(location);
        if (crateName != null) {
            plugin.getLogger().info("§aCrate-Standort-Registrierung aufgehoben: " + crateName);
        }
    }

    /**
     * Hole den Crate-Namen an einem Standort
     */
    public String getCrateAtLocation(Location location) {
        return crateLocations.get(location);
    }

    /**
     * Prüfe, ob ein Standort eine Crate hat
     */
    public boolean isCrateLocation(Location location) {
        return crateLocations.containsKey(location);
    }

    /**
     * Hole alle Crate-Standorte
     */
    public Map<Location, String> getAllCrateLocations() {
        return new HashMap<>(crateLocations);
    }

    /**
     * Erstelle eine neue Crate
     */
    public void createCrate(String name, Material material, String keyType) throws SQLException {
        plugin.getDatabase().addCrate(name, material.toString(), keyType);
        plugin.getLogger().info("§aCrate erstellt: " + name);
    }

    /**
     * Lösche eine Crate
     */
    public void deleteCrate(String name) throws SQLException {
        plugin.getDatabase().removeCrate(name);
        plugin.getLogger().info("§aCrate gelöscht: " + name);
    }

    /**
     * Hole eine Crate nach Name
     */
    public Crate getCrate(String name) throws SQLException {
        Map<String, Object> data = plugin.getDatabase().getCrate(name);
        if (data == null) {
            return null;
        }
        return new Crate(
                (Integer) data.get("id"),
                (String) data.get("name"),
                (String) data.get("material"),
                (String) data.get("key_type")
        );
    }

    /**
     * Hole alle Crates
     */
    public List<Crate> getAllCrates() throws SQLException {
        List<Crate> crates = new ArrayList<>();
        List<Map<String, Object>> data = plugin.getDatabase().getAllCrates();
        for (Map<String, Object> crateData : data) {
            crates.add(new Crate(
                    (Integer) crateData.get("id"),
                    (String) crateData.get("name"),
                    (String) crateData.get("material"),
                    (String) crateData.get("key_type")
            ));
        }
        return crates;
    }
}
