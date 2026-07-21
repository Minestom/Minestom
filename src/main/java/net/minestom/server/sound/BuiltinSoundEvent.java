package net.minestom.server.sound;

import net.kyori.adventure.key.Key;
import net.minestom.server.registry.*;
import org.jetbrains.annotations.UnknownNullability;

@SuppressWarnings("removal")
public record BuiltinSoundEvent(Key key, int id) implements StaticProtocolObject<BuiltinSoundEvent>, SoundEvent {
    static final Registry<BuiltinSoundEvent> REGISTRY = RegistryData.createStaticRegistry(BuiltinRegistries.SOUND_EVENT,
            (namespace, properties) -> new BuiltinSoundEvent(Key.key(namespace), properties.getInt("id")));

    static @UnknownNullability SoundEvent get(RegistryKey<? extends SoundEvent> key) {
        return REGISTRY.get(key.key());
    }

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
