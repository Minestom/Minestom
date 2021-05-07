package net.minestom.server.weather;

import net.minestom.server.weather.manager.WeatherManager;
import org.jetbrains.annotations.NotNull;

/**
 * An object that can contain some weather.
 */
public interface WeatherContainer {

    /**
     * Gets the weather manager in charge of handling the weather for this container.
     *
     * @return the weather manager
     */
    @NotNull WeatherManager getWeatherManager();

    /**
     * Gets the current weather for this object.
     *
     * @return the current weather
     */
    default @NotNull Weather getWeather() {
        return this.getWeatherManager().getWeather();
    }

    /**
     * Sets the current weather for this object.
     *
     * @param weather the new weather
     */
    default void setWeather(@NotNull Weather weather) {
        this.getWeatherManager().setWeather(weather);
    }

    /**
     * Resets the weather for this object back to the default.
     */
    default void resetWeather() {
        this.getWeatherManager().resetWeather();
    }

    /**
     * Checks if this weather container has weather set that is overriding the weather of
     * its parent container, it is has one.
     *
     * @return {@code true} if it does
     */
    default boolean hasWeather() {
        return this.getWeatherManager().hasWeather();
    }
}
