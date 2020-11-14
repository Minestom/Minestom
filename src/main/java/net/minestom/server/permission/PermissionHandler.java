package net.minestom.server.permission;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Represents an object which can have permissions.
 */
public interface PermissionHandler {

    /**
     * Returns all permissions associated to this command sender.
     * The returned collection should be modified only by subclasses.
     *
     * @return the permissions of this command sender.
     */
    @NotNull
    Collection<Permission> getAllPermissions();

    /**
     * Adds a {@link Permission} to this commandSender
     *
     * @param permission the permission to add
     */
    default void addPermission(@NotNull Permission permission) {
        getAllPermissions().add(permission);
    }

    /**
     * Removes a {@link Permission} from this commandSender
     *
     * @param permission the permission to remove
     */
    default void removePermission(@NotNull Permission permission) {
        getAllPermissions().remove(permission);
    }

    /**
     * Checks if the given {@link Permission} is possessed by this command sender.
     * Simple shortcut to <pre>getAllPermissions().contains(permission) &amp;&amp; permission.isValidFor(this)</pre> for readability.
     *
     * @param p permission to check against
     * @return true if the sender has the permission and validate {@link Permission#isValidFor(PermissionHandler, Object)}
     */
    default boolean hasPermission(@NotNull Permission p) {
        return hasPermission(p, null);
    }

    default <T> boolean hasPermission(@NotNull Permission<T> p, @Nullable T data) {
        return getAllPermissions().contains(p) && p.isValidFor(this, data);
    }

    /**
     * Checks if the given {@link Permission} is possessed by this command sender.
     * Will call {@link Permission#isValidFor(PermissionHandler, Object)} on all permissions that are an instance of {@code permissionClass}.
     * If no matching permission is found, this result returns false.
     *
     * @param permissionClass the permission class to check
     * @return true if the sender has the permission and validate {@link Permission#isValidFor(PermissionHandler, Object)}
     * @see #getAllPermissions()
     */
    default boolean hasPermission(@NotNull Class<? extends Permission> permissionClass) {
        boolean result = true;
        boolean foundPerm = false;
        for (Permission p : getAllPermissions()) {
            if (permissionClass.isInstance(p)) {
                foundPerm = true;
                result &= p.isValidFor(this, null);
            }
        }
        if (!foundPerm)
            return false;
        return result;
    }

    default <T> boolean hasPermission(@NotNull Class<? extends Permission<T>> permissionClass, @Nullable T data) {
        boolean result = true;
        boolean foundPerm = false;
        for (Permission p : getAllPermissions()) {
            if (permissionClass.isInstance(p)) {
                foundPerm = true;
                result &= p.isValidFor(this, data);
            }
        }
        if (!foundPerm)
            return false;
        return result;
    }

}
