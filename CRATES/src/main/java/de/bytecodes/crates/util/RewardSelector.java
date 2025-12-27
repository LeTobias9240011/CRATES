package de.bytecodes.crates.util;

import de.bytecodes.crates.model.Reward;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RewardSelector {

    private final Random random = new Random();

    /**
     * Wähle eine zufällige Belohnung aus einer Liste basierend auf Gewicht und Chance
     */
    public Reward selectReward(List<Reward> rewards) {
        if (rewards == null || rewards.isEmpty()) {
            return null;
        }

        int totalWeight = 0;
        for (Reward reward : rewards) {
            totalWeight += reward.getWeight();
        }

        if (totalWeight <= 0) {
            return null;
        }

        int selectedWeight = random.nextInt(totalWeight);
        int currentWeight = 0;

        for (Reward reward : rewards) {
            currentWeight += reward.getWeight();
            if (selectedWeight < currentWeight) {
                if (random.nextDouble() <= reward.getChance()) {
                    return reward;
                }
            }
        }

        return rewards.get(0);
    }

    /**
     * Hole alle Belohnungen, die die Chance-Anforderung erfüllen
     */
    public List<Reward> getValidRewards(List<Reward> rewards) {
        List<Reward> validRewards = new ArrayList<>();
        for (Reward reward : rewards) {
            if (random.nextDouble() <= reward.getChance()) {
                validRewards.add(reward);
            }
        }
        return validRewards;
    }

    /**
     * Berechne die Wahrscheinlichkeit, eine bestimmte Belohnung zu erhalten
     */
    public double getRewardProbability(Reward reward, List<Reward> allRewards) {
        int totalWeight = 0;
        for (Reward r : allRewards) {
            totalWeight += r.getWeight();
        }

        if (totalWeight <= 0) {
            return 0;
        }

        double weightProbability = (double) reward.getWeight() / totalWeight;
        return weightProbability * reward.getChance();
    }
}
