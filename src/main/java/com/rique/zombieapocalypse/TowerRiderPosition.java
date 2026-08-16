package com.rique.zombieapocalypse;

import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.phys.Vec3;

final class TowerRiderPosition {

    private TowerRiderPosition() {
    }

    static Vec3 calculate(Zombie support, Zombie rider) {
        return support.getPassengerRidingPosition(rider)
                .subtract(rider.getVehicleAttachmentPoint(support));
    }
}
