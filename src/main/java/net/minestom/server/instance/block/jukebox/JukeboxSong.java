package net.minestom.server.instance.block.jukebox;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.Registry;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public sealed interface JukeboxSong extends ProtocolObject, JukeboxSongs permits JukeboxSongImpl {

    @NotNull NetworkBuffer.Type<DynamicRegistry.Key<JukeboxSong>> NETWORK_TYPE = NetworkBuffer.RegistryKey(Registries::jukeboxSong);
    @NotNull BinaryTagSerializer<DynamicRegistry.Key<JukeboxSong>> NBT_TYPE = BinaryTagSerializer.registryKey(Registries::jukeboxSong);

    static @NotNull JukeboxSong create(
            @NotNull SoundEvent soundEvent,
            @NotNull Component description,
            float lengthInSeconds,
            int comparatorOutput
    ) {
        return new JukeboxSongImpl(soundEvent, description, lengthInSeconds, comparatorOutput, null);
    }

    static @NotNull Builder builder() {
        return new Builder();
    }

    /**
     * <p>Creates a new registry for banner patterns, loading the vanilla banner patterns.</p>
     *
     * @see net.minestom.server.MinecraftServer to get an existing instance of the registry
     */
    @ApiStatus.Internal
    static @NotNull DynamicRegistry<JukeboxSong> createDefaultRegistry() {
        return DynamicRegistry.create(
                "minecraft:jukebox_song", JukeboxSongImpl.REGISTRY_NBT_TYPE, Registry.Resource.JUKEBOX_SONGS,
                (key, props) -> new JukeboxSongImpl(Registry.jukeboxSong(key, props))
        );
    }

    @NotNull SoundEvent soundEvent();

    @NotNull Component description();

    float lengthInSeconds();

    int comparatorOutput();

    @Override
    @Nullable Registry.JukeboxSongEntry registry();

    final class Builder {
        private SoundEvent soundEvent;
        private Component description;
        private float lengthInSeconds;
        private int comparatorOutput = 0;

        private Builder() {
        }

        public @NotNull Builder soundEvent(@NotNull SoundEvent soundEvent) {
            this.soundEvent = soundEvent;
            return this;
        }

        public @NotNull Builder description(@NotNull Component description) {
            this.description = description;
            return this;
        }

        public @NotNull Builder lengthInSeconds(float lengthInSeconds) {
            this.lengthInSeconds = lengthInSeconds;
            return this;
        }

        public @NotNull Builder comparatorOutput(int comparatorOutput) {
            this.comparatorOutput = comparatorOutput;
            return this;
        }

        public @NotNull JukeboxSong build() {
            return new JukeboxSongImpl(soundEvent, description, lengthInSeconds, comparatorOutput, null);
        }
    }

}
