package net.minestom.server.entity.attribute;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.StaticProtocolObject;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public sealed interface Attribute extends StaticProtocolObject, Attributes permits AttributeImpl {
    @NotNull NetworkBuffer.Type<Attribute> NETWORK_TYPE = NetworkBuffer.VAR_INT.transform(AttributeImpl::getId, Attribute::id);
    @NotNull BinaryTagSerializer<Attribute> NBT_TYPE =  BinaryTagSerializer.STRING.map(AttributeImpl::get, Attribute::name);

    @Contract(pure = true)
    @NotNull Registry.AttributeEntry registry();

    @Override
    default @NotNull NamespaceID namespace() {
        return registry().namespace();
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

    static @Nullable Attribute fromNamespaceId(@NotNull String namespaceID) {
        return AttributeImpl.getSafe(namespaceID);
    }

    static @Nullable Attribute fromNamespaceId(@NotNull NamespaceID namespaceID) {
        return fromNamespaceId(namespaceID.asString());
    }

    static @Nullable Attribute fromId(int id) {
        return AttributeImpl.getId(id);
    }

}
