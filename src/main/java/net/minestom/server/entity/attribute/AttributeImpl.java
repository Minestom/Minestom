package net.minestom.server.entity.attribute;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

record AttributeImpl(@NotNull Registry.AttributeEntry registry) implements Attribute {
    private static final Registry.Container<Attribute> CONTAINER = Registry.createStaticContainer(Registry.Resource.ATTRIBUTES,
            (namespace, properties) -> new AttributeImpl(Registry.attribute(namespace, properties)));

    public static final NetworkBuffer.Type<Attribute> NETWORK_TYPE = NetworkBuffer.VAR_INT.map(AttributeImpl::getId, Attribute::id);
    public static final BinaryTagSerializer<Attribute> NBT_TYPE = BinaryTagSerializer.STRING.map(AttributeImpl::get, Attribute::name);

    static Attribute get(@NotNull String namespace) {
        return CONTAINER.get(namespace);
    }

    static Attribute getSafe(@NotNull String namespace) {
        return CONTAINER.getSafe(namespace);
    }

    static Attribute getId(int id) {
        return CONTAINER.getId(id);
    }

    static Collection<Attribute> values() {
        return CONTAINER.values();
    }

    @Override
    public String toString() {
        return name();
    }
}
