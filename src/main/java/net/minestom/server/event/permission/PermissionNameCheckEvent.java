package net.minestom.server.event.permission;

import net.minestom.server.event.trait.PermissionEvent;
import net.minestom.server.permission.PermissionHandler;
import net.minestom.server.permission.PermissionVerifier;

/**
 * Called on {@link PermissionHandler#hasPermission(String, PermissionVerifier)}
 */
public class PermissionNameCheckEvent implements PermissionEvent {
    private final PermissionHandler permissionHandler;
    private final String permissionName;
    private final PermissionVerifier verifier;
    private Result result;

    /**
     * Creates a new permission check event with the given permission & verifier.
     *
     * @param permissionHandler the permission handler
     * @param permissionName    the permission name
     * @param verifier          the verifier
     */
    public PermissionNameCheckEvent(PermissionHandler permissionHandler, String permissionName, PermissionVerifier verifier) {
        this.permissionHandler = permissionHandler;
        this.permissionName = permissionName;
        this.verifier = verifier;
        this.result = Result.DEFAULT;
    }

    /**
     * Gets the permission name.
     *
     * @return the permission name
     */
    public String getPermissionName() {
        return permissionName;
    }

    /**
     * Gets the verifier.
     *
     * @return the verifier
     */
    public PermissionVerifier getVerifier() {
        return verifier;
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
