package de.bytecodes.crates.model;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class CrateLocation {

    private int id;
    private int crateId;
    private String world;
    private double x;
    private double y;
    private double z;

    public CrateLocation(int id, int crateId, String world, double x, double y, double z) {
        this.id = id;
        this.crateId = crateId;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCrateId() {
        return crateId;
    }

    public void setCrateId(int crateId) {
        this.crateId = crateId;
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public Location toLocation() {
        World w = Bukkit.getWorld(world);
        if (w == null) {
            return null;
        }
        return new Location(w, x, y, z);
    }

    @Override
    public String toString() {
        return "CrateLocation{" +
                "id=" + id +
                ", crateId=" + crateId +
                ", world='" + world + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
