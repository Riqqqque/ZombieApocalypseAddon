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

    /**
     * Day used for scaling when strength rises in steps. With a step of 10, days 1-10 count as
     * day 0 and days 11-20 as day 10, so each rise lands the morning after every 10th day.
     */
    public static long steppedScalingDay(long currentDay, int stepDays) {
        if (stepDays <= 0 || currentDay <= 0) {
            return currentDay;
        }
        return ((currentDay - 1) / stepDays) * stepDays;
    }

    /**
     * First day the stepped strength rises again after {@code currentDay}, or -1 without steps.
     */
    public static long nextScalingStepDay(long currentDay, int stepDays) {
        if (stepDays <= 0) {
            return -1L;
        }
        return steppedScalingDay(Math.max(0L, currentDay), stepDays) + stepDays + 1L;
    }

    public static int scaledExtraZombies(int maxExtra, double scalingFactor) {
        if (maxExtra <= 0) {
            return 0;
        }
        return (int) Math.round(maxExtra * clampProbability(scalingFactor));
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
