package net.minestom.server.item.firework;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.color.Color;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record FireworkEffect(boolean flicker, boolean trail,
                             @NotNull FireworkEffectType type,
                             @NotNull List<Color> colors,
                             @NotNull List<Color> fadeColors) {
    public FireworkEffect {
        colors = List.copyOf(colors);
        fadeColors = List.copyOf(fadeColors);
    }

    /**
     * Retrieves a firework effect from the given {@code compound}.
     *
     * @param compound The NBT connection, which should be a fireworks effect.
     * @return A new created firework effect.
     */
    public static @NotNull FireworkEffect fromCompound(@NotNull CompoundBinaryTag compound) {
        List<Color> primaryColor = new ArrayList<>();
        List<Color> secondaryColor = new ArrayList<>();

        for (int rgb : compound.getIntArray("Colors"))
            primaryColor.add(new Color(rgb));
        for (int rgb : compound.getIntArray("FadeColors"))
            secondaryColor.add(new Color(rgb));

        boolean flicker = compound.getBoolean("Flicker");
        boolean trail = compound.getBoolean("Trail");
        FireworkEffectType type = FireworkEffectType.byId(compound.getByte("Type"));

        return new FireworkEffect(flicker, trail, type, primaryColor, secondaryColor);
    }

    /**
     * Retrieves the {@link FireworkEffect} as a {@link CompoundBinaryTag}.
     *
     * @return The firework effect as a nbt compound.
     */
    public @NotNull CompoundBinaryTag asCompound() {
        return CompoundBinaryTag.builder()
                .putBoolean("Flicker", flicker)
                .putBoolean("Trail", trail)
                .putByte("Type", (byte) type.getType())
                .putIntArray("Colors", colors.stream().mapToInt(Color::asRGB).toArray())
                .putIntArray("FadeColors", fadeColors.stream().mapToInt(Color::asRGB).toArray())
                .build();
    }
}
