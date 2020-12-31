package net.minestom.server.potion;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.EntityEffectPacket;
import net.minestom.server.network.packet.server.play.RemoveEntityEffectPacket;
import org.jetbrains.annotations.NotNull;

public class Potion {
    public PotionEffect effect;
    public byte amplifier;
    public int duration;
    public boolean ambient;
    public boolean particles;
    public boolean icon;

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
        this.particles = particles;
        this.icon = icon;
        this.ambient = ambient;
    }

    public byte getFlags() {
        byte computed = 0x00;
        if (ambient) computed = (byte)(computed | 0x01);
        if (particles) computed = (byte)(computed | 0x02);
        if (icon) computed = (byte)(computed | 0x04);
        return computed;
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
