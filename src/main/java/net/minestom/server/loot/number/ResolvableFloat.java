package net.minestom.server.loot.number;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.Either;

/**
 * A float written directly, or the key of a context float provider that computes it when the item is used.
 * A reference travels as its key and is not checked against any registry.
 */
public sealed interface ResolvableFloat permits ResolvableFloat.Constant, ResolvableFloat.Reference {
    Codec<ResolvableFloat> CODEC = Codec.Either(Codec.FLOAT, Codec.KEY).transform(
            either -> either.unify(Constant::new, Reference::new),
            ResolvableFloat::unwrap);
    NetworkBuffer.Type<ResolvableFloat> NETWORK_TYPE = NetworkBuffer.Either(NetworkBuffer.FLOAT, NetworkBuffer.KEY).transform(
            either -> either.unify(Constant::new, Reference::new),
            ResolvableFloat::unwrap);

    static ResolvableFloat of(float value) {
        return new Constant(value);
    }

    static ResolvableFloat of(Key key) {
        return new Reference(key);
    }

    private static Either<Float, Key> unwrap(ResolvableFloat resolvable) {
        return switch (resolvable) {
            case Constant(float value) -> Either.left(value);
            case Reference(Key key) -> Either.right(key);
        };
    }

    /**
     * A float written directly.
     *
     * @param value the value
     */
    record Constant(float value) implements ResolvableFloat {
    }

    /**
     * The key of a context float provider.
     *
     * @param key the key of the provider
     */
    record Reference(Key key) implements ResolvableFloat {
    }
}
