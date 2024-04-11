package net.minestom.server.item.component;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.IntBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum ItemRarity {
    COMMON,
    UNCOMMON,
    RARE,
    EPIC;

    private static final ItemRarity[] VALUES = values();

    static final NetworkBuffer.Type<ItemRarity> NETWORK_TYPE = new NetworkBuffer.Type<ItemRarity>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, ItemRarity value) {
            buffer.writeEnum(ItemRarity.class, value);
        }

        @Override
        public ItemRarity read(@NotNull NetworkBuffer buffer) {
            return buffer.readEnum(ItemRarity.class);
        }
    };

    static final BinaryTagSerializer<ItemRarity> NBT_TYPE = new BinaryTagSerializer<>() {
        @Override
        public @NotNull BinaryTag write(@NotNull ItemRarity value) {
            return IntBinaryTag.intBinaryTag(value.ordinal());
        }

        @Override
        public @NotNull ItemRarity read(@NotNull BinaryTag tag) {
            return switch (tag) {
                case IntBinaryTag intBinaryTag -> VALUES[intBinaryTag.value()];
                case StringBinaryTag stringBinaryTag -> valueOf(stringBinaryTag.value().toUpperCase(Locale.ROOT));
                default -> COMMON;
            };
        }
    };
}
