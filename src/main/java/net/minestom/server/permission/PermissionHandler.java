package net.minestom.server.permission;

import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.parser.SNBTParser;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Represents an object which can have permissions.
 * <p>
 * Permissions are in-memory only by default.
 * You have however the capacity to store them persistently as the {link Permission} object
 * is serializer-friendly, {link Permission#getPermissionName()} being a {@link String}
 * and {link Permission#getNBTData()} serializable into a string using {@link NBTCompound#toSNBT()}
 * and deserialized back with {@link SNBTParser#parse()}.
 */
public interface PermissionHandler {

    /**
     * Returns all permissions associated to this handler.
     * The returned collection should be modified only by subclasses.
     *
     * @return the permissions of this handler.
     */
    @NotNull
    Set<String> getAllPermissions();

    /**
     * Adds a {link Permission} to this handler.
     *
     * @param permission the permission to add
     */
    default void addPermission(@NotNull String permission) {
        getAllPermissions().add(permission);
    }

    /**
     * Removes a {link Permission} based on its string identifier.
     *
     * @param permissionName the permission name
     */
    default void removePermission(@NotNull String permissionName) {
        getAllPermissions().remove(permissionName);
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
        for (String permissionLoopName : getAllPermissions()) {
            if (permissionLoopName.equals(permission)) {
                return true;
            }
            if (permissionLoopName.contains("*")) {
                // Sanitize permissionLoopName
                String regexSanitized = Pattern.quote(permissionLoopName).replace("*", "\\E(.*)\\Q"); // Replace * with regex
                // pattern matching for wildcards, where foo.b*r.baz matches foo.baaaar.baz or foo.bar.baz
                if (permission.matches(regexSanitized)) {
                    return true;
                }
            }
        }
        return false;
    }
}