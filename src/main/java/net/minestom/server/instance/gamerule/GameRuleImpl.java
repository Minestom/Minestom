package net.minestom.server.instance.gamerule;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryData;

import java.util.Objects;

record GameRuleImpl<T>(Key key, int id, T defaultValue) implements GameRule<T> {
    static final Registry<GameRule<?>> REGISTRY = RegistryData.createStaticRegistry(Key.key("game_rule"), GameRuleImpl::parse);

    // default is typed as String
    static GameRule<?> parse(@KeyPattern String namespace, RegistryData.Properties properties) {
        return switch (properties.getString("type")) {
            case "boolean" ->
                    new GameRuleImpl<>(Key.key(namespace), properties.getInt("id"), Boolean.valueOf(properties.getString("default")));
            case "integer" ->
                    new GameRuleImpl<>(Key.key(namespace), properties.getInt("id"), Integer.valueOf(properties.getString("default")));
            default -> throw new IllegalArgumentException("Unknown game rule type: " + properties.getString("type"));
        };
    }

    @SuppressWarnings("unchecked")
    static <T> GameRule<T> get(@KeyPattern String key) {
        return (GameRule<T>) Objects.requireNonNull(REGISTRY.get(Key.key(key)));
    }
}
