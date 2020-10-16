package net.minestom.server.potion;

/**
 * Represents a custom effect in {@link net.minestom.server.item.metadata.PotionMeta}.
 */
public class CustomPotionEffect {

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
}
