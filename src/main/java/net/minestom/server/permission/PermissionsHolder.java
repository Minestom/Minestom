package net.minestom.server.permission;

import org.jetbrains.annotations.NotNull;

public interface PermissionsHolder {
    @NotNull Permissions getPermissions();
}