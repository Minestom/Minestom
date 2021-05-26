package net.minestom.server.extras.lan;

import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.server.utils.time.UpdateOption;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Configuration for opening the server to LAN.
 *
 * @see OpenToLAN#open(OpenToLANConfig)
 */
public class OpenToLANConfig {
    int port;
    UpdateOption delayBetweenPings, delayBetweenEvent;

    /**
     * Creates a new config with the port set to random and the delay between pings set
     * to 1.5 seconds and the delay between event calls set to 30 seconds.
     */
    public OpenToLANConfig() {
        this.port = 0;
        this.delayBetweenPings = new UpdateOption(1500, TimeUnit.MILLISECOND);
        this.delayBetweenEvent = new UpdateOption(30, TimeUnit.SECOND);
    }

    /**
     * Sets the port used to send pings from. Use {@code 0} to pick a random free port.
     *
     * @param port the port
     * @return {@code this}, for chaining
     */
    @Contract("_ -> this")
    public @NotNull OpenToLANConfig port(int port) {
        this.port = port;
        return this;
    }

    /**
     * Sets the delay between outgoing pings.
     *
     * @param delay the delay
     * @return {@code this}, for chaining
     */
    @Contract("_ -> this")
    public @NotNull OpenToLANConfig pingDelay(@NotNull UpdateOption delay) {
        this.delayBetweenPings = Objects.requireNonNull(delay, "delay");
        return this;
    }

    /**
     * Sets the delay between calls of {@link ServerListPingEvent}.
     *
     * @param delay the delay
     * @return {@code this}, for chaining
     */
    @Contract("_ -> this")
    public @NotNull OpenToLANConfig eventCallDelay(@NotNull UpdateOption delay) {
        this.delayBetweenEvent = Objects.requireNonNull(delay, "delay");
        return this;
    }
}
