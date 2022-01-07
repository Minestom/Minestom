package net.minestom.server.permission;

public interface PermissionHandler {

    /**
     * Checks if permission handler has given permission
     * @param permission the name of permission
     * @return true if has permission otherwise false
     */
    boolean hasPermission(String permission);

}
