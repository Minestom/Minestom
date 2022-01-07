package net.minestom.server.permission;

import net.minestom.server.command.CommandSender;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PermissionManagerImpl implements PermissionManager {

    private final Map<String, Permission> permissions = new HashMap<>();

    @Override
    public boolean verify(CommandSender sender, String permission) {
        Permission permissionObject = permissions.get(permission.toLowerCase(Locale.ROOT));
        if (permissionObject == null) return false;
        return permissionObject.getPermissionDefault().test(sender);
    }

    @Override
    public void registerPermission(Permission permission) {
        permissions.put(permission.getPermissionName().toLowerCase(Locale.ROOT), permission);
    }
}
