package net.minestom.server.sound;

import net.kyori.adventure.key.Key;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.StaticProtocolObject;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

record BuiltinSoundEvent(Key key, int id) implements StaticProtocolObject, SoundEvent {
    private static final Registry.Container<BuiltinSoundEvent> CONTAINER = Registry.createStaticContainer(Registry.Resource.SOUNDS,
            (key, properties) -> new BuiltinSoundEvent(Key.key(key), properties.getInt("id")));

    static SoundEvent get(@NotNull String key) {
        return CONTAINER.get(key);
    }

    static SoundEvent getSafe(@NotNull String key) {
        return CONTAINER.getSafe(key);
    }

    static SoundEvent getId(int id) {
        return CONTAINER.getId(id);
    }

    static Collection<? extends SoundEvent> values() {
        return CONTAINER.values();
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
