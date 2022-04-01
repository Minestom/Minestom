package net.minestom.server.permission

import org.jglrxavpok.hephaistos.nbt.NBTCompound
import net.minestom.server.permission.PermissionVerifier

/**
 * Represents an object which can have permissions.
 *
 *
 * Permissions are in-memory only by default.
 * You have however the capacity to store them persistently as the [Permission] object
 * is serializer-friendly, [Permission.getPermissionName] being a [String]
 * and [Permission.getNBTData] serializable into a string using [NBTCompound.toSNBT]
 * and deserialized back with [SNBTParser.parse].
 */
interface PermissionHandler {
    /**
     * Returns all permissions associated to this handler.
     * The returned collection should be modified only by subclasses.
     *
     * @return the permissions of this handler.
     */
    val allPermissions: MutableSet<Permission>

    /**
     * Adds a [Permission] to this handler.
     *
     * @param permission the permission to add
     */
    fun addPermission(permission: Permission) {
        allPermissions.add(permission)
    }

    /**
     * Removes a [Permission] from this handler.
     *
     * @param permission the permission to remove
     */
    fun removePermission(permission: Permission) {
        allPermissions.remove(permission)
    }

    /**
     * Removes a [Permission] based on its string identifier.
     *
     * @param permissionName the permission name
     */
    fun removePermission(permissionName: String) {
        allPermissions.removeIf { permission: Permission -> permission.permissionName == permissionName }
    }

    /**
     * Gets if this handler has the permission `permission`.
     *
     *
     * Uses [Permission.equals] internally.
     *
     * @param permission the permission to check
     * @return true if the handler has the permission, false otherwise
     */
    fun hasPermission(permission: Permission): Boolean {
        for (permissionLoop in allPermissions) {
            if (permissionLoop == permission) {
                return true
            }
        }
        return false
    }

    /**
     * Gets the [Permission] with the name `permissionName`.
     *
     *
     * Useful if you want to retrieve the permission data.
     *
     * @param permissionName the permission name
     * @return the permission from its name, null if not found
     */
    fun getPermission(permissionName: String): Permission? {
        for (permission in allPermissions) {
            // Verify permission name equality
            if (permission.permissionName == permissionName) {
                return permission
            }
        }
        return null
    }
    /**
     * Gets if this handler has the permission with the name `permissionName` and which verify the optional
     * [PermissionVerifier].
     *
     * @param permissionName     the permission name
     * @param permissionVerifier the optional verifier,
     * null means that only the permission name will be used
     * @return true if the handler has the permission, false otherwise
     */
    /**
     * Gets if this handler has the permission with the name `permissionName`.
     *
     * @param permissionName the permission name
     * @return true if the handler has the permission, false otherwise
     */
    @JvmOverloads
    fun hasPermission(permissionName: String, permissionVerifier: PermissionVerifier? = null): Boolean {
        val permission = getPermission(permissionName)
        return if (permission != null) {
            // Verify using the permission verifier
            permissionVerifier == null || permissionVerifier.isValid(permission.nbtData)
        } else false
    }
}