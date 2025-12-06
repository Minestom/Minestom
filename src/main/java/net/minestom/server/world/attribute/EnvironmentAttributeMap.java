package net.minestom.server.world.attribute;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.utils.Either;
import net.minestom.server.world.attribute.EnvironmentAttribute.Modifier;

import java.util.Map;

public record EnvironmentAttributeMap(Map<EnvironmentAttribute<?>, Entry<?, ?>> entries) {
    public static final EnvironmentAttributeMap EMPTY = new EnvironmentAttributeMap(Map.of());

    public static final Codec<EnvironmentAttributeMap> CODEC = EnvironmentAttribute.CODEC
            .mapValueTyped(Entry::codec0, true)
            .transform(EnvironmentAttributeMap::new, EnvironmentAttributeMap::entries);

    public EnvironmentAttributeMap {
        entries = Map.copyOf(entries);
    }

    public record Entry<T, Arg>(Arg argument, Modifier<T, Arg> modifier) {

        @SuppressWarnings("unchecked")
        public static <T> Codec<Entry<T, ?>> codec(EnvironmentAttribute<T> attribute) {
            // A value is represented by either a single value which acts as an override,
            // or a struct with `modifier` and `argument` keys (full codec).

            Codec<Entry<T, ?>> fullCodec = attribute.type().modifierCodec()
                    .unionType("modifier", Entry::fullCodec, Entry::modifier);

            final var override = new Modifier.Override<>(attribute.valueCodec());
            return Codec.Either(attribute.valueCodec(), fullCodec).transform(
                    either -> either.unify(
                            value -> new Entry<>(value, override),
                            u -> u),
                    entry -> entry.modifier instanceof Modifier.Override
                            ? Either.left((T) entry.argument) : Either.right(entry));
        }

        private static Codec<Entry<?, ?>> codec0(EnvironmentAttribute<?> attribute) {
            //noinspection unchecked,rawtypes
            return (Codec) codec(attribute);
        }

        private static <T, Arg> StructCodec<Entry<T, Arg>> fullCodec(Modifier<T, Arg> modifier) {
            return StructCodec.struct(
                    "argument", modifier.argumentCodec(), Entry::argument,
                    (argument) -> new Entry<>(argument, modifier)
            );
        }

    }


}
