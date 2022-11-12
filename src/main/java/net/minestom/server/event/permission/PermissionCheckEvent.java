package net.minestom.server.event.permission;

import net.minestom.server.event.trait.PermissionEvent;
import net.minestom.server.permission.Permission;
import net.minestom.server.permission.PermissionHandler;
import org.jetbrains.annotations.NotNull;

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
    public PermissionCheckEvent(@NotNull PermissionHandler permissionHandler, @NotNull Permission permission) {
        this.permissionHandler = permissionHandler;
        this.permission = permission;
        this.result = Result.DEFAULT;
    }

    /**
     * Gets the permission.
     *
     * @return the permission
     */
    public @NotNull Permission getPermission() {
        return permission;
    }

    @Override
    public @NotNull PermissionHandler getPermissionHandler() {
        return permissionHandler;
    }

    @Override
    public @NotNull Result getResult() {
        return result;
    }

    @Override
    public void setResult(@NotNull Result result) {
        this.result = result;
    }
}
