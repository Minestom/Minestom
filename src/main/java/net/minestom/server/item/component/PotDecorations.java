package net.minestom.server.item.component;

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

    public static final NetworkBuffer.Type<PotDecorations> NETWORK_TYPE = Material.NETWORK_TYPE.list(4).transform(PotDecorations::new, PotDecorations::asList);
    public static BinaryTagSerializer<PotDecorations> NBT_TYPE = Material.NBT_TYPE.list().map(PotDecorations::new, PotDecorations::asList);

    public PotDecorations(@NotNull List<Material> list) {
        this(getOrAir(list, 0), getOrAir(list, 1), getOrAir(list, 2), getOrAir(list, 3));
    }

    public PotDecorations(@NotNull Material material) {
        this(material, material, material, material);
    }

    public @NotNull List<Material> asList() {
        return List.of(back, left, right, front);
    }

    private static @NotNull Material getOrAir(@NotNull List<Material> list, int index) {
        return index < list.size() ? list.get(index) : Material.BRICK;
    }
}
