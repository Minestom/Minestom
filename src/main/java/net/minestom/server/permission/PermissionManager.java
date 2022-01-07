package net.minestom.server.permission;

public interface PermissionManager {

    boolean verify(PermissionHandler sender, String permission);

    void registerPermission(PermissionDescriptor permission);

    void unregisterPermission(String permission);

    default void unregisterPermission(Permission permission) {
        unregisterPermission(permission);
    }

}
