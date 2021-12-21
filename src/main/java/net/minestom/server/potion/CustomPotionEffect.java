package net.minestom.server.potion;

/**
 * Represents a custom effect in {@link net.minestom.server.item.metadata.PotionMeta}.
 */
public record CustomPotionEffect(byte id, byte amplifier, int duration,
                                 boolean isAmbient, boolean showParticles,
                                 boolean showIcon) {
}
