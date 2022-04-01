package net.minestom.server.resourcepack

import net.minestom.server.resourcepack.ResourcePack

/**
 * Represents the result of [Player.setResourcePack] in
 * [net.minestom.server.event.player.PlayerResourcePackStatusEvent].
 */
enum class ResourcePackStatus {
    SUCCESS, DECLINED, FAILED_DOWNLOAD, ACCEPTED
}