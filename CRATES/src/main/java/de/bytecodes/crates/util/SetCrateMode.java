package de.bytecodes.crates.util;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SetCrateMode {

    private static final Set<UUID> playersInMode = new HashSet<>();

    /**
     * Aktiviere den SetCrate-Modus für einen Spieler
     */
    public static void enableMode(UUID playerUUID) {
        playersInMode.add(playerUUID);
    }

    /**
     * Deaktiviere den SetCrate-Modus für einen Spieler
     */
    public static void disableMode(UUID playerUUID) {
        playersInMode.remove(playerUUID);
    }

    /**
     * Prüfe, ob ein Spieler im SetCrate-Modus ist
     */
    public static boolean isInMode(UUID playerUUID) {
        return playersInMode.contains(playerUUID);
    }
}
