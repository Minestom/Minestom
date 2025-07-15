package net.minestom.server.component;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.item.enchant.EffectComponent;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.StaticProtocolObject;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

/**
 * A common type to represent all forms of component in the game. Each group of component types has its own declaration
 * file (see {@link net.minestom.server.component.DataComponent} for example).
 *
 * @param <T> The value type of the component
 * @see net.minestom.server.component.DataComponent
 * @see EffectComponent
 */
public sealed interface DataComponent<T> extends StaticProtocolObject<DataComponent<T>>, Codec<T> permits DataComponentImpl {

    NetworkBuffer.Type<DataComponent<?>> NETWORK_TYPE = NetworkBuffer.VAR_INT.transform(DataComponent::fromId, DataComponent::id);
    Codec<DataComponent<?>> CODEC = Codec.STRING.transform(DataComponent::fromKey, DataComponent::name);

    NetworkBuffer.Type<DataComponentMap> MAP_NETWORK_TYPE = DataComponentMap.networkType(DataComponent::fromId);
    Codec<DataComponentMap> MAP_NBT_TYPE = DataComponentMap.codec(DataComponent::fromId, DataComponent::fromKey);

    NetworkBuffer.Type<DataComponentMap> PATCH_NETWORK_TYPE = DataComponentMap.patchNetworkType(DataComponent::fromId, true);
    NetworkBuffer.Type<DataComponentMap> UNTRUSTED_PATCH_NETWORK_TYPE = DataComponentMap.patchNetworkType(DataComponent::fromId, false);
    Codec<DataComponentMap> PATCH_CODEC = DataComponentMap.patchCodec(DataComponent::fromId, DataComponent::fromKey);

    /**
     * Represents any type which can hold data components. Represents a finalized view of a component, that is to say
     * an implementation may represent a patch on top of another Holder, however the return values of this type
     * will always represent the merged view.
     */
    interface Holder {
        default boolean has(DataComponent<?> component) {
            return get(component) != null;
        }

        <T> @Nullable T get(DataComponent<T> component);

        default <T> T get(DataComponent<T> component, T defaultValue) {
            final T value = get(component);
            return value != null ? value : defaultValue;
        }
    }

    record Value(DataComponent<?> component, @Nullable Object value) {
    }

    boolean isSynced();
    boolean isSerialized();

    T read(NetworkBuffer reader);
    void write(NetworkBuffer writer, T value);

    static @Nullable DataComponent<?> fromKey(String key) {
        return DataComponentImpl.NAMESPACES.get(key);
    }

    static @Nullable DataComponent<?> fromKey(Key key) {
        return fromKey(key.asString());
    }

    static @Nullable DataComponent<?> fromId(int id) {
        return DataComponentImpl.IDS.get(id);
    }

    static Collection<DataComponent<?>> values() {
        return DataComponentImpl.NAMESPACES.values();
    }

    @ApiStatus.Internal
    static <T> DataComponent<T> createHeadless(
            int id, Key key,
            NetworkBuffer.@Nullable Type<T> network,
            @Nullable Codec<T> codec
    ) {
        return new DataComponentImpl<>(id, key, network, codec);
    }
}
