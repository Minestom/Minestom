package net.minestom.server.permission;

import org.jetbrains.annotations.NotNull;

/**
 * Represents an object which can have permissions.
 * <p>
 * Permissions are in-memory only by default.
 * You have however the capacity to store them persistently as the {link Permission} object
 * is serializer-friendly, {link Permission#getPermissionName()} being a {@link String}
 * and {link Permission#getNBTData()} serializable into a string using {link NBTCompound#toSNBT()}
 * and deserialized back with {link SNBTParser#parse()}.
 */
public interface Permissions {
    PermissionTristate checkPermission(@NotNull String permission);

    void setPermission(@NotNull String permission, PermissionTristate tristate);

    /**
     * Removes a {link Permission} based on its string identifier.
     *
     * @param permissionName the permission name
     */
    default void removePermission(@NotNull String permissionName) {
        setPermission(permissionName, PermissionTristate.UNDEFINED);
    }

    /**
     * Gets if this handler has the permission {@code permission}.
     * This method will also pattern match for wildcards. For example, if this handler has the permission {@code "*"}, this method will always return true.
     * However, if this handler has the permission {@code "foo.b*r.baz"}, this method will return true if {@code permission} is {@code "foo.baaar.baz"} or {@code "foo.br.baz}, but not {@code "foo.bar.bz"}.
     * <p>
     * Uses {link Permission#equals(Object)} internally.
     *
     * @param permission the permission to check
     * @return true if the handler has the permission, false otherwise
     */
    default boolean hasPermission(@NotNull String permission, boolean defaultValue) {
        return checkPermission(permission).asBoolean(defaultValue);
    }

    /**
     * Gets if this handler has the permission {@code permission}.
     * This method will also pattern match for wildcards. For example, if this handler has the permission {@code "*"}, this method will always return true.
     * However, if this handler has the permission {@code "foo.b*r.baz"}, this method will return true if {@code permission} is {@code "foo.baaar.baz"} or {@code "foo.br.baz}, but not {@code "foo.bar.bz"}.
     * <p>
     * Uses {link Permission#equals(Object)} internally.
     *
     * @param permission the permission to check
     * @return true if the handler has the permission, false otherwise
     */
    default boolean hasPermission(@NotNull String permission) {
        return checkPermission(permission).asBoolean();
    }

    /**
     * Adds a {link Permission} to this handler.
     *
     * @param permission the permission to add
     */
    default void addPermission(String permission) {
        setPermission(permission, PermissionTristate.TRUE);
    }
}