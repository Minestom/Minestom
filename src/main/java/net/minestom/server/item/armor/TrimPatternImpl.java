package net.minestom.server.item.armor;

import net.minestom.server.adventure.serializer.nbt.NbtComponentSerializer;
import net.minestom.server.registry.Registry;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

record TrimPatternImpl(Registry.TrimPatternEntry registry, int id) implements TrimPattern {
    static final AtomicInteger i = new AtomicInteger();
    private static final Registry.Container<TrimPattern> CONTAINER;

    static {
        CONTAINER = Registry.createContainer(Registry.Resource.TRIM_PATTERNS,
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

    public NBTCompound asNBT() {
        return NBT.Compound(nbt -> {
            nbt.setString("asset_id", assetID().asString());
            nbt.setString("template_item", template().namespace().asString());
            nbt.set("description", NbtComponentSerializer.nbt().serialize(description()));
            nbt.setByte("decal", (byte) (decal() ? 1 : 0));
        });
    }

}
