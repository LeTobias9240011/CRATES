package de.bytecodes.crates.model;

import org.bukkit.inventory.ItemStack;

public class Reward {

    private int id;
    private int crateId;
    private ItemStack item;
    private double chance;
    private int weight;
    private int limitPerPlayer;

    public Reward(int id, int crateId, ItemStack item, double chance, int weight, int limitPerPlayer) {
        this.id = id;
        this.crateId = crateId;
        this.item = item;
        this.chance = chance;
        this.weight = weight;
        this.limitPerPlayer = limitPerPlayer;
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

    public ItemStack getItem() {
        return item;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    public double getChance() {
        return chance;
    }

    public void setChance(double chance) {
        this.chance = chance;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getLimitPerPlayer() {
        return limitPerPlayer;
    }

    public void setLimitPerPlayer(int limitPerPlayer) {
        this.limitPerPlayer = limitPerPlayer;
    }

    @Override
    public String toString() {
        return "Reward{" +
                "id=" + id +
                ", crateId=" + crateId +
                ", chance=" + chance +
                ", weight=" + weight +
                ", limitPerPlayer=" + limitPerPlayer +
                '}';
    }
}
