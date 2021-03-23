package net.minestom.server.permission.verifier;

import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Permission verifier that returns true regardless of context.
 */
public class AllPermissionVerifier implements PermissionVerifier {
    @Override
    public boolean isValid(@NotNull Permission permission, @NotNull Set<Permission> currentPermissions) {
        return true;
    }
}
