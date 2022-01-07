package net.minestom.server.permission;

import net.minestom.server.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.Objects;

/**
 * Representation of a permission granted to a {@link CommandSender}.
 * Each permission has a string representation used as an identifier, and an optional
 * {@link NBTCompound} used to store additional data.
 * <p>
 * The class is immutable.
 */
public class Permission {

    private final String permissionName;
    private final PermissionDefault permissionDefault;
    private final NBTCompound data;

    /**
     * Creates a new permission object with optional permission default and optional data
     * @param permissionName    the name of permission
     * @param permissionDefault the default behaviour of permission
     * @param data              the optional data of the permission
     */
    public Permission(@NotNull String permissionName, @Nullable PermissionDefault permissionDefault, @Nullable NBTCompound data) {
        this.permissionName = permissionName;
        this.permissionDefault = Objects.requireNonNullElse(permissionDefault, PermissionDefault.TRUE);
        this.data = data;
    }

    /**
     * Creates a new permission object with optional data.
     *
     * @param permissionName the name of the permission
     * @param data           the optional data of the permission
     */
    public Permission(@NotNull String permissionName, @Nullable NBTCompound data) {
        this(permissionName, null, data);
    }

    /**
     * Creates a new permission object with optional permission default
     *
     * @param permissionName    the name of permission
     * @param permissionDefault the default behaviour of permission
     */
    public Permission(@NotNull String permissionName, @Nullable PermissionDefault permissionDefault) {
        this(permissionName, permissionDefault, null);
    }

    /**
     * Creates a new permission object without additional data
     *
     * @param permissionName the name of the permission
     */
    public Permission(@NotNull String permissionName) {
        this(permissionName, null, null);
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
     * Gets the data associated to this permission.
     *
     * @return the nbt data of this permission, can be null if not any
     */
    @Nullable
    public NBTCompound getNBTData() {
        return data;
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
        if (o == null || getClass() != o.getClass()) return false;
        Permission that = (Permission) o;
        return permissionName.equals(that.permissionName) && Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(permissionName, data);
    }
}
