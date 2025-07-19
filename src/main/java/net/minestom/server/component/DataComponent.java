package net.minestom.server.component;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.item.enchant.EffectComponent;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.StaticProtocolObject;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    @NotNull NetworkBuffer.Type<DataComponent<?>> NETWORK_TYPE = NetworkBuffer.VAR_INT.transform(DataComponent::fromId, DataComponent::id);
    @NotNull Codec<DataComponent<?>> CODEC = Codec.STRING.transform(DataComponent::fromKey, DataComponent::name);

    @NotNull NetworkBuffer.Type<DataComponentMap> MAP_NETWORK_TYPE = DataComponentMap.networkType(DataComponent::fromId);
    @NotNull Codec<DataComponentMap> MAP_NBT_TYPE = DataComponentMap.codec(DataComponent::fromId, DataComponent::fromKey);

    @NotNull NetworkBuffer.Type<DataComponentMap> PATCH_NETWORK_TYPE = DataComponentMap.patchNetworkType(DataComponent::fromId, true);
    @NotNull NetworkBuffer.Type<DataComponentMap> UNTRUSTED_PATCH_NETWORK_TYPE = DataComponentMap.patchNetworkType(DataComponent::fromId, false);
    @NotNull Codec<DataComponentMap> PATCH_CODEC = DataComponentMap.patchCodec(DataComponent::fromId, DataComponent::fromKey);

    /**
     * Represents any type which can hold data components. Represents a finalized view of a component, that is to say
     * an implementation may represent a patch on top of another Holder, however the return values of this type
     * will always represent the merged view.
     */
    interface Holder {
        default boolean has(@NotNull DataComponent<?> component) {
            return get(component) != null;
        }

        <T> @Nullable T get(@NotNull DataComponent<T> component);

        default <T> @NotNull T get(@NotNull DataComponent<T> component, @NotNull T defaultValue) {
            final T value = get(component);
            return value != null ? value : defaultValue;
        }
    }

    record Value(@NotNull DataComponent<?> component, @Nullable Object value) {
    }

    boolean isSynced();
    boolean isSerialized();

    @NotNull T read(@NotNull NetworkBuffer reader);
    void write(@NotNull NetworkBuffer writer, @NotNull T value);

    static @Nullable DataComponent<?> fromKey(@NotNull String key) {
        return DataComponentImpl.NAMESPACES.get(key);
    }

    static @Nullable DataComponent<?> fromKey(@NotNull Key key) {
        return fromKey(key.asString());
    }

    static @Nullable DataComponent<?> fromId(int id) {
        return DataComponentImpl.IDS.get(id);
    }

    static @NotNull Collection<DataComponent<?>> values() {
        return DataComponentImpl.NAMESPACES.values();
    }

    @ApiStatus.Internal
    static <T> DataComponent<T> createHeadless(
            int id, @NotNull Key key,
            @Nullable NetworkBuffer.Type<T> network,
            @Nullable Codec<T> codec
    ) {
        return new DataComponentImpl<>(id, key, network, codec);
    }
}
