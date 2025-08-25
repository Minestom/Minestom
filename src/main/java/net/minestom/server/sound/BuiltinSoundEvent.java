package net.minestom.server.sound;

import net.kyori.adventure.key.Key;
import net.minestom.server.registry.*;
import org.jetbrains.annotations.UnknownNullability;

record BuiltinSoundEvent(Key key, int id) implements StaticProtocolObject<BuiltinSoundEvent>, SoundEvent {
    @SuppressWarnings("unchecked")
    static final Registry<BuiltinSoundEvent> REGISTRY =
            RegistryData.createStaticRegistry(
                    (RegistryKey<Registry<BuiltinSoundEvent>>) (RegistryKey<?>) BuiltinRegistries.SOUND_EVENT, // Cant expose RegistryKey<BuiltinSoundEvent> directly
                    (namespace, properties) -> new BuiltinSoundEvent(namespace.key(), properties.getInt("id"))
            );

    static @UnknownNullability SoundEvent get(String key) {
        return REGISTRY.get(Key.key(key));
    }

    static @UnknownNullability SoundEvent get(RegistryKey<? extends SoundEvent> key) {
        return REGISTRY.get((RegistryKey<BuiltinSoundEvent>) key);
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
