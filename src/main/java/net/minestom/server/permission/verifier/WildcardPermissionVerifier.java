package net.minestom.server.permission.verifier;

import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Permission verifier that processes string names + wildcards.
 */
public class WildcardPermissionVerifier implements PermissionVerifier {
    @Override
    public boolean isValid(@NotNull Permission permission, @NotNull Set<Permission> currentPermissions) {

        // If this is a flat permission, skip the rest of the steps.
        if (currentPermissions.contains(permission)) {
            return true;
        }

        if (permission.getPermissionName().endsWith(".*")) {

            // Remove the last 2 characters from the permission (.*)
            String permissionPrefix = permission.getPermissionName().substring(0, permission.getPermissionName().length() - 2);

            // Loop through all permissions
            for (Permission loopPermission : currentPermissions) {

                // Make sure the permission is valid.
                if (!permissionCorrect(loopPermission.getPermissionName()))
                    continue;

                // If that looped permission starts with it (ex permissionPrefix is some.perm and this permission is some.perm.*) its valid.
                if (loopPermission.getPermissionName().startsWith(permissionPrefix))
                    return true;
            }
        }

        return false;
    }

    /**
     * Checks if a permission is in the correct format for a string permission.
     *
     * REGEX: ^(\w+\.?)+\*?$
     *
     * @param permissionName The permission to check against
     * @return If this follows the regex pattern
     */
    public boolean permissionCorrect(@NotNull String permissionName) {
        return permissionName.matches("^(\\w+\\.?)+\\*?$");
    }
}
