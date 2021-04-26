package net.minestom.server.utils;

import java.net.InetAddress;

import java.util.Objects;

import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

/**
 * A utility class to hold an {@link InetAddress} and a port.
 */
public class InetAddressWithPort {
    private final InetAddress inetAddress;
    private final int port;

    /**
     * Creates a new {@link InetAddressWithPort}.
     *
     * @param inetAddress the inet address
     * @param port the port
     */
    public InetAddressWithPort(@NotNull InetAddress inetAddress, int port) {
        Validate.inclusiveBetween(1, 65535, port, "port must be a valid port");

        this.inetAddress = Objects.requireNonNull(inetAddress, "inetAddress");
        this.port = port;
    }

    /**
     * Gets the inet address.
     *
     * @return the inet address
     */
    public @NotNull InetAddress getInetAddress() {
        return this.inetAddress;
    }

    /**
     * Gets the port.
     *
     * @return the port
     */
    public int getPort() {
        return this.port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InetAddressWithPort that = (InetAddressWithPort) o;
        return port == that.port && Objects.equals(inetAddress, that.inetAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inetAddress, port);
    }
}
