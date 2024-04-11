package net.minestom.server.item.component;

import net.kyori.adventure.nbt.BinaryTag;
import net.minestom.server.item.Material;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

public record PotDecorations(
        @NotNull Material back,
        @NotNull Material left,
        @NotNull Material right,
        @NotNull Material front
) {
    public static final PotDecorations EMPTY = new PotDecorations(Material.AIR, Material.AIR, Material.AIR, Material.AIR);

    public static NetworkBuffer.Type<PotDecorations> NETWORK_TYPE = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, PotDecorations value) {

        }

        @Override
        public PotDecorations read(@NotNull NetworkBuffer buffer) {
            return null;
        }
    };

    public static BinaryTagSerializer<PotDecorations> NBT_TYPE = new BinaryTagSerializer<>() {
        @Override
        public @NotNull BinaryTag write(@NotNull PotDecorations value) {
            return null;
        }

        @Override
        public @NotNull PotDecorations read(@NotNull BinaryTag tag) {
            return null;
        }
    };
}
