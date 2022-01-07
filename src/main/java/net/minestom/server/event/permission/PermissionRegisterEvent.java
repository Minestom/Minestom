package net.minestom.server.event.permission;

import net.minestom.server.event.trait.PermissionEvent;
import net.minestom.server.permission.PermissionDescriptor;

public class PermissionRegisterEvent implements PermissionEvent {

    private final PermissionDescriptor permission;

    public PermissionRegisterEvent(PermissionDescriptor permission) {
        this.permission = permission;
    }

    @Override
    public PermissionDescriptor getPermission() {
        return permission;
    }
}
