package net.minestom.server.sound;

import net.kyori.adventure.key.Key;
import net.minestom.server.registry.BuiltinRegistries;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.registry.StaticProtocolObject;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnknownNullability;

public record BuiltinSoundEvent(Key key, int id) implements StaticProtocolObject<BuiltinSoundEvent>, SoundEvent {
    static final Registry<BuiltinSoundEvent> REGISTRY = RegistryData.createStaticRegistry(reinterpretKey(),
            (namespace, properties) -> new BuiltinSoundEvent(Key.key(namespace), properties.getInt("id")));

    @ApiStatus.Internal
    public BuiltinSoundEvent {}

    @SuppressWarnings("all")
    private static RegistryKey<Registry<BuiltinSoundEvent>> reinterpretKey() {
        //Cant use directly due to this class implementing the StaticProtocolObject.
        return (RegistryKey<Registry<BuiltinSoundEvent>>) (RegistryKey<?>) BuiltinRegistries.SOUND_EVENT;
    }

    @SuppressWarnings("all")
    static @UnknownNullability BuiltinSoundEvent get(RegistryKey<SoundEvent> key) {
        //Cant test directly due to this class implementing the registry.
        return REGISTRY.get((RegistryKey<BuiltinSoundEvent>) (RegistryKey<?>) key);
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
