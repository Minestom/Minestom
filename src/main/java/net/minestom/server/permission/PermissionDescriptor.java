package net.minestom.server.permission;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class PermissionDescriptor {

    private final String permissionName;
    private final PermissionDefault permissionDefault;

    /**
     * Creates a new permission object with optional permission default
     *
     * @param permissionName    the name of permission
     * @param permissionDefault the default behaviour of permission
     */
    public PermissionDescriptor(@NotNull String permissionName, @Nullable PermissionDefault permissionDefault) {
        this.permissionName = permissionName;
        this.permissionDefault = Objects.requireNonNullElse(permissionDefault, PermissionDefault.TRUE);
    }

    /**
     * Creates a new permission object without additional data
     *
     * @param permissionName the name of the permission
     */
    public PermissionDescriptor(@NotNull String permissionName) {
        this(permissionName, null);
    }

    /**
     * Gets the name of the permission.
     *
     * @return the permission name
     */
    @NotNull
    public String getPermissionName() {
        return permissionName;
    }

    /**
     * Gets the permission default
     *
     * @return the default permission behaviour
     */
    @NotNull
    public PermissionDefault getPermissionDefault() {
        return permissionDefault;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass() || o.hashCode() != hashCode()) return false;
        PermissionDescriptor that = (PermissionDescriptor) o;
        return permissionName.equals(that.permissionName);
    }

    @Override
    public int hashCode() {
        return permissionName.hashCode();
    }
    
}
