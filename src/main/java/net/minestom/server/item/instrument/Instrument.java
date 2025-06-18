package net.minestom.server.item.instrument;

import net.kyori.adventure.text.Component;
import net.minestom.server.ServerFlag;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Holder;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.BuiltinRegistries;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public sealed interface Instrument extends Holder.Direct<Instrument>, Instruments permits InstrumentImpl {
    @NotNull NetworkBuffer.Type<Instrument> REGISTRY_NETWORK_TYPE = NetworkBufferTemplate.template(
            SoundEvent.NETWORK_TYPE, Instrument::soundEvent,
            NetworkBuffer.FLOAT, Instrument::useDuration,
            NetworkBuffer.FLOAT, Instrument::range,
            NetworkBuffer.COMPONENT, Instrument::description,
            InstrumentImpl::new);
    @NotNull Codec<Instrument> REGISTRY_CODEC = StructCodec.struct(
            "sound_event", SoundEvent.CODEC, Instrument::soundEvent,
            "use_duration", Codec.FLOAT, Instrument::useDuration,
            "range", Codec.FLOAT, Instrument::range,
            "description", Codec.COMPONENT, Instrument::description,
            InstrumentImpl::new);

    @NotNull NetworkBuffer.Type<Holder<Instrument>> NETWORK_TYPE = Holder.networkType(Registries::instrument, REGISTRY_NETWORK_TYPE);
    @NotNull Codec<Holder<Instrument>> CODEC = Holder.codec(Registries::instrument, REGISTRY_CODEC);

    static @NotNull Instrument create(
            @NotNull SoundEvent soundEvent,
            float useDuration,
            float range,
            @NotNull Component description
    ) {
        return new InstrumentImpl(soundEvent, useDuration, range, description);
    }

    static @NotNull Builder builder() {
        return new Builder();
    }

    /**
     * <p>Creates a new registry for instruments, loading the vanilla instruments.</p>
     *
     * @see net.minestom.server.MinecraftServer to get an existing instance of the registry
     */
    @ApiStatus.Internal
    static @NotNull DynamicRegistry<Instrument> createDefaultRegistry() {
        return DynamicRegistry.load(BuiltinRegistries.INSTRUMENT, REGISTRY_CODEC);
    }

    @NotNull SoundEvent soundEvent();

    float useDuration();

    default int useDurationTicks() {
        return (int) (useDuration() * ServerFlag.SERVER_TICKS_PER_SECOND);
    }

    float range();

    @NotNull Component description();

    final class Builder {
        private SoundEvent soundEvent;
        private float useDuration;
        private float range;
        private Component description;

        private Builder() {
        }

        public @NotNull Builder soundEvent(@NotNull SoundEvent soundEvent) {
            this.soundEvent = soundEvent;
            return this;
        }

        public @NotNull Builder useDuration(float useDuration) {
            this.useDuration = useDuration;
            return this;
        }

        public @NotNull Builder range(float range) {
            this.range = range;
            return this;
        }

        public @NotNull Builder description(@NotNull Component description) {
            this.description = description;
            return this;
        }

        public @NotNull Instrument build() {
            return new InstrumentImpl(soundEvent, useDuration, range, description);
        }
    }
}
