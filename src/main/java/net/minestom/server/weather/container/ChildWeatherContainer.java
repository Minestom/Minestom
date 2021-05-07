package net.minestom.server.weather.container;

import net.minestom.server.weather.manager.ForwardingWeatherManager;
import org.jetbrains.annotations.NotNull;

/**
 * A special weather container that has its own weather or the weather of its parent.
 */
public interface ChildWeatherContainer extends WeatherContainer {

    /**
     * {@inheritDoc}
     */
    @NotNull ForwardingWeatherManager getWeatherManager();

    /**
     * Checks if this weather container has weather set that is overriding the weather of
     * its parent container.
     *
     * @return {@code true} if it does
     */
    default boolean hasWeather() {
        return this.getWeatherManager().hasWeather();
    }
}
