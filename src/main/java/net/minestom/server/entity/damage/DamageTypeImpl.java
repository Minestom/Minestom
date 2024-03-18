package net.minestom.server.entity.damage;

import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.minestom.server.registry.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

record DamageTypeImpl(Registry.DamageTypeEntry registry, int id) implements DamageType {
    private static final Registry.Container<DamageType> CONTAINER;

    static {
        AtomicInteger i = new AtomicInteger();
        CONTAINER = Registry.createStaticContainer(Registry.Resource.DAMAGE_TYPES,
                (namespace, properties) -> new DamageTypeImpl(Registry.damageType(namespace, properties), i.getAndIncrement()));
    }

    static DamageType get(@NotNull String namespace) {
        return CONTAINER.get(namespace);
    }

    static DamageType getSafe(@NotNull String namespace) {
        return CONTAINER.getSafe(namespace);
    }

    static DamageType getId(int id) {
        return CONTAINER.getId(id);
    }

    @Override
    public CompoundBinaryTag asNBT() {
        return CompoundBinaryTag.builder()
                .putFloat("exhaustion", registry.exhaustion())
                .putString("message_id", registry.messageId())
                .putString("scaling", registry.scaling())
                .build();
    }

    static Collection<DamageType> values() {
        return CONTAINER.values();
    }

    @Override
    public String toString() {
        return name();
    }

    @Override
    public int id() {
        return id;
    }

    private static CompoundBinaryTag lazyNbt = null;

    static CompoundBinaryTag getNBT() {
        if (lazyNbt == null) {
            var entries = ListBinaryTag.builder(BinaryTagTypes.COMPOUND);
            for (var damageType : values()) {
                entries.add(CompoundBinaryTag.builder()
                        .putInt("id", damageType.id())
                        .putString("name", damageType.name())
                        .put("element", damageType.asNBT())
                        .build());
            }

            lazyNbt = CompoundBinaryTag.builder()
                    .putString("type", "minecraft:damage_type")
                    .put("value", entries.build())
                    .build();
        }
        return lazyNbt;
    }
}