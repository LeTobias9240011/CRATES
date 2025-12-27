package de.bytecodes.crates.model;

import java.util.ArrayList;
import java.util.List;

public class Crate {

    private int id;
    private String name;
    private String material;
    private String keyType;
    private List<Reward> rewards;

    public Crate(int id, String name, String material, String keyType) {
        this.id = id;
        this.name = name;
        this.material = material;
        this.keyType = keyType;
        this.rewards = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public String getKeyType() {
        return keyType;
    }

    public void setKeyType(String keyType) {
        this.keyType = keyType;
    }

    public List<Reward> getRewards() {
        return rewards;
    }

    public void setRewards(List<Reward> rewards) {
        this.rewards = rewards;
    }

    public void addReward(Reward reward) {
        this.rewards.add(reward);
    }

    public void removeReward(Reward reward) {
        this.rewards.remove(reward);
    }

    @Override
    public String toString() {
        return "Crate{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", material='" + material + '\'' +
                ", keyType='" + keyType + '\'' +
                ", rewards=" + rewards.size() +
                '}';
    }
}
