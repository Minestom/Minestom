package net.minestom.server.event.permission;

import net.minestom.server.event.trait.PermissionEvent;
import net.minestom.server.permission.Permission;
import net.minestom.server.permission.PermissionHandler;

/**
 * Called on {@link PermissionHandler#hasPermission(Permission)}
 */
public class PermissionCheckEvent implements PermissionEvent {
    private final PermissionHandler permissionHandler;
    private final Permission permission;
    private Result result;

    /**
     * Creates a new permission check event with the given permission.
     *
     * @param permissionHandler the permission handler
     * @param permission        the permission
     */
    public PermissionCheckEvent(PermissionHandler permissionHandler, Permission permission) {
        this.permissionHandler = permissionHandler;
        this.permission = permission;
        this.result = Result.DEFAULT;
    }

    /**
     * Gets the permission.
     *
     * @return the permission
     */
    public Permission getPermission() {
        return permission;
    }

    @Override
    public PermissionHandler getPermissionHandler() {
        return permissionHandler;
    }

    @Override
    public Result getResult() {
        return result;
    }

    @Override
    public void setResult(Result result) {
        this.result = result;
    }
}
