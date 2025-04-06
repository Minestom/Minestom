package net.minestom.server.sound;

import net.kyori.adventure.key.Key;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.registry.StaticProtocolObject;
import net.minestom.server.registry.StaticRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

record BuiltinSoundEvent(Key key, int id) implements StaticProtocolObject, SoundEvent {
    static final StaticRegistry<BuiltinSoundEvent> REGISTRY = RegistryData.createStaticRegistry(
            RegistryData.Resource.SOUNDS, "minecraft:sound_event",
            (namespace, properties) -> new BuiltinSoundEvent(Key.key(namespace), properties.getInt("id")));

    static @UnknownNullability SoundEvent get(@NotNull String key) {
        return REGISTRY.get(Key.key(key));
    }

    @Override
    public String toString() {
        return name();
    }

    @Override
    public @NotNull String name() {
        return StaticProtocolObject.super.name();
    }
}
