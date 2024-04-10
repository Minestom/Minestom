package net.minestom.server.entity.damage;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.network.packet.server.configuration.RegistryDataPacket;
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
    public @NotNull RegistryDataPacket.Entry toRegistryEntry() {
        return new RegistryDataPacket.Entry(name(), CompoundBinaryTag.builder()
                .putFloat("exhaustion", registry.exhaustion())
                .putString("message_id", registry.messageId())
                .putString("scaling", registry.scaling())
                .build());
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

    private static RegistryDataPacket lazyRegistryDataPacket = null;

    static @NotNull RegistryDataPacket registryDataPacket() {
        if (lazyRegistryDataPacket != null) return lazyRegistryDataPacket;
        return lazyRegistryDataPacket = new RegistryDataPacket(
                "minecraft:damage_type",
                values().stream()
                        .map(DamageType::toRegistryEntry)
                        .toList()
        );
    }
}