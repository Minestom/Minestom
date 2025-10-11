package net.minestom.server.entity.metadata.golem;

import net.minestom.server.codec.Codec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;

public class CopperGolemMeta extends AbstractGolemMeta {

    public CopperGolemMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public @NotNull WeatherState getWeatherState() {
        return metadata.get(MetadataDef.CopperGolem.WEATHER_STATE);
    }

    public void setWeatherState(@NotNull WeatherState weatherState) {
        metadata.set(MetadataDef.CopperGolem.WEATHER_STATE, weatherState);
    }

    public @NotNull State getState() {
        return metadata.get(MetadataDef.CopperGolem.STATE);
    }

    public void setState(@NotNull State state) {
        metadata.set(MetadataDef.CopperGolem.STATE, state);
    }

    public enum WeatherState {
        UNAFFECTED,
        EXPOSED,
        WEATHERED,
        OXIDIZED;

        public static final NetworkBuffer.Type<WeatherState> NETWORK_TYPE = NetworkBuffer.Enum(WeatherState.class);
        public static final Codec<WeatherState> CODEC = Codec.Enum(WeatherState.class);
    }

    public enum State {
        IDLE,
        GETTING_ITEM,
        GETTING_NO_ITEM,
        DROPPING_ITEM,
        DROPPING_NO_ITEM;

        public static final NetworkBuffer.Type<State> NETWORK_TYPE = NetworkBuffer.Enum(State.class);
        public static final Codec<State> CODEC = Codec.Enum(State.class);
    }
}
