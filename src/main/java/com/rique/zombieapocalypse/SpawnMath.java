package com.rique.zombieapocalypse;

/**
 * Small deterministic math helpers used by spawning and scaling systems.
 */
public final class SpawnMath {

    private SpawnMath() {
    }

    public static double clampProbability(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(1.0, value));
    }

    public static boolean isSpawnDistanceImpossible(int minDistance, int horizontalRange) {
        long safeRange = Math.max(1, horizontalRange);
        long safeMinDistance = Math.max(0, minDistance);
        return safeMinDistance * safeMinDistance > 2L * safeRange * safeRange;
    }

    public static int maxHorizontalDistance(int horizontalRange) {
        int safeRange = Math.max(1, horizontalRange);
        return (int) Math.floor(Math.sqrt(2.0) * safeRange);
    }

    public static double computeLinearScale(long currentDay, int startDay, int maxDay) {
        if (currentDay < startDay) {
            return 0.0;
        }

        if (maxDay <= startDay) {
            return 1.0;
        }

        if (currentDay >= maxDay) {
            return 1.0;
        }

        return (double) (currentDay - startDay) / (double) (maxDay - startDay);
    }

    public static VariantWeights normalizeVariantWeights(double villagerChance, double huskChance, double drownedChance) {
        double villager = clampProbability(villagerChance);
        double husk = clampProbability(huskChance);
        double drowned = clampProbability(drownedChance);

        double sum = villager + husk + drowned;
        if (sum > 1.0) {
            villager /= sum;
            husk /= sum;
            drowned /= sum;
        }

        return new VariantWeights(villager, husk, drowned);
    }

    public record VariantWeights(double zombieVillagerChance, double huskChance, double drownedChance) {
        public double totalVariantChance() {
            return zombieVillagerChance + huskChance + drownedChance;
        }
    }
}
