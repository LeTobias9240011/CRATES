package de.bytecodes.crates.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CreateCrateMode {

    private static final Map<UUID, Map<String, Object>> playersCreating = new HashMap<>();

    /**
     * Starte die Crate-Erstellung für einen Spieler
     */
    public static void startCreation(UUID playerUUID) {
        if (!playersCreating.containsKey(playerUUID)) {
            Map<String, Object> data = new HashMap<>();
            data.put("name", null);
            data.put("material", null);
            data.put("keyType", null);
            playersCreating.put(playerUUID, data);
        }
    }

    /**
     * Prüfe, ob ein Spieler eine Crate erstellt
     */
    public static boolean isCreating(UUID playerUUID) {
        return playersCreating.containsKey(playerUUID);
    }

    /**
     * Setze den Namen der Crate
     */
    public static void setName(UUID playerUUID, String name) {
        Map<String, Object> data = playersCreating.get(playerUUID);
        if (data != null) {
            data.put("name", name);
        }
    }

    /**
     * Setze das Material der Crate
     */
    public static void setMaterial(UUID playerUUID, String material) {
        Map<String, Object> data = playersCreating.get(playerUUID);
        if (data != null) {
            data.put("material", material);
        }
    }

    /**
     * Setze den Key-Type der Crate
     */
    public static void setKeyType(UUID playerUUID, String keyType) {
        Map<String, Object> data = playersCreating.get(playerUUID);
        if (data != null) {
            data.put("keyType", keyType);
        }
    }

    /**
     * Hole die Daten der Crate
     */
    public static Map<String, Object> getData(UUID playerUUID) {
        return playersCreating.get(playerUUID);
    }

    /**
     * Beende die Crate-Erstellung
     */
    public static void endCreation(UUID playerUUID) {
        playersCreating.remove(playerUUID);
    }
}
