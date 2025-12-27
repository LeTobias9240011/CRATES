package de.bytecodes.crates.database;

import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.sql.*;
import java.util.*;

public class DatabaseAPI {

    private final JavaPlugin plugin;
    private Connection connection;
    private final File databaseFile;

    public DatabaseAPI(JavaPlugin plugin) {
        this.plugin = plugin;
        this.databaseFile = new File(plugin.getDataFolder(), "crates.db");
    }

    public void initialize() {
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }

            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());

            createTables();
            plugin.getLogger().info("§aDatenbank erfolgreich initialisiert!");
        } catch (Exception e) {
            plugin.getLogger().severe("§cFehler beim Initialisieren der Datenbank: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS crates (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT UNIQUE NOT NULL," +
                    "material TEXT NOT NULL," +
                    "key_type TEXT NOT NULL," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS keys (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "player_uuid TEXT NOT NULL," +
                    "crate_id INTEGER NOT NULL," +
                    "amount INTEGER DEFAULT 1," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY(crate_id) REFERENCES crates(id)," +
                    "UNIQUE(player_uuid, crate_id)" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS rewards (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "crate_id INTEGER NOT NULL," +
                    "item_data TEXT NOT NULL," +
                    "chance REAL DEFAULT 1.0," +
                    "weight INTEGER DEFAULT 1," +
                    "limit_per_player INTEGER DEFAULT -1," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY(crate_id) REFERENCES crates(id)" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS crate_locations (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "crate_id INTEGER NOT NULL," +
                    "world TEXT NOT NULL," +
                    "x DOUBLE NOT NULL," +
                    "y DOUBLE NOT NULL," +
                    "z DOUBLE NOT NULL," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY(crate_id) REFERENCES crates(id)" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS reward_history (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "player_uuid TEXT NOT NULL," +
                    "crate_id INTEGER NOT NULL," +
                    "reward_id INTEGER NOT NULL," +
                    "received_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY(crate_id) REFERENCES crates(id)," +
                    "FOREIGN KEY(reward_id) REFERENCES rewards(id)" +
                    ")");
        }
    }

    public void addCrate(String name, String material, String keyType) throws SQLException {
        String sql = "INSERT INTO crates (name, material, key_type) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, material);
            pstmt.setString(3, keyType);
            pstmt.executeUpdate();
        }
    }

    public void removeCrate(String name) throws SQLException {
        String sql = "DELETE FROM crates WHERE name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
        }
    }

    public Map<String, Object> getCrate(String name) throws SQLException {
        String sql = "SELECT * FROM crates WHERE name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Map<String, Object> crate = new HashMap<>();
                crate.put("id", rs.getInt("id"));
                crate.put("name", rs.getString("name"));
                crate.put("material", rs.getString("material"));
                crate.put("key_type", rs.getString("key_type"));
                return crate;
            }
        }
        return null;
    }

    public List<Map<String, Object>> getAllCrates() throws SQLException {
        List<Map<String, Object>> crates = new ArrayList<>();
        String sql = "SELECT * FROM crates";
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Map<String, Object> crate = new HashMap<>();
                crate.put("id", rs.getInt("id"));
                crate.put("name", rs.getString("name"));
                crate.put("material", rs.getString("material"));
                crate.put("key_type", rs.getString("key_type"));
                crates.add(crate);
            }
        }
        return crates;
    }

    public void giveKey(String playerUUID, String crateName, int amount) throws SQLException {
        Map<String, Object> crate = getCrate(crateName);
        if (crate == null) return;
        int crateId = (Integer) crate.get("id");

        String selectSql = "SELECT amount FROM keys WHERE player_uuid = ? AND crate_id = ?";
        try (PreparedStatement select = connection.prepareStatement(selectSql)) {
            select.setString(1, playerUUID);
            select.setInt(2, crateId);
            ResultSet rs = select.executeQuery();

            if (rs.next()) {
                int current = rs.getInt("amount");
                String updateSql = "UPDATE keys SET amount = ? WHERE player_uuid = ? AND crate_id = ?";
                try (PreparedStatement update = connection.prepareStatement(updateSql)) {
                    update.setInt(1, current + amount);
                    update.setString(2, playerUUID);
                    update.setInt(3, crateId);
                    update.executeUpdate();
                }
            } else {
                String insertSql = "INSERT INTO keys (player_uuid, crate_id, amount) VALUES (?, ?, ?)";
                try (PreparedStatement insert = connection.prepareStatement(insertSql)) {
                    insert.setString(1, playerUUID);
                    insert.setInt(2, crateId);
                    insert.setInt(3, amount);
                    insert.executeUpdate();
                }
            }
        }
    }

    public void removeKey(String playerUUID, String crateName, int amount) throws SQLException {
        Map<String, Object> crate = getCrate(crateName);
        if (crate == null) return;

        String sql = "UPDATE keys SET amount = amount - ? WHERE player_uuid = ? AND crate_id = ? AND amount > 0";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, amount);
            pstmt.setString(2, playerUUID);
            pstmt.setInt(3, (Integer) crate.get("id"));
            pstmt.executeUpdate();
        }
    }

    public int getKeyAmount(String playerUUID, String crateName) throws SQLException {
        Map<String, Object> crate = getCrate(crateName);
        if (crate == null) return 0;

        String sql = "SELECT amount FROM keys WHERE player_uuid = ? AND crate_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerUUID);
            pstmt.setInt(2, (Integer) crate.get("id"));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("amount");
            }
        }
        return 0;
    }

    public void addReward(String crateName, String itemData, double chance, int weight, int limitPerPlayer) throws SQLException {
        Map<String, Object> crate = getCrate(crateName);
        if (crate == null) return;

        String sql = "INSERT INTO rewards (crate_id, item_data, chance, weight, limit_per_player) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, (Integer) crate.get("id"));
            pstmt.setString(2, itemData);
            pstmt.setDouble(3, chance);
            pstmt.setInt(4, weight);
            pstmt.setInt(5, limitPerPlayer);
            pstmt.executeUpdate();
        }
    }

    public List<Map<String, Object>> getRewards(String crateName) throws SQLException {
        Map<String, Object> crate = getCrate(crateName);
        if (crate == null) return new ArrayList<>();

        List<Map<String, Object>> rewards = new ArrayList<>();
        String sql = "SELECT * FROM rewards WHERE crate_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, (Integer) crate.get("id"));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> reward = new HashMap<>();
                reward.put("id", rs.getInt("id"));
                reward.put("item_data", rs.getString("item_data"));
                reward.put("chance", rs.getDouble("chance"));
                reward.put("weight", rs.getInt("weight"));
                reward.put("limit_per_player", rs.getInt("limit_per_player"));
                rewards.add(reward);
            }
        }
        return rewards;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("§cFehler beim Schließen der Datenbank: " + e.getMessage());
        }
    }

    public void addCrateLocation(int crateId, String world, double x, double y, double z) throws SQLException {
        String sql = "INSERT INTO crate_locations (crate_id, world, x, y, z) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, crateId);
            pstmt.setString(2, world);
            pstmt.setDouble(3, x);
            pstmt.setDouble(4, y);
            pstmt.setDouble(5, z);
            pstmt.executeUpdate();
        }
    }

    public List<Map<String, Object>> getCrateLocations(int crateId) throws SQLException {
        List<Map<String, Object>> locations = new ArrayList<>();
        String sql = "SELECT * FROM crate_locations WHERE crate_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, crateId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> location = new HashMap<>();
                location.put("id", rs.getInt("id"));
                location.put("world", rs.getString("world"));
                location.put("x", rs.getDouble("x"));
                location.put("y", rs.getDouble("y"));
                location.put("z", rs.getDouble("z"));
                locations.add(location);
            }
        }
        return locations;
    }

    public Connection getConnection() {
        return connection;
    }

    /**
     * Entferne eine Crate-Location aus der Datenbank
     */
    public void removeCrateLocation(int locationId) throws SQLException {
        String sql = "DELETE FROM crate_locations WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, locationId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Entferne eine Crate-Location anhand der Koordinaten
     */
    public void removeCrateLocationByCoords(String world, double x, double y, double z) throws SQLException {
        String sql = "DELETE FROM crate_locations WHERE world = ? AND x = ? AND y = ? AND z = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, world);
            pstmt.setDouble(2, x);
            pstmt.setDouble(3, y);
            pstmt.setDouble(4, z);
            pstmt.executeUpdate();
        }
    }

    /**
     * Hole alle Crate-Locations
     */
    public List<Map<String, Object>> getAllCrateLocations() throws SQLException {
        List<Map<String, Object>> locations = new ArrayList<>();
        String sql = "SELECT cl.*, c.name as crate_name FROM crate_locations cl JOIN crates c ON cl.crate_id = c.id";
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Map<String, Object> location = new HashMap<>();
                location.put("id", rs.getInt("id"));
                location.put("crate_id", rs.getInt("crate_id"));
                location.put("crate_name", rs.getString("crate_name"));
                location.put("world", rs.getString("world"));
                location.put("x", rs.getDouble("x"));
                location.put("y", rs.getDouble("y"));
                location.put("z", rs.getDouble("z"));
                locations.add(location);
            }
        }
        return locations;
    }

    /**
     * Entferne eine Belohnung aus der Datenbank
     */
    public void removeReward(int rewardId) throws SQLException {
        String sql = "DELETE FROM rewards WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, rewardId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Aktualisiere eine Belohnung
     */
    public void updateReward(int rewardId, double chance, int weight, int limitPerPlayer) throws SQLException {
        String sql = "UPDATE rewards SET chance = ?, weight = ?, limit_per_player = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDouble(1, chance);
            pstmt.setInt(2, weight);
            pstmt.setInt(3, limitPerPlayer);
            pstmt.setInt(4, rewardId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Aktualisiere nur die Chance einer Belohnung
     */
    public void updateRewardChance(int rewardId, double chance) throws SQLException {
        String sql = "UPDATE rewards SET chance = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDouble(1, chance);
            pstmt.setInt(2, rewardId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Aktualisiere nur das Limit einer Belohnung
     */
    public void updateRewardLimit(int rewardId, int limitPerPlayer) throws SQLException {
        String sql = "UPDATE rewards SET limit_per_player = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, limitPerPlayer);
            pstmt.setInt(2, rewardId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Aktualisiere den Namen einer Crate
     */
    public void updateCrateName(String oldName, String newName) throws SQLException {
        String sql = "UPDATE crates SET name = ? WHERE name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newName);
            pstmt.setString(2, oldName);
            pstmt.executeUpdate();
        }
    }

    /**
     * Aktualisiere den Key-Type einer Crate
     */
    public void updateCrateKeyType(String crateName, String newKeyType) throws SQLException {
        String sql = "UPDATE crates SET key_type = ? WHERE name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newKeyType);
            pstmt.setString(2, crateName);
            pstmt.executeUpdate();
        }
    }

    /**
     * Aktualisiere das Material einer Crate
     */
    public void updateCrateMaterial(String crateName, String newMaterial) throws SQLException {
        String sql = "UPDATE crates SET material = ? WHERE name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newMaterial);
            pstmt.setString(2, crateName);
            pstmt.executeUpdate();
        }
    }

    /**
     * Hole alle Schlüssel eines Spielers
     */
    public Map<String, Integer> getAllPlayerKeys(String playerUUID) throws SQLException {
        Map<String, Integer> keys = new HashMap<>();
        String sql = "SELECT c.name, k.amount FROM keys k JOIN crates c ON k.crate_id = c.id WHERE k.player_uuid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerUUID);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                keys.put(rs.getString("name"), rs.getInt("amount"));
            }
        }
        return keys;
    }

    /**
     * Lösche eine Crate komplett (mit allen zugehörigen Daten)
     */
    public void deleteCrateCompletely(String crateName) throws SQLException {
        Map<String, Object> crate = getCrate(crateName);
        if (crate == null) return;
        int crateId = (Integer) crate.get("id");

        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM reward_history WHERE crate_id = " + crateId);
            stmt.execute("DELETE FROM rewards WHERE crate_id = " + crateId);
            stmt.execute("DELETE FROM keys WHERE crate_id = " + crateId);
            stmt.execute("DELETE FROM crate_locations WHERE crate_id = " + crateId);
            stmt.execute("DELETE FROM crates WHERE id = " + crateId);
        }
    }

    /**
     * Füge einen Eintrag zur Reward-History hinzu
     */
    public void addRewardHistory(String playerUUID, int crateId, int rewardId) throws SQLException {
        String sql = "INSERT INTO reward_history (player_uuid, crate_id, reward_id) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerUUID);
            pstmt.setInt(2, crateId);
            pstmt.setInt(3, rewardId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Zähle wie oft ein Spieler eine bestimmte Belohnung erhalten hat
     */
    public int getRewardCountForPlayer(String playerUUID, int rewardId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reward_history WHERE player_uuid = ? AND reward_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerUUID);
            pstmt.setInt(2, rewardId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
}
