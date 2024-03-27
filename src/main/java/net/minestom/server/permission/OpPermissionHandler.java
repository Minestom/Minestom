package net.minestom.server.permission;

import org.jetbrains.annotations.NotNull;

public class OpPermissionHandler implements PermissionHandler {
    @Override
    public PermissionTristate checkPermission(@NotNull String permission) {
        return PermissionTristate.TRUE;
    }

    @Override
    public void setPermission(@NotNull String permission, PermissionTristate tristate) {
    }
}