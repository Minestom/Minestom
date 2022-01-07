package net.minestom.server.permission;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PermissionManagerImpl implements PermissionManager {

    private final Map<String, PermissionDescriptor> permissions = new ConcurrentHashMap<>();

    @Override
    public boolean verify(PermissionHandler permissionHandler, String permission) {
        PermissionDescriptor permissionObject = permissions.get(permission.toLowerCase(Locale.ROOT));
        if (permissionObject == null) return false;
        return permissionObject.getPermissionDefault().test(permissionHandler);
    }

    @Override
    public void registerPermission(PermissionDescriptor permission) {
        permissions.put(permission.getPermissionName().toLowerCase(Locale.ROOT), permission);
    }

    @Override
    public void unregisterPermission(String permission) {
        permissions.remove(permission);
    }
}
