package net.minestom.server.resourcepack;

import net.minestom.server.entity.Player;

/**
 * Represents the result of {@link Player#setResourcePack(ResourcePack)} in
 * {@link net.minestom.server.event.player.PlayerResourcePackStatusEvent}.
 */
public enum ResourcePackStatus {

    SUCCESSFULLY_LOADED,
    DECLINED,
    FAILED_DOWNLOAD,
    ACCEPTED,
    DOWNLOADED,
    INVALID_URL,
    FAILED_RELOAD,
    DISCARDED,
}
