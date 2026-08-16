package com.rique.zombieapocalypse;

import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.phys.Vec3;

final class TowerRiderPosition {

    private TowerRiderPosition() {
    }

    static Vec3 calculate(Zombie support, Zombie rider) {
        return new Vec3(
                support.getX(),
                support.getY() + support.getPassengersRidingOffset() + rider.getMyRidingOffset(),
                support.getZ());
    }
}
