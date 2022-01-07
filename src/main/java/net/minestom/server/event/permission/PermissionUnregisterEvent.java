package net.minestom.server.event.permission;

import net.minestom.server.event.trait.PermissionEvent;
import net.minestom.server.permission.PermissionDescriptor;

/**
 * @author Jenya705
 */
public class PermissionUnregisterEvent implements PermissionEvent {

    private final PermissionDescriptor permission;

    public PermissionUnregisterEvent(PermissionDescriptor permission) {
        this.permission = permission;
    }

    @Override
    public PermissionDescriptor getPermission() {
        return permission;
    }

}
