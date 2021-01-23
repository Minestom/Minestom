package net.minestom.server.potion;

import net.minestom.server.MinecraftServer;
import net.minestom.server.utils.clone.PublicCloneable;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents a custom effect in {@link net.minestom.server.item.metadata.PotionMeta}.
 * <p>
 * This is an immutable class.
 */
public class CustomPotionEffect implements PublicCloneable<CustomPotionEffect> {

    private final byte id;
    private final byte amplifier;
    private final int duration;
    private final boolean ambient;
    private final boolean showParticles;
    private final boolean showIcon;

    public CustomPotionEffect(byte id, byte amplifier, int duration,
                              boolean ambient, boolean showParticles, boolean showIcon) {
        this.id = id;
        this.amplifier = amplifier;
        this.duration = duration;
        this.ambient = ambient;
        this.showParticles = showParticles;
        this.showIcon = showIcon;
    }

    public byte getId() {
        return id;
    }

    public byte getAmplifier() {
        return amplifier;
    }

    public int getDuration() {
        return duration;
    }

    public boolean isAmbient() {
        return ambient;
    }

    public boolean showParticles() {
        return showParticles;
    }

    public boolean showIcon() {
        return showIcon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomPotionEffect that = (CustomPotionEffect) o;
        return id == that.id && amplifier == that.amplifier && duration == that.duration && ambient == that.ambient && showParticles == that.showParticles && showIcon == that.showIcon;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, amplifier, duration, ambient, showParticles, showIcon);
    }

    @NotNull
    @Override
    public CustomPotionEffect clone() {
        try {
            return (CustomPotionEffect) super.clone();
        } catch (CloneNotSupportedException e) {
            MinecraftServer.getExceptionManager().handleException(e);
            throw new IllegalStateException("Weird thing happened");
        }
    }
}
