package net.minestom.server.component;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import net.minestom.server.codec.Codec;
import net.minestom.server.item.enchant.EffectComponent;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.StaticProtocolObject;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.UnaryOperator;

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

    /**
     * Freezes the given value if possible. For example, collections should be frozen.
     * <br>
     * Note: Only {@link T} itself is required to be frozen, the objects inside {@link T} should be immutable.
     *
     * @param value the value to freeze
     * @return the frozen value, or the original value if it could not be frozen
     */
    T freeze(T value);

    static @Nullable DataComponent<?> fromKey(@KeyPattern String key) {
        return fromKey(Key.key(key));
    }

    static @Nullable DataComponent<?> fromKey(Key key) {
        return DataComponentImpl.NAMESPACES.get(key);
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
            @Nullable NetworkBuffer.Type<T> network,
            @Nullable Codec<T> codec,
            @Nullable UnaryOperator<T> freeze
    ) {
        return new DataComponentImpl<>(id, key, network, codec, freeze);
    }
}
