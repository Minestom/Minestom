package net.minestom.server.world.timeline;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.utils.EaseFunction;
import net.minestom.server.world.attribute.EnvironmentAttribute;
import net.minestom.server.world.attribute.EnvironmentAttribute.Modifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public sealed interface Timeline extends Timelines permits TimelineImpl {
    @SuppressWarnings({"unchecked", "rawtypes"})
    Codec<Map<EnvironmentAttribute<?>, Track<?, ?>>> TRACKS_CODEC = EnvironmentAttribute.CODEC
            .mapValueTyped(attribute -> (Codec) Track.codec(attribute), true);
    Codec<Timeline> REGISTRY_CODEC = StructCodec.struct(
            "period_ticks", Codec.INT.optional(), Timeline::periodTicks,
            "tracks", TRACKS_CODEC.optional(Map.of()), Timeline::tracks,
            Timeline::create);

    NetworkBuffer.Type<RegistryKey<Timeline>> NETWORK_TYPE = RegistryKey.networkType(Registries::timeline);
    Codec<RegistryKey<Timeline>> CODEC = RegistryKey.codec(Registries::timeline);

    @Nullable Integer periodTicks();

    Map<EnvironmentAttribute<?>, Track<?, ?>> tracks();

    static Timeline create(@Nullable Integer periodTicks, Map<EnvironmentAttribute<?>, Track<?, ?>> tracks) {
        return new TimelineImpl(periodTicks, tracks);
    }

    // TODO: builder

    /// Creates a new registry for timelines, loading the vanilla timelines.
    ///
    /// @see net.minestom.server.MinecraftServer to get an existing instance of the registry
    @ApiStatus.Internal
    static DynamicRegistry<Timeline> createDefaultRegistry() {
        return DynamicRegistry.create(Key.key("timeline"),
                REGISTRY_CODEC, RegistryData.Resource.TIMELINES);
    }

    record Track<T, Arg>(
            Modifier<T, Arg> modifier,
            List<Keyframe<Arg>> keyframes,
            EaseFunction ease
    ) {
        public static <T, Arg> Codec<Track<T, Arg>> codec(EnvironmentAttribute<T> attribute) {
            //noinspection unchecked
            return attribute.type().modifierCodec().unionType("modifier",
                    modifier -> fullCodec((Modifier<T, Arg>) modifier), Track::modifier);
        }

        private static <T, Arg> StructCodec<Track<T, Arg>> fullCodec(Modifier<T, Arg> modifier) {
            var keyframesCodec = Keyframe.codec(modifier.argumentCodec()).list();
            return StructCodec.struct(
                    "keyframes", keyframesCodec, Track::keyframes,
                    "ease", EaseFunction.CODEC.optional(EaseFunction.LINEAR), Track::ease,
                    (keyframes, ease) -> new Track<>(modifier, keyframes, ease));
        }
    }

    record Keyframe<T>(int ticks, T value) {
        public static <T> Codec<Keyframe<T>> codec(Codec<T> valueCodec) {
            return StructCodec.struct(
                    "ticks", Codec.INT, Keyframe::ticks,
                    "value", valueCodec, Keyframe::value,
                    Keyframe::new);
        }
    }
}
