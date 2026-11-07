package com.rique.zombieapocalypse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;

/**
 * Which zombie stands on which, for towers in standing pose.
 *
 * Sitting towers use vanilla riding, which the game saves and syncs on its own. Standing towers
 * are plain entities that the mod keeps on each other's heads, so the links live here. They are
 * runtime only: after a restart or chunk reload a standing zombie simply has no support any more,
 * and the tower code drops its tower tag.
 */
final class TowerLinks {

    private record Link(UUID support, ResourceKey<Level> dimension) {
    }

    private static final Map<UUID, Link> SUPPORT_OF = new HashMap<>();
    private static final Map<UUID, UUID> UPPER_OF = new HashMap<>();

    private TowerLinks() {
    }

    static void link(Zombie upper, Zombie support) {
        link(upper.getUUID(), support.getUUID(), upper.level().dimension());
    }

    /** Links by id. Each zombie has at most one support and at most one zombie standing on it. */
    static void link(UUID upperId, UUID supportId, ResourceKey<Level> dimension) {
        unlink(upperId);
        UUID previousUpper = UPPER_OF.get(supportId);
        if (previousUpper != null) {
            SUPPORT_OF.remove(previousUpper);
        }
        SUPPORT_OF.put(upperId, new Link(supportId, dimension));
        UPPER_OF.put(supportId, upperId);
    }

    static UUID supportIdOf(UUID upperId) {
        Link link = SUPPORT_OF.get(upperId);
        return link == null ? null : link.support();
    }

    static UUID upperIdOf(UUID supportId) {
        return UPPER_OF.get(supportId);
    }

    static void unlink(Zombie upper) {
        unlink(upper.getUUID());
    }

    static void unlink(UUID upperId) {
        Link link = SUPPORT_OF.remove(upperId);
        if (link != null && upperId.equals(UPPER_OF.get(link.support()))) {
            UPPER_OF.remove(link.support());
        }
    }

    static boolean isLinked(Zombie upper) {
        return SUPPORT_OF.containsKey(upper.getUUID());
    }

    static boolean isEmpty() {
        return SUPPORT_OF.isEmpty();
    }

    static void clear() {
        SUPPORT_OF.clear();
        UPPER_OF.clear();
    }

    /** The live zombie this one stands on, or null when the link is gone or the support is not usable. */
    static Zombie supportOf(Zombie upper) {
        Link link = SUPPORT_OF.get(upper.getUUID());
        if (link == null || !(upper.level() instanceof ServerLevel level) || !level.dimension().equals(link.dimension())) {
            return null;
        }
        return liveZombie(level, link.support());
    }

    /** The live zombie standing on this one, or null. */
    static Zombie upperOf(Zombie support) {
        UUID upperId = UPPER_OF.get(support.getUUID());
        if (upperId == null || !(support.level() instanceof ServerLevel level)) {
            return null;
        }
        Zombie upper = liveZombie(level, upperId);
        if (upper == null) {
            return null;
        }
        Link link = SUPPORT_OF.get(upperId);
        return link != null && link.support().equals(support.getUUID()) ? upper : null;
    }

    /** Standing zombies linked in this dimension, for the end-of-tick pass. */
    static List<UUID> uppersIn(ResourceKey<Level> dimension) {
        List<UUID> uppers = new ArrayList<>();
        SUPPORT_OF.forEach((upperId, link) -> {
            if (link.dimension().equals(dimension)) {
                uppers.add(upperId);
            }
        });
        return uppers;
    }

    private static Zombie liveZombie(ServerLevel level, UUID id) {
        Entity entity = level.getEntity(id);
        return entity instanceof Zombie zombie && zombie.isAlive() && !zombie.isRemoved() ? zombie : null;
    }
}
