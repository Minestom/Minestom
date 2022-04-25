package net.minestom.server.permission;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * A permission handler that wraps another permission handler
 * ForwardingPermissionHandler is designed to easily allow users or implementations wrap an existing PermissionHandler
 *
 * @see net.minestom.server.permission.PermissionHandler
 */
public interface ForwardingPermissionHandler extends PermissionHandler {

    @NotNull PermissionHandler getPermissionHandler();

    @Override
    default @NotNull Set<Permission> getAllPermissions() {
        return this.getPermissionHandler().getAllPermissions();
    }

    @Override
    default void addPermission(@NotNull Permission permission) {
        this.getPermissionHandler().addPermission(permission);
    }

    @Override
    default void removePermission(@NotNull Permission permission) {
        this.getPermissionHandler().removePermission(permission);
    }

    @Override
    default void removePermission(@NotNull String permissionName) {
        this.getPermissionHandler().removePermission(permissionName);
    }

    @Override
    default boolean hasPermission(@NotNull String permissionName) {
        return this.getPermissionHandler().hasPermission(permissionName);
    }

    @Override
    default boolean hasPermission(@NotNull Permission permission) {
        return this.getPermissionHandler().hasPermission(permission);
    }

    @Override
    default boolean hasPermission(@NotNull String permissionName, @Nullable PermissionVerifier permissionVerifier) {
        return this.getPermissionHandler().hasPermission(permissionName, permissionVerifier);
    }

    @Override
    @Nullable
    default Permission getPermission(@NotNull String permissionName) {
        return this.getPermissionHandler().getPermission(permissionName);
    }

}
