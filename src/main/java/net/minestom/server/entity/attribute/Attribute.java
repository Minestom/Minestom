package net.minestom.server.entity.attribute;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.registry.StaticProtocolObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public sealed interface Attribute extends StaticProtocolObject, Attributes permits AttributeImpl {
    @NotNull NetworkBuffer.Type<Attribute> NETWORK_TYPE = NetworkBuffer.VAR_INT.transform(AttributeImpl::getId, Attribute::id);
    @NotNull Codec<Attribute> CODEC = Codec.STRING.transform(AttributeImpl::get, Attribute::name);

    @Contract(pure = true)
    @NotNull RegistryData.AttributeEntry registry();

    @Override
    default @NotNull Key key() {
        return registry().key();
    }

    @Override
    default int id() {
        return registry().id();
    }

    default double defaultValue() {
        return registry().defaultValue();
    }

    default double minValue() {
        return registry().minValue();
    }

    default double maxValue() {
        return registry().maxValue();
    }

    default boolean isSynced() {
        return registry().clientSync();
    }

    static @NotNull Collection<@NotNull Attribute> values() {
        return AttributeImpl.values();
    }

    static @Nullable Attribute fromKey(@NotNull String key) {
        return AttributeImpl.getSafe(key);
    }

    static @Nullable Attribute fromKey(@NotNull Key key) {
        return fromKey(key.asString());
    }

    static @Nullable Attribute fromId(int id) {
        return AttributeImpl.getId(id);
    }

}
