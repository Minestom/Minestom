package net.minestom.server.item.component;

import net.kyori.adventure.nbt.BinaryTag;
import net.minestom.server.item.Material;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record PotDecorations(
        @NotNull Material back,
        @NotNull Material left,
        @NotNull Material right,
        @NotNull Material front
) {
    public static final @NotNull Material DEFAULT_ITEM = Material.BRICK;
    public static final PotDecorations EMPTY = new PotDecorations(DEFAULT_ITEM, DEFAULT_ITEM, DEFAULT_ITEM, DEFAULT_ITEM);

    public static NetworkBuffer.Type<PotDecorations> NETWORK_TYPE = new NetworkBuffer.Type<PotDecorations>() {
        @Override public void write(@NotNull NetworkBuffer buffer, PotDecorations value) {
            Material.NETWORK_TYPE.list(4).map(PotDecorations::new, PotDecorations::asList).write(buffer, value);
        }

        @Override public PotDecorations read(@NotNull NetworkBuffer buffer) {
            return Material.NETWORK_TYPE.list(4).map(PotDecorations::new, PotDecorations::asList).read(buffer);
        }
    };
    public static BinaryTagSerializer<PotDecorations> NBT_TYPE = new BinaryTagSerializer<PotDecorations>() {
        @Override public @NotNull BinaryTag write(@NotNull PotDecorations value) {
            return Material.NBT_TYPE.list().map(PotDecorations::new, PotDecorations::asList).write(value);
        }

        @Override public @NotNull PotDecorations read(@NotNull BinaryTag tag) {
            return Material.NBT_TYPE.list().map(PotDecorations::new, PotDecorations::asList).read(tag);
        }
    };

    public PotDecorations(@NotNull List<Material> list) {
        this(getOrAir(list, 0), getOrAir(list, 1), getOrAir(list, 2), getOrAir(list, 3));
    }

    public @NotNull List<Material> asList() {
        return List.of(back, left, right, front);
    }

    private static @NotNull Material getOrAir(@NotNull List<Material> list, int index) {
        return index < list.size() ? list.get(index) : Material.BRICK;
    }
}
