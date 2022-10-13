package net.minestom.server.permission;

import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The very basic implementation for PermissionHandler
 *
 * @see net.minestom.server.permission.PermissionHandler
 */
public class PermissionHandlerImpl implements PermissionHandler {

    private final Set<Permission> permissions = ConcurrentHashMap.newKeySet();

    @Override
    public @NotNull Set<Permission> getAllPermissions() {
        return permissions;
    }
}