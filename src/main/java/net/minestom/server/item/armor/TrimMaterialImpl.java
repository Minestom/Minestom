package net.minestom.server.item.armor;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import net.minestom.server.adventure.serializer.nbt.NbtComponentSerializer;
import net.minestom.server.registry.Registry;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

record TrimMaterialImpl(Registry.TrimMaterialEntry registry, int id) implements TrimMaterial {
    static final AtomicInteger i = new AtomicInteger();
    private static final Registry.Container<TrimMaterial> CONTAINER;

    static {
        CONTAINER = Registry.createStaticContainer(Registry.Resource.TRIM_MATERIALS,
                (namespace, properties) -> new TrimMaterialImpl(Registry.trimMaterial(namespace, properties)));
    }

    public TrimMaterialImpl(Registry.TrimMaterialEntry registry) {
        this(registry, i.getAndIncrement());
    }

    public static TrimMaterial get(String namespace) {
        return CONTAINER.get(namespace);
    }

    static Collection<TrimMaterial> values() {
        return CONTAINER.values();
    }

    public CompoundBinaryTag asNBT() {
        return CompoundBinaryTag.builder()
                .putString("asset_name", assetName())
                .putString("ingredient", ingredient().namespace().asString())
                .putFloat("item_model_index", itemModelIndex())
                .put("override_armor_materials", CompoundBinaryTag.from(overrideArmorMaterials().entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, entry -> StringBinaryTag.stringBinaryTag(entry.getValue())))))
                .put("description", NbtComponentSerializer.nbt().serialize(description()))
                .build();
    }

}
