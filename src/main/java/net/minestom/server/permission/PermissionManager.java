package net.minestom.server.permission;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PermissionManager {
    private final Map<String, PermissionDescriptor> permissionMap = new HashMap<>();

    public void registerPermission(@NotNull PermissionDescriptor permissionDescriptor) {
        permissionMap.put(permissionDescriptor.getName(), permissionDescriptor);
    }

    public void unregisterPermission(@NotNull String permission) {
        permissionMap.remove(permission);
    }

    public @NotNull Set<@NotNull String> getPermissionStrings() {
        return Collections.unmodifiableSet(permissionMap.keySet());
    }

    public @NotNull Collection<@NotNull PermissionDescriptor> getPermissionDescriptors() {
        return Collections.unmodifiableCollection(permissionMap.values());
    }

}
