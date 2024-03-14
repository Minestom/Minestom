package net.minestom.server.permission;

import org.jetbrains.annotations.NotNull;

public interface PermissionHandlerProxy extends PermissionHandler {
    @NotNull
    PermissionHandler getPermissionHandler();

    @Override
    default PermissionTristate checkPermission(@NotNull String permission) {
        return getPermissionHandler().checkPermission(permission);
    }

    @Override
    default void setPermission(@NotNull String permission, PermissionTristate tristate) {
        getPermissionHandler().setPermission(permission, tristate);
    }

    @Override
    default void removePermission(@NotNull String permissionName) {
        getPermissionHandler().removePermission(permissionName);
    }

    @Override
    default boolean hasPermission(@NotNull String permission, boolean defaultValue) {
        return getPermissionHandler().hasPermission(permission, defaultValue);
    }

    @Override
    default boolean hasPermission(@NotNull String permission) {
        return getPermissionHandler().hasPermission(permission);
    }

    @Override
    default void addPermission(String permission) {
        getPermissionHandler().addPermission(permission);
    }
}