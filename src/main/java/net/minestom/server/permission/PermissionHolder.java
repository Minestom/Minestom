package net.minestom.server.permission;

import org.jetbrains.annotations.NotNull;

public interface PermissionHolder {
    @NotNull PermissionHandler getPermissionHandler();
}