package com.rique.zombieapocalypse;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Zombie;

/**
 * Zombies hunt animals for food. Gated entirely by config: the goal is always
 * installed so live toggles work, and every check runs through
 * {@link ZombieHunt}. The candidate class is LivingEntity so the include
 * overrides can cover non-Animal targets.
 */
public final class ZombieHuntGoal extends NearestAttackableTargetGoal<LivingEntity> {

    private final Zombie hunter;

    public ZombieHuntGoal(Zombie zombie) {
        super(zombie, LivingEntity.class, 10, true, true,
                candidate -> ZombieHunt.isHuntableTarget(zombie, candidate));
        this.hunter = zombie;
    }

    @Override
    public boolean canUse() {
        if (!(hunter.level() instanceof ServerLevel level)) {
            return false;
        }
        return ZombieHunt.canSeekTarget(hunter, level) && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return ZombieHunt.isEnabled() && super.canContinueToUse();
    }

    @Override
    protected double getFollowDistance() {
        return super.getFollowDistance()
                * Math.max(0.0, Config.COMMON.huntFollowDistanceFactor.get());
    }
}
