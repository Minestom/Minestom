package net.minestom.server.resourcepack;

import net.minestom.server.entity.Player;

/**
 * Represents the result of {@link Player#setResourcePack(ResourcePack)} in
 * {@link net.minestom.server.event.player.PlayerResourcePackStatusEvent}.
 */
public enum ResourcePackStatus {
    SUCCESS, DECLINED, FAILED_DOWNLOAD, ACCEPTED
}
