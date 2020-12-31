package net.minestom.server.potion;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.EntityEffectPacket;
import net.minestom.server.network.packet.server.play.RemoveEntityEffectPacket;
import org.jetbrains.annotations.NotNull;

public class Potion {
    private final PotionEffect effect;
    private final byte amplifier;
    private final int duration;
    private final byte flags;

    public Potion(PotionEffect effect, byte amplifier, int duration) {
        this(effect, amplifier, duration, true, true, false);
    }

    public Potion(PotionEffect effect, byte amplifier, int duration, boolean particles) {
        this(effect, amplifier, duration, particles, true, false);
    }

    public Potion(PotionEffect effect, byte amplifier, int duration, boolean particles, boolean icon) {
        this(effect, amplifier, duration, particles, icon, false);
    }

    public Potion(PotionEffect effect, byte amplifier, int duration, boolean particles, boolean icon, boolean ambient) {
        this.effect = effect;
        this.amplifier = amplifier;
        this.duration = duration;
        byte flags = 0;
        if (ambient) flags = (byte)(flags | 0x01);
        if (particles) flags = (byte)(flags | 0x02);
        if (icon) flags = (byte)(flags | 0x04);
        this.flags = flags;
    }

    public PotionEffect getEffect() {
        return effect;
    }

    public byte getAmplifier() {
        return amplifier;
    }

    public int getDuration() {
        return duration;
    }

    public byte getFlags() {
        return flags;
    }

    public void sendAddPacket(@NotNull Entity entity) {
        EntityEffectPacket eep = new EntityEffectPacket();
        eep.entityId = entity.getEntityId();
        eep.potion = this;
        entity.sendPacketToViewersAndSelf(eep);
    }

    public void sendRemovePacket(@NotNull Entity entity) {
        RemoveEntityEffectPacket reep = new RemoveEntityEffectPacket();
        reep.entityId = entity.getEntityId();
        reep.effect = effect;
        entity.sendPacketToViewersAndSelf(reep);
    }
}
