package net.minestom.server.entity.metadata.other;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.metadata.MobMeta;
import org.jetbrains.annotations.NotNull;

public class EnderDragonMeta extends MobMeta {

    public EnderDragonMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    @NotNull
    public Phase getPhase() {
        return Phase.VALUES[super.metadata.getIndex((byte) 15, 0)];
    }

    public void setPhase(@NotNull Phase value) {
        super.metadata.getIndex((byte) 15, Metadata.VarInt(value.ordinal()));
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
