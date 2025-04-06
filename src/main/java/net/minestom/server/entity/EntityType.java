package net.minestom.server.entity;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import net.minestom.server.codec.Codec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.registry.StaticProtocolObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public sealed interface EntityType extends StaticProtocolObject, EntityTypes permits EntityTypeImpl {
    NetworkBuffer.Type<EntityType> NETWORK_TYPE = NetworkBuffer.VAR_INT.transform(EntityType::fromId, EntityType::id);
    Codec<EntityType> CODEC = Codec.INT.transform(EntityType::fromId, EntityType::id);

    /**
     * Returns the entity registry.
     *
     * @return the entity registry
     */
    @Contract(pure = true)
    @NotNull RegistryData.EntityEntry registry();

    @Override
    default @NotNull Key key() {
        return registry().key();
    }

    @Override
    default int id() {
        return registry().id();
    }

    default double width() {
        return registry().width();
    }

    default double height() {
        return registry().height();
    }

    static @NotNull Collection<@NotNull EntityType> values() {
        return EntityTypeImpl.REGISTRY.values();
    }

    static @Nullable EntityType fromKey(@KeyPattern @NotNull String key) {
        return fromKey(Key.key(key));
    }

    static @Nullable EntityType fromKey(@NotNull Key key) {
        return EntityTypeImpl.REGISTRY.get(key);
    }

    static @Nullable EntityType fromId(int id) {
        return EntityTypeImpl.REGISTRY.get(id);
    }
}
