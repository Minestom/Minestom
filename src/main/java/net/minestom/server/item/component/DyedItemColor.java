package net.minestom.server.item.component;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.ByteBinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.IntBinaryTag;
import net.kyori.adventure.util.RGBLike;
import net.minestom.server.color.Color;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

public record DyedItemColor(@NotNull RGBLike color, boolean showInTooltip) {
    public static DyedItemColor LEATHER = new DyedItemColor(new Color(-6265536), true);

    public static final NetworkBuffer.Type<DyedItemColor> NETWORK_TYPE = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, DyedItemColor value) {
            buffer.write(Color.NETWORK_TYPE, value.color);
            buffer.write(NetworkBuffer.BOOLEAN, value.showInTooltip);
        }

        @Override
        public DyedItemColor read(@NotNull NetworkBuffer buffer) {
            return new DyedItemColor(buffer.read(Color.NETWORK_TYPE), buffer.read(NetworkBuffer.BOOLEAN));
        }
    };

    public static final BinaryTagSerializer<DyedItemColor> NBT_TYPE = new BinaryTagSerializer<>() {
        @Override
        public @NotNull BinaryTag write(@NotNull DyedItemColor value) {
            return CompoundBinaryTag.builder()
                    .putInt("color", Color.fromRGBLike(value.color).asRGB())
                    .putBoolean("show_in_tooltip", value.showInTooltip)
                    .build();
        }

        @Override
        public @NotNull DyedItemColor read(@NotNull BinaryTag tag) {
            if (tag instanceof CompoundBinaryTag compoundTag) {
                int color = compoundTag.getInt("color");
                // https://github.com/KyoriPowered/adventure/issues/1068
                boolean showInTooltip = !(compoundTag.get("show_in_tooltip") instanceof ByteBinaryTag showInTooltipTag) || showInTooltipTag.value() != 0;
                return new DyedItemColor(new Color(color), showInTooltip);
            } else if (tag instanceof IntBinaryTag intTag) {
                return new DyedItemColor(new Color(intTag.intValue()), true);
            }
            return new DyedItemColor(new Color(0), false);
        }
    };

    public DyedItemColor(int color) {
        this(color, true);
    }

    public DyedItemColor(@NotNull RGBLike color) {
        this(color, true);
    }

    public DyedItemColor(int color, boolean showInTooltip) {
        this(new Color(color), showInTooltip);
    }

    public @NotNull DyedItemColor withColor(@NotNull Color color) {
        return new DyedItemColor(color, showInTooltip);
    }

    public @NotNull DyedItemColor withTooltip(boolean showInTooltip) {
        return new DyedItemColor(color, showInTooltip);
    }

}
