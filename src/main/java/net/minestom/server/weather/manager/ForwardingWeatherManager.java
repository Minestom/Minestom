package net.minestom.server.weather.manager;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Supplier;
import net.minestom.server.MinecraftServer;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.weather.Weather;
import net.minestom.server.weather.WeatherContainer;
import org.jetbrains.annotations.NotNull;

/**
 * A weather manager that either has a weather or has the weather of it's parent.
 */
public class ForwardingWeatherManager extends WeatherManager {
    private final Supplier<? extends WeatherContainer> parent;
    private final Supplier<Collection<? extends WeatherContainer>> children;

    /**
     * Creates a new forwarding weather manager with children.
     *
     * @param children the children
     */
    public ForwardingWeatherManager(@NotNull Supplier<Collection<? extends WeatherContainer>> children) {
        this(MinecraftServer::getGlobalWeatherManager, children);
    }

    /**
     * Creates a new forwarding weather manager with a parent and children.
     *
     * @param parent the parent
     * @param children the children
     */
    public ForwardingWeatherManager(@NotNull Supplier<? extends WeatherContainer> parent, @NotNull Supplier<Collection<? extends WeatherContainer>> children) {
        super(null);
        this.parent = Objects.requireNonNull(parent, "parent");
        this.children = Objects.requireNonNull(children, "children");
    }

    /**
     * Gets the parent container of this manager.
     *
     * @return the parent container
     */
    public @NotNull WeatherContainer getParentContainer() {
        return this.parent.get();
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

    @Override
    protected void sendWeatherPackets(@NotNull Collection<SendablePacket> packets) {
        if (!packets.isEmpty()) {
            for (WeatherContainer weatherContainer : this.children.get()) {
                weatherContainer.getWeatherManager().sendWeatherPackets(packets);
            }
        }
    }
}
