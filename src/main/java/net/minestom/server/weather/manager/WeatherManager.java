package net.minestom.server.weather.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.play.ChangeGameStatePacket;
import net.minestom.server.network.packet.server.play.ChangeGameStatePacket.Reason;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.weather.Weather;
import net.minestom.server.weather.Weather.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A weather manager.
 */
public abstract class WeatherManager {
    protected Weather weather;

    /**
     * Creates a new weather manager with an initial weather.
     *
     * @param initialWeather the initial weather
     */
    public WeatherManager(@Nullable Weather initialWeather) {
        this.weather = initialWeather;
    }

    /**
     * Gets the current weather.
     *
     * @return the current weather
     */
    public @NotNull Weather getWeather() {
        return this.weather;
    }

    /**
     * Sets the current weather.
     *
     * @param weather the new weather
     */
    public void setWeather(@NotNull Weather weather) {
        Objects.requireNonNull(weather, "weather");
        if (weather.hasExpired()) {
            throw new IllegalArgumentException("cannot set expired weather");
        }

        // don't send the weather packets if it's the same for the client
        if (!this.getWeather().equalsIgnoreExpiration(weather)) {
            this.sendWeatherPackets(createWeatherPackets(this.getWeather(), weather));
        }

        this.weather = weather;
    }

    /**
     * Checks if the weather has expired, resetting it to it's default if it has.
     */
    public void checkWeatherExpiration() {
        if (this.getWeather().hasExpired()) {
            this.resetWeather();
        }
    }

    /**
     * Checks if this weather manager has its own weather set.
     *
     * @return {@code true} if it does
     */
    public boolean hasWeather() {
        return this.weather != null;
    }

    /**
     * Sets the weather back to it's default.
     */
    public abstract void resetWeather();

    /**
     * Sends some packets to the appropriate players.
     *
     * @param packets the packets
     */
    protected abstract void sendWeatherPackets(@NotNull Collection<SendablePacket> packets);

    /**
     * Calculates the weather packets that need to be sent to the client in order to
     * change the weather.
     *
     * @param oldWeather the old weather
     * @param newWeather the new weather
     * @return the resulting packets
     */
    public static @NotNull Collection<SendablePacket> createWeatherPackets(@NotNull Weather oldWeather, @NotNull Weather newWeather) {
        if (newWeather.getType() == Type.CLEAR) {
            return List.of(new ChangeGameStatePacket(Reason.END_RAINING, 0),
                    new ChangeGameStatePacket(Reason.RAIN_LEVEL_CHANGE, 0),
                    new ChangeGameStatePacket(Reason.THUNDER_LEVEL_CHANGE, 0));
        } else {
            final List<SendablePacket> packets = new ArrayList<>(3);

            if (oldWeather.getType() == Type.CLEAR) {
                packets.add(new ChangeGameStatePacket(Reason.BEGIN_RAINING, 0));
                packets.add(new ChangeGameStatePacket(Reason.RAIN_LEVEL_CHANGE, newWeather.getRainStrength()));
                packets.add(new ChangeGameStatePacket(Reason.THUNDER_LEVEL_CHANGE, newWeather.getThunderStrength()));
            } else {
                if (!MathUtils.equals(oldWeather.getRainStrength(), newWeather.getRainStrength())) {
                    packets.add(new ChangeGameStatePacket(Reason.RAIN_LEVEL_CHANGE, newWeather.getRainStrength()));
                }

                if (!MathUtils.equals(oldWeather.getThunderStrength(), newWeather.getThunderStrength())) {
                    packets.add(new ChangeGameStatePacket(Reason.THUNDER_LEVEL_CHANGE, newWeather.getThunderStrength()));
                }
            }

            return packets;
        }
    }
}
