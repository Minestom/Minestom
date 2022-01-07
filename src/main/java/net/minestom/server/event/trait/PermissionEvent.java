package net.minestom.server.event.trait;

import net.minestom.server.event.Event;
import net.minestom.server.permission.PermissionDescriptor;

/**
 * Represents any event called on a {@link net.minestom.server.permission.PermissionManager}
 */
public interface PermissionEvent extends Event {

    /**
     * Returns permission descriptor
     *
     * @return Permission descriptor
     */
    PermissionDescriptor getPermission();

}
