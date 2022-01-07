package net.minestom.server.event.permission;

import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PermissionEvent;
import net.minestom.server.event.trait.PermissionHandlerEvent;
import net.minestom.server.permission.PermissionDescriptor;
import net.minestom.server.permission.PermissionHandler;

public class PermissionVerifyEvent implements PermissionHandlerEvent, CancellableEvent, PermissionEvent {

    private final PermissionDescriptor permission;
    private final PermissionHandler permissionHandler;

    private boolean cancelled;

    public PermissionVerifyEvent(PermissionDescriptor permission, PermissionHandler permissionHandler, boolean cancelled) {
        this.permission = permission;
        this.permissionHandler = permissionHandler;
        this.cancelled = cancelled;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public PermissionDescriptor getPermission() {
        return permission;
    }

    @Override
    public PermissionHandler getPermissionHandler() {
        return permissionHandler;
    }
}
