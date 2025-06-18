package net.minestom.server.sound;

import net.kyori.adventure.key.Key;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.registry.BuiltinRegistries;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.registry.StaticProtocolObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

record BuiltinSoundEvent(Key key, int id) implements StaticProtocolObject<BuiltinSoundEvent>, SoundEvent {
    @SuppressWarnings("unchecked")
    static final Registry<BuiltinSoundEvent> REGISTRY =
            RegistryData.createStaticRegistry(
                    (RegistryKey<Registry<BuiltinSoundEvent>>) (RegistryKey<?>) BuiltinRegistries.SOUND_EVENT, // Cant expose RegistryKey<BuiltinSoundEvent> directly
                    (namespace, properties) -> new BuiltinSoundEvent(Key.key(namespace), properties.getInt("id"))
            );

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
