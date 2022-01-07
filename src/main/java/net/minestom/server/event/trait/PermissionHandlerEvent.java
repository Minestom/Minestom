package net.minestom.server.event.trait;

import net.minestom.server.event.Event;
import net.minestom.server.permission.PermissionHandler;

/**
 * Represents any event targeting {@link PermissionHandler}
 */
public interface PermissionHandlerEvent extends Event {

    PermissionHandler getPermissionHandler();

}
