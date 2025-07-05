package net.minestom.server.registry;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.codec.Codec;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a reference to a {@link Registry} entry.
 *
 * @param <T> the type of the registry entry
 */
@ApiStatus.NonExtendable
public sealed interface RegistryKey<T> extends Holder<T>, Keyed permits RegistryKeyImpl, StaticProtocolObject {

    static <T> NetworkBuffer.@NotNull Type<RegistryKey<T>> networkType(@NotNull Registries.Selector<T> selector) {
        return new RegistryNetworkTypes.RegistryKeyImpl<>(selector);
    }

    static <T> @NotNull Codec<RegistryKey<T>> codec(@NotNull Registries.Selector<T> selector) {
        return new RegistryCodecs.RegistryKeyImpl<>(selector);
    }

    static <T> NetworkBuffer.@NotNull Type<RegistryKey<T>> uncheckedNetworkType() {
        return NetworkBuffer.KEY.transform(RegistryKeyImpl::new, RegistryKey::key);
    }

    static <T> @NotNull Codec<RegistryKey<T>> uncheckedCodec() {
        return Codec.KEY.transform(RegistryKeyImpl::new, RegistryKey::key);
    }

    /**
     * Creates a new {@link RegistryKey} from the given raw string. Should not be used externally.
     * Registry keys are returned from {@link DynamicRegistry#register(Keyed, Object)}.
     */
    @ApiStatus.Internal
    static <T> @NotNull RegistryKey<T> unsafeOf(@NotNull String key) {
        return unsafeOf(Key.key(key));
    }

    /**
     * Creates a new {@link RegistryKey} from the given raw string. Should not be used externally.
     * Registry keys are returned from {@link DynamicRegistry#register(Keyed, Object)}.
     */
    @ApiStatus.Internal
    static <T> @NotNull RegistryKey<T> unsafeOf(@NotNull Key key) {
        return new RegistryKeyImpl<>(key);
    }

    default @NotNull String name() {
        return key().asString();
    }

}
