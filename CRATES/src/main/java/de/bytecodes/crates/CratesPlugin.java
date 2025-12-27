package de.bytecodes.crates;

import de.bytecodes.crates.command.CratesCommand;
import de.bytecodes.crates.database.DatabaseAPI;
import de.bytecodes.crates.listener.CrateInteractionListener;
import de.bytecodes.crates.hologram.HologramManager;
import de.bytecodes.crates.util.CrateLocationManager;
import de.bytecodes.crates.util.VirtualKeyManager;
import de.bytecodes.crates.util.CrateOpener;
import org.bukkit.plugin.java.JavaPlugin;

public class CratesPlugin extends JavaPlugin {

    private static CratesPlugin instance;
    private DatabaseAPI databaseAPI;
    private HologramManager hologramManager;
    private CrateLocationManager crateLocationManager;
    private VirtualKeyManager virtualKeyManager;
    private CrateOpener crateOpener;
    private boolean maintenanceMode;

    @Override
    public void onEnable() {
        instance = this;
        maintenanceMode = false;

        databaseAPI = new DatabaseAPI(this);
        databaseAPI.initialize();

        hologramManager = new HologramManager(this);

        crateLocationManager = new CrateLocationManager(this);

        virtualKeyManager = new VirtualKeyManager(this);

        crateOpener = new CrateOpener(this);

        CratesCommand cratesCommand = new CratesCommand(this);
        getCommand("crates").setExecutor(cratesCommand);
        getCommand("crates").setTabCompleter(cratesCommand);

        getServer().getPluginManager().registerEvents(new CrateInteractionListener(this), this);
        getServer().getPluginManager().registerEvents(new de.bytecodes.crates.listener.AsyncChatListener(this), this);

        getLogger().info("§b§lCRATES §7Plugin aktiviert!");
    }

    @Override
    public void onDisable() {
        if (hologramManager != null) {
            hologramManager.removeAllHolograms();
        }
        if (databaseAPI != null) {
            databaseAPI.close();
        }
        getLogger().info("§b§lCRATES §7Plugin deaktiviert!");
    }

    public static CratesPlugin getInstance() {
        return instance;
    }

    public DatabaseAPI getDatabase() {
        return databaseAPI;
    }

    public HologramManager getHologramManager() {
        return hologramManager;
    }

    public CrateLocationManager getCrateLocationManager() {
        return crateLocationManager;
    }

    public VirtualKeyManager getVirtualKeyManager() {
        return virtualKeyManager;
    }

    public CrateOpener getCrateOpener() {
        return crateOpener;
    }

    public boolean isMaintenanceMode() {
        return maintenanceMode;
    }

    public void setMaintenanceMode(boolean maintenanceMode) {
        this.maintenanceMode = maintenanceMode;
    }
}
