package net.minestom.server.entity;

import java.util.UUID;

public interface OfflinePlayer {
    boolean isOnline();

    String getUsername();

    UUID getUuid();

    PlayerProfile getPlayerProfile();
    void setPlayerProfile(PlayerProfile playerProfile);
}
