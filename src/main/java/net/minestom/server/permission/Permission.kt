package net.minestom.server.permission

import org.jglrxavpok.hephaistos.nbt.NBTCompound
import net.minestom.server.permission.PermissionVerifier
import java.util.*

/**
 * Representation of a permission granted to a [CommandSender].
 * Each permission has a string representation used as an identifier, and an optional
 * [NBTCompound] used to store additional data.
 *
 *
 * The class is immutable.
 */
class Permission
/**
 * Creates a new permission object without additional data
 *
 * @param permissionName the name of the permission
 */ @JvmOverloads constructor(
    /**
     * Gets the name of the permission.
     *
     * @return the permission name
     */
    val permissionName: String,
    /**
     * Gets the data associated to this permission.
     *
     * @return the nbt data of this permission, can be null if not any
     */
    val nBTData: NBTCompound? = null
) {
    /**
     * Creates a new permission object with optional data.
     *
     * @param permissionName the name of the permission
     * @param nBTData           the optional data of the permission
     */
    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as Permission
        return permissionName == that.permissionName && nBTData == that.nBTData
    }

    override fun hashCode(): Int {
        return Objects.hash(permissionName, nBTData)
    }
}