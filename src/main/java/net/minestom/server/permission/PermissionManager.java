package net.minestom.server.permission;

import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.permission.PermissionRegisterEvent;
import net.minestom.server.event.permission.PermissionUnregisterEvent;
import net.minestom.server.event.permission.PermissionVerifyEvent;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PermissionManager {

    private final Map<String, PermissionDescriptor> permissions = new ConcurrentHashMap<>();

    public boolean verify(PermissionHandler permissionHandler, String permission) {
        PermissionDescriptor permissionDescriptor = permissions.get(permission.toLowerCase(Locale.ROOT));
        if (permissionDescriptor == null) return false;
        boolean toggled = permissionDescriptor.getPermissionDefault().test(permissionHandler);
        PermissionVerifyEvent verifyEvent = new PermissionVerifyEvent(permissionDescriptor, permissionHandler, !toggled);
        EventDispatcher.call(verifyEvent);
        return !verifyEvent.isCancelled();
    }

    public void registerPermission(PermissionDescriptor permission) {
        EventDispatcher.call(new PermissionRegisterEvent(permission));
        permissions.put(permission.getPermissionName().toLowerCase(Locale.ROOT), permission);
    }

    public void unregisterPermission(String permission) {
        PermissionDescriptor permissionDescriptor = permissions.remove(permission);
        EventDispatcher.call(new PermissionUnregisterEvent(permissionDescriptor));
    }
}
