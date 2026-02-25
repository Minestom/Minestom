package net.minestom.server.item.component;

import net.minestom.server.codec.Codec;
import net.minestom.server.item.Material;
import net.minestom.server.network.NetworkBuffer;

import java.util.List;

public record PotDecorations(
        Material back,
        Material left,
        Material right,
        Material front
) {
    public static final Material DEFAULT_ITEM = Material.BRICK;
    public static final PotDecorations EMPTY = new PotDecorations(DEFAULT_ITEM, DEFAULT_ITEM, DEFAULT_ITEM, DEFAULT_ITEM);

    public static final NetworkBuffer.Type<PotDecorations> NETWORK_TYPE = Material.NETWORK_TYPE.list(4).transform(PotDecorations::new, PotDecorations::asList);
    public static final Codec<PotDecorations> NBT_TYPE = Material.CODEC.list(4).transform(PotDecorations::new, PotDecorations::asList);

    public PotDecorations(List<Material> list) {
        this(getOrAir(list, 0), getOrAir(list, 1), getOrAir(list, 2), getOrAir(list, 3));
    }

    public PotDecorations(Material material) {
        this(material, material, material, material);
    }

    public List<Material> asList() {
        return List.of(back, left, right, front);
    }

    private static Material getOrAir(List<Material> list, int index) {
        return index < list.size() ? list.get(index) : Material.BRICK;
    }
}
