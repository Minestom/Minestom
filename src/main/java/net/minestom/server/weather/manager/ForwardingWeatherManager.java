package net.minestom.server.weather.manager;

import net.minestom.server.weather.Weather;
import net.minestom.server.weather.container.WeatherContainer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * A weather manager that either has a weather or has the weather of it's parent.
 */
public abstract class ForwardingWeatherManager extends WeatherManager {
    private final Supplier<WeatherContainer> parent;

    /**
     * Creates a new forwarding weather manager with a given parent.
     */
    public ForwardingWeatherManager(@NotNull WeatherContainer parent) {
        super(null);
        Objects.requireNonNull(parent, "parent");
        this.parent = () -> parent;
    }

    /**
     * Creates a new forwarding weather manager with a given parent.
     */
    public ForwardingWeatherManager(@NotNull Supplier<WeatherContainer> parent) {
        super(null);
        this.parent = Objects.requireNonNull(parent, "parent");
    }

    /**
     * Gets the parent container of this manager.
     *
     * @return the parent container
     */
    public @NotNull WeatherContainer getParentContainer() {
        return this.parent.get();
    }

    /**
     * Checks if this weather manager has its own weather set.
     *
     * @return {@code true} if it does
     */
    public boolean hasWeather() {
        return this.weather != null;
    }

    @Override
    public void checkWeatherExpiration() {
        if (this.weather != null && this.weather.hasExpired()) {
            this.resetWeather();
        }
    }

    @Override
    public @NotNull Weather getWeather() {
        return this.hasWeather() ? this.weather : this.getParentContainer().getWeather();
    }

    @Override
    public void resetWeather() {
        if (this.weather != null) {
            this.sendWeatherPackets(WeatherManager.createWeatherPackets(this.weather, this.getParentContainer().getWeather()));
            this.weather = null;
        }
    }
}
