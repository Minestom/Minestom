package net.minestom.server.entity.attribute;

import net.minestom.server.MinecraftServer;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public sealed interface Attribute extends ProtocolObject, Attributes permits AttributeImpl {
    @NotNull NetworkBuffer.Type<DynamicRegistry.Key<Attribute>> NETWORK_TYPE = NetworkBuffer.RegistryKey(Registries::attribute);
    @NotNull BinaryTagSerializer<DynamicRegistry.Key<Attribute>> NBT_TYPE = BinaryTagSerializer.registryKey(Registries::attribute);

    /**
     * Fetches an existing attribute from a registry.
     * <p>
     * The attribute must be found, or an exception will be thrown.
     *
     * @param key the attribute key
     * @param registries the registry in which to look up attributes, ex. {@link MinecraftServer#process()}
     * @return the attribute instance
     * @throws IllegalArgumentException if no attribute is found in the provided registry
     */
    static @NotNull Attribute lookup(@NotNull DynamicRegistry.Key<Attribute> key, @NotNull Registries registries) {
        final Attribute attribute = registries.attribute().get(key);
        Check.argCondition(attribute == null, "unknown attribute {0}", key.name());
        return attribute;
    }

    /**
     * Convenience overload of {@link Attribute#create(double, boolean, double, double)} to create a "server-side"
     * attribute that is not sent to the client.
     *
     * @param baseValue the base (default) value of the attribute
     * @param maxValue the maximum value of the attribute
     * @param minValue the minimum value of the attribute
     * @return a new attribute
     * @throws IllegalArgumentException if {@code minValue > maxValue}, or {@code baseValue} is not in range
     * {@code [minValue, maxValue]}
     */
    static @NotNull Attribute create(double baseValue, double maxValue, double minValue) {
        return create(baseValue, false, maxValue, minValue);
    }

    /**
     * Create a new, unregistered attribute, which must be registered before it can be used (see {@link Registries}).
     *
     * @param baseValue the base (default) value of the attribute
     * @param clientSync whether to update the client of when this attribute changes server-side; should generally be
     *                   {@code false} for custom attributes
     * @param maxValue the maximum value of the attribute
     * @param minValue the minimum value of the attribute
     * @return a new attribute
     * @throws IllegalArgumentException if {@code minValue > maxValue}, or {@code baseValue} is not in range
     * {@code [minValue, maxValue]}
     */
    static @NotNull Attribute create(double baseValue, boolean clientSync, double maxValue, double minValue) {
        Check.argCondition(maxValue < minValue, "maxValue must be <= minValue");
        Check.argCondition(baseValue < minValue, "baseValue must be >= minValue");
        Check.argCondition(baseValue > maxValue, "baseValue must be <= maxValue");
        return new AttributeImpl(baseValue, clientSync, maxValue, minValue, null);
    }

    @Contract(pure = true)
    @Nullable Registry.AttributeEntry registry();

    double defaultValue();

    double minValue();

    double maxValue();

    boolean isSynced();

    @ApiStatus.Internal
    static @NotNull DynamicRegistry<Attribute> createDefaultRegistry() {
        return DynamicRegistry.create("minecraft:attributes", AttributeImpl.REGISTRY_NBT_TYPE,
                Registry.Resource.ATTRIBUTES, (namespace, props) ->
                        new AttributeImpl(Registry.attribute(namespace, props)));
    }
}
