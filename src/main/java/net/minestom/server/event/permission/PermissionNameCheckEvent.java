package net.minestom.server.event.permission;

import net.minestom.server.event.trait.PermissionEvent;
import net.minestom.server.permission.PermissionHandler;
import net.minestom.server.permission.PermissionVerifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called on {@link PermissionHandler#hasPermission(String, PermissionVerifier)}
 */
public class PermissionNameCheckEvent implements PermissionEvent {
    private final PermissionHandler permissionHandler;
    private final String permissionName;
    private final PermissionVerifier verifier;
    private Result result;

    /**
     * Creates a new permission check event with the given permission and verifier.
     *
     * @param permissionHandler the permission handler
     * @param permissionName    the permission name
     * @param verifier          the verifier
     */
    public PermissionNameCheckEvent(@NotNull PermissionHandler permissionHandler, @NotNull String permissionName, @Nullable PermissionVerifier verifier) {
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
    public @NotNull String getPermissionName() {
        return permissionName;
    }

    /**
     * Gets the verifier.
     *
     * @return the verifier
     */
    public @Nullable PermissionVerifier getVerifier() {
        return verifier;
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
