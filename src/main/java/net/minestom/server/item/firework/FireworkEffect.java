package net.minestom.server.item.firework;

import net.minestom.server.color.Color;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTIntArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    public static @NotNull FireworkEffect fromCompound(@NotNull NBTCompound compound) {
        List<Color> primaryColor = new ArrayList<>();
        List<Color> secondaryColor = new ArrayList<>();

        if (compound.get("Colors") instanceof NBTIntArray colors) {
            for (int rgb : colors) primaryColor.add(new Color(rgb));
        }
        if (compound.get("FadeColors") instanceof NBTIntArray fadeColors) {
            for (int rgb : fadeColors) secondaryColor.add(new Color(rgb));
        }

        boolean flicker = compound.containsKey("Flicker") && compound.getBoolean("Flicker");
        boolean trail = compound.containsKey("Trail") && compound.getBoolean("Trail");
        FireworkEffectType type = compound.containsKey("Type") ?
                FireworkEffectType.byId(compound.getAsByte("Type")) : FireworkEffectType.SMALL_BALL;

        return new FireworkEffect(flicker, trail, type, primaryColor, secondaryColor);
    }

    /**
     * Retrieves the {@link FireworkEffect} as a {@link NBTCompound}.
     *
     * @return The firework effect as a nbt compound.
     */
    public @NotNull NBTCompound asCompound() {
        return NBT.Compound(Map.of(
                "Flicker", NBT.Boolean(flicker),
                "Trail", NBT.Boolean(trail),
                "Type", NBT.Byte(type.getType()),
                "Colors", NBT.IntArray(colors.stream().mapToInt(Color::asRGB).toArray()),
                "FadeColors", NBT.IntArray(fadeColors.stream().mapToInt(Color::asRGB).toArray())));
    }
}
