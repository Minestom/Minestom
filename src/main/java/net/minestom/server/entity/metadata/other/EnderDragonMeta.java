package net.minestom.server.entity.metadata.other;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.MobMeta;

public class EnderDragonMeta extends MobMeta {
    public EnderDragonMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public Phase getPhase() {
        return Phase.VALUES[metadata.get(MetadataDef.EnderDragon.DRAGON_PHASE)];
    }

    public void setPhase(Phase value) {
        metadata.set(MetadataDef.EnderDragon.DRAGON_PHASE, value.ordinal());
    }

    public enum Phase {
        CIRCLING,
        STRAFING,
        FLYING_TO_THE_PORTAL,
        LANDING_ON_THE_PORTAL,
        TAKING_OFF_FROM_THE_PORTAL,
        BREATH_ATTACK,
        LOOKING_FOR_BREATH_ATTACK_PLAYER,
        ROAR,
        CHARGING_PLAYER,
        FLYING_TO_THE_PORTAL_TO_DIE,
        HOVERING_WITHOUT_AI;

        private final static Phase[] VALUES = values();
    }

}
