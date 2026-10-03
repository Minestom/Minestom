package net.minestom.server.instance.gamerule;

import net.minestom.server.registry.Registry;
import net.minestom.server.registry.StaticProtocolObject;

/// Bindings for [Game rule](https://minecraft.wiki/w/Game_rule)
public sealed interface GameRule<T> extends GameRules, StaticProtocolObject<GameRule<?>> permits GameRuleImpl {
    static Registry<GameRule<?>> staticRegistry() {
        return GameRuleImpl.REGISTRY;
    }

    T defaultValue();
}
