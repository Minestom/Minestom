package net.minestom.server.instance.block.jukebox;

import net.kyori.adventure.text.Component;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Holder;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.registry.BuiltinRegistries;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.Either;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public sealed interface JukeboxSong extends Holder.Direct<JukeboxSong>, JukeboxSongs permits JukeboxSongImpl {
    @NotNull NetworkBuffer.Type<JukeboxSong> REGISTRY_NETWORK_TYPE = NetworkBufferTemplate.template(
            SoundEvent.NETWORK_TYPE, JukeboxSong::soundEvent,
            NetworkBuffer.COMPONENT, JukeboxSong::description,
            NetworkBuffer.FLOAT, JukeboxSong::lengthInSeconds,
            NetworkBuffer.VAR_INT, JukeboxSong::comparatorOutput,
            JukeboxSong::create);
    @NotNull Codec<JukeboxSong> REGISTRY_CODEC = StructCodec.struct(
            "sound_event", SoundEvent.CODEC, JukeboxSong::soundEvent,
            "description", Codec.COMPONENT, JukeboxSong::description,
            "length_in_seconds", Codec.FLOAT, JukeboxSong::lengthInSeconds,
            "comparator_output", Codec.INT, JukeboxSong::comparatorOutput,
            JukeboxSong::create);

    // This is a similar case to PaintingVariant, see comment there for why one of these is a holder and not the other.
    // However, in this case, this component _must_ be hashable, which uses the regular codec on the client which does not
    // support holders. So it is **never valid** to use a direct holder here, so we use a weirdly serialized registrykey here.
    @NotNull NetworkBuffer.Type<RegistryKey<JukeboxSong>> NETWORK_TYPE = Holder.networkType(Registries::jukeboxSong, REGISTRY_NETWORK_TYPE)
            .transform(Holder::asKey, key -> key);
    @NotNull Codec<RegistryKey<JukeboxSong>> CODEC = RegistryKey.codec(Registries::jukeboxSong);

    // The network type of jukebox playable is an EitherHolder, but as discussed it always has to be a registry key,
    // so we just map to that type and dont think about it any more.
    @NotNull NetworkBuffer.Type<RegistryKey<JukeboxSong>> JUKEBOX_PLAYABLE_NETWORK_TYPE = NetworkBuffer.Either(NETWORK_TYPE, NETWORK_TYPE)
            .transform(e -> ((Either.Left<RegistryKey<JukeboxSong>, RegistryKey<JukeboxSong>>) e).value(), Either::left);

    static @NotNull JukeboxSong create(
            @NotNull SoundEvent soundEvent,
            @NotNull Component description,
            float lengthInSeconds,
            int comparatorOutput
    ) {
        return new JukeboxSongImpl(soundEvent, description, lengthInSeconds, comparatorOutput);
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
        return DynamicRegistry.load(BuiltinRegistries.JUKEBOX_SONG, REGISTRY_CODEC);
    }

    @NotNull SoundEvent soundEvent();

    @NotNull Component description();

    float lengthInSeconds();

    int comparatorOutput();

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
            return new JukeboxSongImpl(soundEvent, description, lengthInSeconds, comparatorOutput);
        }
    }

}
