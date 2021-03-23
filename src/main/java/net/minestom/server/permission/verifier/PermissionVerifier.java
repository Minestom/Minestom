package net.minestom.server.permission.verifier;

import net.minestom.server.permission.Permission;
import net.minestom.server.permission.PermissionHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.Set;

/**
 * Interface used to check if a {@link Permission} is correct
 */
@FunctionalInterface
public interface PermissionVerifier {

    /**
     * Called when using {@link PermissionHandler#hasPermission(String)}.
     *
     * @param permission the permission to check for
     * @param currentPermissions the permissions this PermissionHandler currently has.
     *
     * @return true if {@link PermissionHandler#hasPermission(String)}
     * should return true if this permission is valid, false otherwise
     */
    boolean isValid(@NotNull Permission permission, @NotNull Set<Permission> currentPermissions);

    /**
     * Shorthand for using a permission verifier lambda to check against a permission.
     *
     * @param verifier The verifier to check against.
     * @param permission         the permission to check for
     * @param currentPermissions the permissions this PermissionHandler currently has.
     * @return true if {@link PermissionHandler#hasPermission(String)}
     * should return true if this permission is valid, false otherwise
     */
    static boolean verifierValid(
            @NotNull PermissionVerifier verifier,
            @NotNull Permission permission,
            @NotNull Set<Permission> currentPermissions) {
        return verifier.isValid(permission, currentPermissions);
    }
}
