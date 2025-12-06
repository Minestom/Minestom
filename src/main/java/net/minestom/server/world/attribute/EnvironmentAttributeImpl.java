package net.minestom.server.world.attribute;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import net.minestom.server.codec.Codec;
import net.minestom.server.registry.DynamicRegistry;

import java.util.Objects;

record EnvironmentAttributeImpl<T>(
        Key key,
        EnvironmentAttribute.Type<T> type,
        T defaultValue
) implements EnvironmentAttribute<T> {
    public static final DynamicRegistry<EnvironmentAttribute<?>> REGISTRY =
            DynamicRegistry.create(Key.key("environment_attribute"));
    public static final Codec<EnvironmentAttribute<?>> CODEC = Codec.KEY.transform(
            key -> Objects.requireNonNull(REGISTRY.get(key), () -> "no such environment attribute: " + key),
            EnvironmentAttribute::key);

    static <T> EnvironmentAttribute<T> register(
            @KeyPattern String key,
            EnvironmentAttribute.Type<T> type,
            T defaultValue
    ) {
        EnvironmentAttributeImpl<T> attribute = new EnvironmentAttributeImpl<>(Key.key(key), type, defaultValue);
        REGISTRY.register(attribute.key(), attribute);
        return attribute;
    }

    @Override
    public Codec<T> valueCodec() {
        return type.codec();
    }
}
