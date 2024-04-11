package net.minestom.server.item.component;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.color.Color;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public record FireworkExplosion(
        @NotNull Shape shape,
        @NotNull List<Color> colors,
        @NotNull List<Color> fadeColors,
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

    public static final NetworkBuffer.Type<FireworkExplosion> NETWORK_TYPE = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, FireworkExplosion value) {
            buffer.writeEnum(Shape.class, value.shape);
            buffer.writeCollection(NetworkBuffer.COLOR, value.colors);
            buffer.writeCollection(NetworkBuffer.COLOR, value.fadeColors);
            buffer.write(NetworkBuffer.BOOLEAN, value.hasTrail);
            buffer.write(NetworkBuffer.BOOLEAN, value.hasTwinkle);
        }

        @Override
        public FireworkExplosion read(@NotNull NetworkBuffer buffer) {
            return new FireworkExplosion(
                    buffer.readEnum(Shape.class),
                    buffer.readCollection(NetworkBuffer.COLOR, Short.MAX_VALUE),
                    buffer.readCollection(NetworkBuffer.COLOR, Short.MAX_VALUE),
                    buffer.read(NetworkBuffer.BOOLEAN),
                    buffer.read(NetworkBuffer.BOOLEAN)
            );
        }
    };

    public static final BinaryTagSerializer<FireworkExplosion> NBT_TYPE = BinaryTagSerializer.COMPOUND.map(
            tag -> {
                Shape shape = Shape.valueOf(tag.getString("shape").toUpperCase(Locale.ROOT));
                List<Color> colors = new ArrayList<>();
                for (int color : tag.getIntArray("colors"))
                    colors.add(new Color(color));
                List<Color> fadeColors = new ArrayList<>();
                for (int fadeColor : tag.getIntArray("fadeColors"))
                    fadeColors.add(new Color(fadeColor));
                boolean hasTrail = tag.getBoolean("hasTrail");
                boolean hasTwinkle = tag.getBoolean("hasTwinkle");
                return new FireworkExplosion(shape, colors, fadeColors, hasTrail, hasTwinkle);
            },
            value -> {
                CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
                builder.putString("shape", value.shape.name().toLowerCase(Locale.ROOT));
                if (!value.colors.isEmpty()) {
                    int[] colors = new int[value.colors.size()];
                    for (int i = 0; i < value.colors.size(); i++)
                        colors[i] = value.colors.get(i).asRGB();
                    builder.putIntArray("colors", colors);
                }
                if (!value.fadeColors.isEmpty()) {
                    int[] fadeColors = new int[value.fadeColors.size()];
                    for (int i = 0; i < value.fadeColors.size(); i++)
                        fadeColors[i] = value.fadeColors.get(i).asRGB();
                    builder.putIntArray("fadeColors", fadeColors);
                }
                if (value.hasTrail) builder.putBoolean("hasTrail", value.hasTrail);
                if (value.hasTwinkle) builder.putBoolean("hasTwinkle", value.hasTwinkle);
                return builder.build();
            }
    );

    public FireworkExplosion {
        colors = List.copyOf(colors);
        fadeColors = List.copyOf(fadeColors);
    }
}
