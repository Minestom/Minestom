package net.minestom.server.network.template;


import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnknownNullability;

/**
 * Marker interface for network templates. Used to identify template types.
 *
 * @param <T> the type, nullable
 */
@ApiStatus.NonExtendable
public interface NetworkTemplate<T extends @UnknownNullability Object> extends NetworkBuffer.Type<T> {}
