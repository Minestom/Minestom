package net.minestom.server.item.component;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.util.RGBLike;
import net.minestom.server.color.Color;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static net.minestom.server.network.NetworkBuffer.BOOLEAN;

public record FireworkExplosion(
        @NotNull Shape shape,
        @NotNull List<RGBLike> colors,
        @NotNull List<RGBLike> fadeColors,
        boolean hasTrail,
        boolean hasTwinkle
) {

    public enum Shape {
        SMALL_BALL,
        LARGE_BALL,
        STAR,
        CREEPER,
        BURST
    }

    public static final NetworkBuffer.Type<FireworkExplosion> NETWORK_TYPE = NetworkBufferTemplate.template(
            NetworkBuffer.Enum(Shape.class), FireworkExplosion::shape,
            Color.NETWORK_TYPE.list(Short.MAX_VALUE), FireworkExplosion::colors,
            Color.NETWORK_TYPE.list(Short.MAX_VALUE), FireworkExplosion::fadeColors,
            BOOLEAN, FireworkExplosion::hasTrail,
            BOOLEAN, FireworkExplosion::hasTwinkle,
            FireworkExplosion::new
    );

    public static final BinaryTagSerializer<FireworkExplosion> NBT_TYPE = BinaryTagSerializer.COMPOUND.map(
            tag -> {
                Shape shape = Shape.valueOf(tag.getString("shape").toUpperCase(Locale.ROOT));
                List<RGBLike> colors = new ArrayList<>();
                for (int color : tag.getIntArray("colors"))
                    colors.add(new Color(color));
                List<RGBLike> fadeColors = new ArrayList<>();
                for (int fadeColor : tag.getIntArray("fade_colors"))
                    fadeColors.add(new Color(fadeColor));
                boolean hasTrail = tag.getBoolean("has_trail");
                boolean hasTwinkle = tag.getBoolean("has_twinkle");
                return new FireworkExplosion(shape, colors, fadeColors, hasTrail, hasTwinkle);
            },
            value -> {
                CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
                builder.putString("shape", value.shape.name().toLowerCase(Locale.ROOT));
                if (!value.colors.isEmpty()) {
                    int[] colors = new int[value.colors.size()];
                    for (int i = 0; i < value.colors.size(); i++)
                        colors[i] = Color.fromRGBLike(value.colors.get(i)).asRGB();
                    builder.putIntArray("colors", colors);
                }
                if (!value.fadeColors.isEmpty()) {
                    int[] fadeColors = new int[value.fadeColors.size()];
                    for (int i = 0; i < value.fadeColors.size(); i++)
                        fadeColors[i] = Color.fromRGBLike(value.fadeColors.get(i)).asRGB();
                    builder.putIntArray("fade_colors", fadeColors);
                }
                if (value.hasTrail) builder.putBoolean("has_trail", value.hasTrail);
                if (value.hasTwinkle) builder.putBoolean("has_twinkle", value.hasTwinkle);
                return builder.build();
            }
    );

    public FireworkExplosion {
        colors = List.copyOf(colors);
        fadeColors = List.copyOf(fadeColors);
    }
}
