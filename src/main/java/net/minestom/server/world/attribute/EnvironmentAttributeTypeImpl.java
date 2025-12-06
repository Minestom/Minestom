package net.minestom.server.world.attribute;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import net.minestom.server.codec.Codec;
import net.minestom.server.world.attribute.EnvironmentAttribute.Modifier;

import java.util.HashMap;
import java.util.Map;

record EnvironmentAttributeTypeImpl<T>(
        Key key,
        Codec<T> codec,
        Codec<Modifier<T, ?>> modifierCodec
) implements EnvironmentAttribute.Type<T> {

    static <T> EnvironmentAttribute.Type<T> register(
            @KeyPattern String key,
            Codec<T> codec,
            Map<Modifier.Operator, Modifier<T, ?>> operators
    ) {
        final var withOverride = new HashMap<>(operators);
        withOverride.put(Modifier.Operator.OVERRIDE, new Modifier.Override<>(codec));

        final var inverse = new HashMap<Modifier<T, ?>, Modifier.Operator>(operators.size());
        for (var entry : operators.entrySet()) inverse.put(entry.getValue(), entry.getKey());

        final Codec<Modifier<T, ?>> modifierCodec = Modifier.Operator.CODEC.transform(withOverride::get, inverse::get);
        return new EnvironmentAttributeTypeImpl<>(Key.key(key), codec, modifierCodec);
    }
}
