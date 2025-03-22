package net.minestom.server.registry;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.Result;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Objects;

/**
 * A {@link Holder} is either a reference into a registry or a direct value which isnt necessarily registered with the client.
 */
public sealed interface Holder<T> {

    @Nullable T resolve(@NotNull DynamicRegistry<T> registry);

    record Direct<T>(@NotNull T value) implements Holder<T> {
        @Override
        public @NotNull T resolve(@NotNull DynamicRegistry<T> registry) {
            return value;
        }
    }

    record Reference<T>(@NotNull DynamicRegistry.Key<T> key) implements Holder<T> {
        @Override
        public @Nullable T resolve(@NotNull DynamicRegistry<T> registry) {
            return registry.get(key);
        }
    }


    /**
     * A lazy holder allows for the value to be read as a reference without resolving it against the registry.
     * @param <T>
     */
    record Lazy<T>(@UnknownNullability Holder<T> holder, @UnknownNullability Key reference) {
        public Lazy(@NotNull Holder<T> holder) {
            this(holder, null);
        }

        public Lazy(@NotNull Key reference) {
            this(null, reference);
        }

        public @Nullable T resolve(@NotNull DynamicRegistry<T> registry) {
            return holder != null ? holder.resolve(registry) : registry.get(reference);
        }
    }

    static <T> NetworkBuffer.@NotNull Type<Holder<T>> networkType(@NotNull Registries.Selector<T> selector, @NotNull NetworkBuffer.Type<T> registryNetworkType) {
        return new NetworkBuffer.Type<>() {
            @Override
            public void write(@NotNull NetworkBuffer buffer, Holder<T> value) {
                final var registries = Objects.requireNonNull(buffer.registries(), "Buffer is missing registries");
                final var registry = selector.select(registries);
                switch (value) {
                    case Reference(DynamicRegistry.Key<T> key) -> {
                        final int id = registry.getId(key);
                        if (id == -1)
                            throw new IllegalArgumentException("Unknown key " + key + " for registry " + registry);
                        buffer.write(NetworkBuffer.VAR_INT, id + 1);
                    }
                    case Direct(T directValue) -> {
                        buffer.write(NetworkBuffer.VAR_INT, 0);
                        buffer.write(registryNetworkType, directValue);
                    }
                }
            }

            @Override
            public Holder<T> read(@NotNull NetworkBuffer buffer) {
                final var registries = Objects.requireNonNull(buffer.registries(), "Buffer is missing registries");
                final var registry = selector.select(registries);
                final int id = buffer.read(NetworkBuffer.VAR_INT) - 1;
                if (id == -1) {
                    final T value = buffer.read(registryNetworkType);
                    return new Direct<>(value);
                } else {
                    final var key = registry.getKey(id);
                    if (key == null)
                        throw new IllegalStateException("Unknown id " + id + " for registry " + registry);
                    return new Reference<>(key);
                }
            }
        };
    }
    static <T> @NotNull Codec<Holder<T>> codec(@NotNull Registries.Selector<T> selector, @NotNull Codec<T> registryCodec) {
        return new Codec<>() {
            @Override
            public @NotNull <D> Result<D> encode(@NotNull Transcoder<D> coder, @Nullable Holder<T> value) {
                if (value == null) return new Result.Error<>("null");
                if (!(coder instanceof RegistryTranscoder<D>(var ignored, var registries)))
                    return new Result.Error<>("Missing registries in transcoder");
                final var registry = selector.select(registries);
                return switch (value) {
                    case Reference(DynamicRegistry.Key<T> key) -> {
                        if (registry.getId(key) == -1)
                            throw new IllegalArgumentException("Unknown key " + key + " for registry " + registry);
                        yield new Result.Ok<>(coder.createString(key.name()));
                    }
                    case Direct(T directValue) -> registryCodec.encode(coder, directValue);
                };
            }

            @Override
            public @NotNull <D> Result<Holder<T>> decode(@NotNull Transcoder<D> coder, @NotNull D value) {
                if (!(coder instanceof RegistryTranscoder<D>(var ignored, var registries)))
                    return new Result.Error<>("Missing registries in transcoder");
                final var registry = selector.select(registries);
                final Result<T> directResult = registryCodec.decode(coder, value);
                if (directResult instanceof Result.Ok(T direct))
                    return new Result.Ok<>(new Direct<>(direct));
                final Result<String> referenceResult = coder.getString(value);
                if (!(referenceResult instanceof Result.Ok(String reference)))
                    return referenceResult.cast();
                return new Result.Ok<>(new Reference<>(DynamicRegistry.Key.of(reference)));
            }
        };
    }

    static <T> NetworkBuffer.@NotNull Type<Holder.Lazy<T>> lazyNetworkType(@NotNull Registries.Selector<T> selector, @NotNull NetworkBuffer.Type<T> registryNetworkType) {
        final NetworkBuffer.Type<Holder<T>> holderNetworkType = networkType(selector, registryNetworkType);
        return new NetworkBuffer.Type<>() {
            @Override
            public void write(@NotNull NetworkBuffer buffer, Lazy<T> value) {
                buffer.write(NetworkBuffer.BOOLEAN, value.holder != null);
                if (value.holder != null) {
                    holderNetworkType.write(buffer, value.holder);
                } else {
                    buffer.write(NetworkBuffer.KEY, value.reference);
                }
            }

            @Override
            public Lazy<T> read(@NotNull NetworkBuffer buffer) {
                if (buffer.read(NetworkBuffer.BOOLEAN)) {
                    return new Lazy<>(holderNetworkType.read(buffer), null);
                } else {
                    return new Lazy<>(null, buffer.read(NetworkBuffer.KEY));
                }
            }
        };
    }
    static <T> @NotNull Codec<Holder.Lazy<T>> lazyCodec(@NotNull Registries.Selector<T> selector, @NotNull Codec<T> registryCodec) {
        final Codec<Holder<T>> holderCodec = codec(selector, registryCodec);
        return new Codec<>() {
            @Override
            public @NotNull <D> Result<Lazy<T>> decode(@NotNull Transcoder<D> coder, @NotNull D value) {
                final Result<Lazy<T>> holderResult = holderCodec.decode(coder, value).mapResult(Lazy::new);
                if (holderResult instanceof Result.Ok<Lazy<T>> ok) return ok;

                final Result<Lazy<T>> referenceResult = Codec.KEY.decode(coder, value).mapResult(Lazy::new);
                if (referenceResult instanceof Result.Ok<Lazy<T>> ok) return ok;

                // Return the original error since thats what we prioritize here
                return holderResult.cast();
            }

            @Override
            public @NotNull <D> Result<D> encode(@NotNull Transcoder<D> coder, @Nullable Lazy<T> value) {
                if (value == null) return new Result.Error<>("null");
                return value.holder != null
                        ? holderCodec.encode(coder, value.holder)
                        : Codec.KEY.encode(coder, value.reference);
            }
        };
    }
}
