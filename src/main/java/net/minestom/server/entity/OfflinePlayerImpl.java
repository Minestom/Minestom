package net.minestom.server.entity;

import net.minestom.server.MinecraftServer;

import java.util.UUID;

public class OfflinePlayerImpl implements OfflinePlayer {
    private final String name;
    private final UUID uuid;
    private PlayerProfile playerProfile;

    public OfflinePlayerImpl(
            final String name,
            final UUID uuid,
            final PlayerProfile playerProfile
    ) {
        this.name = name;
        this.uuid = uuid;
        this.playerProfile = playerProfile;
    }

    @Override
    public boolean isOnline() {
        return MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(getUsername()) != null;
    }

    @Override
    public String getUsername() {
        return this.name;
    }

    @Override
    public UUID getUuid() {
        return this.uuid;
    }

    @Override
    public PlayerProfile getPlayerProfile() {
        return this.playerProfile;
    }

    @Override
    public void setPlayerProfile(PlayerProfile playerProfile) {
        this.playerProfile = playerProfile;
    }
}
