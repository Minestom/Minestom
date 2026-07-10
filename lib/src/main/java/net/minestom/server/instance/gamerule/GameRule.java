package net.minestom.server.instance.gamerule;

import net.minestom.server.registry.Registry;
import net.minestom.server.registry.StaticProtocolObject;
import org.jetbrains.annotations.Nullable;

/// Bindings for [Game rule](https://minecraft.wiki/w/Game_rule)
public sealed interface GameRule<T> extends GameRules, StaticProtocolObject<GameRule<?>> permits GameRuleImpl {
    static Registry<GameRule<?>> staticRegistry() {
        return GameRuleImpl.REGISTRY;
    }

    @Deprecated(forRemoval = true)
    @Override
    @Nullable
    default Object registry() {
        return null;
    }

    T defaultValue();
}
