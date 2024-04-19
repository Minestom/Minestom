package net.minestom.server.sound;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.StaticProtocolObject;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

record BuiltinSoundEvent(NamespaceID namespace, int id) implements StaticProtocolObject, SoundEvent {
    private static final Registry.Container<BuiltinSoundEvent> CONTAINER = Registry.createStaticContainer(Registry.Resource.SOUNDS,
            (namespace, properties) -> new BuiltinSoundEvent(NamespaceID.from(namespace), properties.getInt("id")));

    public static final NetworkBuffer.Type<SoundEvent> NETWORK_TYPE = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, SoundEvent value) {
            switch (value) {
                case BuiltinSoundEvent soundEvent -> buffer.write(NetworkBuffer.VAR_INT, soundEvent.id + 1);
                case CustomSoundEvent soundEvent -> {
                    buffer.write(NetworkBuffer.VAR_INT, 0); // Custom sound
                    buffer.write(NetworkBuffer.STRING, soundEvent.name());
                    buffer.writeOptional(NetworkBuffer.FLOAT, soundEvent.range());
                }
            }
        }

        @Override
        public SoundEvent read(@NotNull NetworkBuffer buffer) {
            int id = buffer.read(NetworkBuffer.VAR_INT) - 1;
            if (id != -1) return getId(id);

            NamespaceID namespace = NamespaceID.from(buffer.read(NetworkBuffer.STRING));
            return new CustomSoundEvent(namespace, buffer.readOptional(NetworkBuffer.FLOAT));
        }
    };

    static SoundEvent get(@NotNull String namespace) {
        return CONTAINER.get(namespace);
    }

    static SoundEvent getSafe(@NotNull String namespace) {
        return CONTAINER.getSafe(namespace);
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
}
