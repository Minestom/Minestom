package net.minestom.server.loot.number;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.Either;

/**
 * An integer written directly, or the key of a context int provider that computes it when the item is used.
 * A reference travels as its key and is not checked against any registry.
 */
public sealed interface ResolvableInt permits ResolvableInt.Constant, ResolvableInt.Reference {
    Codec<ResolvableInt> CODEC = Codec.Either(Codec.INT, Codec.KEY).transform(
            either -> either.unify(Constant::new, Reference::new),
            ResolvableInt::unwrap);
    NetworkBuffer.Type<ResolvableInt> NETWORK_TYPE = NetworkBuffer.Either(NetworkBuffer.INT, NetworkBuffer.KEY).transform(
            either -> either.unify(Constant::new, Reference::new),
            ResolvableInt::unwrap);

    static ResolvableInt of(int value) {
        return new Constant(value);
    }

    static ResolvableInt of(Key key) {
        return new Reference(key);
    }

    private static Either<Integer, Key> unwrap(ResolvableInt resolvable) {
        return switch (resolvable) {
            case Constant(int value) -> Either.left(value);
            case Reference(Key key) -> Either.right(key);
        };
    }

    /**
     * An integer written directly.
     *
     * @param value the value
     */
    record Constant(int value) implements ResolvableInt {
    }

    /**
     * The key of a context int provider.
     *
     * @param key the key of the provider
     */
    record Reference(Key key) implements ResolvableInt {
    }
}
