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
import net.minestom.server.utils.Either;
import net.minestom.server.utils.validate.Check;
import net.minestom.server.world.clock.ClockTimeMarker;
import net.minestom.server.world.clock.WorldClock;
import net.minestom.server.world.attribute.EnvironmentAttribute;
import net.minestom.server.world.attribute.EnvironmentAttribute.Modifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public sealed interface Timeline extends Timelines permits TimelineImpl {
    @SuppressWarnings({"unchecked", "rawtypes"})
    Codec<Map<EnvironmentAttribute<?>, Track<?, ?>>> TRACKS_CODEC = EnvironmentAttribute.CODEC
            .mapValueTyped(attribute -> (Codec) Track.codec(attribute), true);
    Codec<Timeline> REGISTRY_CODEC = StructCodec.struct(
            "clock", WorldClock.CODEC, Timeline::clock,
            "period_ticks", Codec.INT.optional(), Timeline::periodTicks,
            "tracks", TRACKS_CODEC.optional(Map.of()), Timeline::tracks,
            "time_markers", ClockTimeMarker.CODEC.mapValue(TimeMarkerInfo.REGISTRY_CODEC).optional(Map.of()), Timeline::timeMarkers,
            Timeline::create);

    NetworkBuffer.Type<RegistryKey<Timeline>> NETWORK_TYPE = RegistryKey.networkType(Registries::timeline);
    Codec<RegistryKey<Timeline>> CODEC = RegistryKey.codec(Registries::timeline);

    RegistryKey<WorldClock> clock();

    @Nullable Integer periodTicks();

    Map<EnvironmentAttribute<?>, Track<?, ?>> tracks();

    Map<RegistryKey<ClockTimeMarker>, Timeline.TimeMarkerInfo> timeMarkers();

    static Timeline create(RegistryKey<WorldClock> clock, @Nullable Integer periodTicks, Map<EnvironmentAttribute<?>, Track<?, ?>> tracks, Map<RegistryKey<ClockTimeMarker>, Timeline.TimeMarkerInfo> timeMarkers) {
        return new TimelineImpl(clock, periodTicks, tracks, timeMarkers);
    }

    static Builder builder() {
        return new Builder();
    }

    /// Creates a new registry for timelines, loading the vanilla timelines.
    ///
    /// @see net.minestom.server.MinecraftServer to get an existing instance of the registry
    @ApiStatus.Internal
    static DynamicRegistry<Timeline> createDefaultRegistry(Registries registries) {
        return DynamicRegistry.create(Key.key("timeline"),
                REGISTRY_CODEC, registries, RegistryData.Resource.TIMELINES);
    }

    record Track<T, Arg>(
            Modifier<T, Arg> modifier,
            List<Keyframe<Arg>> keyframes,
            EaseFunction ease
    ) {
        public static <T, Arg> Codec<Track<T, Arg>> codec(EnvironmentAttribute<T> attribute) {
            //noinspection unchecked
            return attribute.type().modifierCodec().optional(new Modifier.Override<>(attribute.valueCodec()))
                    .unionType("modifier", modifier -> fullCodec((Modifier<T, Arg>) modifier), Track::modifier);
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

    record TimeMarkerInfo(int ticks, boolean showInCommands) {
        public static final Codec<TimeMarkerInfo> CODEC = StructCodec.struct(
                "ticks", Codec.INT, TimeMarkerInfo::ticks,
                "show_in_commands", Codec.BOOLEAN.optional(false), TimeMarkerInfo::showInCommands,
                TimeMarkerInfo::new);
        public static final Codec<TimeMarkerInfo> REGISTRY_CODEC = Codec.Either(Codec.INT, CODEC).transform(
                it -> it.unify(TimeMarkerInfo::new, Function.identity()),
                it -> !it.showInCommands() ? Either.left(it.ticks()) : Either.right(it));

        public TimeMarkerInfo(int ticks) {
            this(ticks, false);
        }

        public TimeMarkerInfo {
            Check.argCondition(ticks < 0, "ticks must be positive");
        }

        public ClockTimeMarker clockTimeMarker(Timeline timeline) {
            return ClockTimeMarker.create(timeline.clock(), ticks(), timeline.periodTicks(), showInCommands());
        }
    }

    final class Builder {
        private @UnknownNullability RegistryKey<WorldClock> clock;
        private @Nullable Integer periodTicks;
        private final Map<EnvironmentAttribute<?>, Track<?, ?>> tracks = new HashMap<>();
        private final Map<RegistryKey<ClockTimeMarker>, Timeline.TimeMarkerInfo> timeMarkers = new HashMap<>();

        public Builder clock(RegistryKey<WorldClock> clock) {
            this.clock = clock;
            return this;
        }

        public Builder periodTicks(int ticks) {
            this.periodTicks = ticks;
            return this;
        }

        public Builder periodTicks(@Nullable Integer ticks) {
            this.periodTicks = ticks;
            return this;
        }

        public <T, Arg> Builder track(EnvironmentAttribute<T> attribute, Track<T, Arg> track) {
            tracks.put(attribute, track);
            return this;
        }

        public Builder tracks(Map<EnvironmentAttribute<?>, Track<?, ?>> tracks) {
            this.tracks.putAll(tracks);
            return this;
        }

        public Builder timeMarker(RegistryKey<ClockTimeMarker> key, Timeline.TimeMarkerInfo info) {
            timeMarkers.put(key, info);
            return this;
        }

        public Builder timeMarkers(Map<RegistryKey<ClockTimeMarker>, Timeline.TimeMarkerInfo> timeMarkers) {
            this.timeMarkers.putAll(timeMarkers);
            return this;
        }

        public Timeline build() {
            return Timeline.create(clock, periodTicks, tracks, timeMarkers);
        }

    }
}
