package net.minestom.server.item.armor;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.adventure.serializer.nbt.NbtComponentSerializer;
import net.minestom.server.registry.Registry;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

record TrimPatternImpl(Registry.TrimPatternEntry registry, int id) implements TrimPattern {
    static final AtomicInteger i = new AtomicInteger();
    private static final Registry.Container<TrimPattern> CONTAINER;

    static {
        CONTAINER = Registry.createStaticContainer(Registry.Resource.TRIM_PATTERNS,
                (namespace, properties) -> new TrimPatternImpl(Registry.trimPattern(namespace, properties)));
    }

    public TrimPatternImpl(Registry.TrimPatternEntry registry) {
        this(registry, i.getAndIncrement());
    }

    public static TrimPattern get(String namespace) {
        return CONTAINER.get(namespace);
    }

    static Collection<TrimPattern> values() {
        return CONTAINER.values();
    }

    public CompoundBinaryTag asNBT() {
        return CompoundBinaryTag.builder()
                .putString("asset_id", assetID().asString())
                .putString("template_item", template().namespace().asString())
                .put("description", NbtComponentSerializer.nbt().serialize(description()))
                .putBoolean("decal", decal())
                .build();
    }

}
