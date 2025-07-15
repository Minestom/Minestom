package net.minestom.server.sound;

import net.kyori.adventure.key.Key;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.registry.StaticProtocolObject;
import org.jetbrains.annotations.UnknownNullability;

record BuiltinSoundEvent(Key key, int id) implements StaticProtocolObject<BuiltinSoundEvent>, SoundEvent {
    static final Registry<BuiltinSoundEvent> REGISTRY = RegistryData.createStaticRegistry(Key.key("minecraft:sound_event"),
            (namespace, properties) -> new BuiltinSoundEvent(Key.key(namespace), properties.getInt("id")));

    static @UnknownNullability SoundEvent get(String key) {
        return REGISTRY.get(Key.key(key));
    }

    @Override
    public String toString() {
        return name();
    }

    @Override
    public String name() {
        return StaticProtocolObject.super.name();
    }
}
