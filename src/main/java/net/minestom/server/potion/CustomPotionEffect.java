package net.minestom.server.potion;

public record CustomPotionEffect(byte id, byte amplifier, int duration,
                                 boolean isAmbient, boolean showParticles,
                                 boolean showIcon) {
}
