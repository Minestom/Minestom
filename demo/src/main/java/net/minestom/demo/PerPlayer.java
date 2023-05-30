package net.minestom.demo;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.packet.server.play.*;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;


public class PerPlayer extends Player {

    HashMap<UUID, PlayerSkin> PerSkin = new HashMap<>();
    HashMap<UUID, String> PerUsername = new HashMap<>();
    HashMap<UUID, Component> PerDisplayname = new HashMap<>();
    HashMap<UUID, GameMode> PerGameMode = new HashMap<>();
    HashMap<UUID, UUID> PerUUID = new HashMap<>();

    public PerPlayer(@NotNull UUID uuid, @NotNull String username, @NotNull PlayerConnection playerConnection) {
        super(uuid, username, playerConnection);
    }

    private void rebuild(PerPlayer receiver) {
        if (instance == null)
            return;

        boolean self = (receiver.equals(this));

        DestroyEntitiesPacket destroyEntitiesPacket = new DestroyEntitiesPacket(getEntityId());

        final PlayerInfoRemovePacket removePlayerPacket = getRemovePlayerToList();
        final PlayerInfoUpdatePacket addPlayerPacket = getAddPlayerToList(receiver);

        RespawnPacket respawnPacket = new RespawnPacket(
                getDimensionType().toString(),
                getDimensionType().getName().asString(),
                0, getGameMode(), getGameMode(), false, levelFlat, true, getDeathLocation());

        if (self) sendPacket(removePlayerPacket);
        if (self) sendPacket(destroyEntitiesPacket);
        if (self) sendPacket(addPlayerPacket);
        if (self) sendPacket(respawnPacket);
        if (self) refreshClientStateAfterRespawn();

        {
            // Remove player
            receiver.sendPacket(removePlayerPacket);
            sendPacketToViewers(destroyEntitiesPacket);
            if (!self) receiver.sendPacket(destroyEntitiesPacket);

            // Show player again
            receiver.sendPacket(addPlayerPacket);
            if (!self) receiver.showPlayer(getPlayerConnection());
            getViewers().stream().filter(player -> player.equals(receiver)).forEach(player -> showPlayer(player.getPlayerConnection()));
        }

        if (self) getInventory().update();
        teleport(getPosition());

    }


    private PlayerInfoUpdatePacket.Entry infoEntry(PerPlayer receiver) {
        final PlayerSkin skin = getSkin(receiver);
        List<PlayerInfoUpdatePacket.Property> prop = skin != null
                ? List.of(new PlayerInfoUpdatePacket.Property("textures", skin.textures(), skin.signature()))
                : List.of();
        return new PlayerInfoUpdatePacket.Entry(getUuid(), getUsername(receiver), prop, true, getLatency(), getGameMode(receiver), getDisplayName(receiver), null);
    }

    @Override
    public synchronized void setSkin(@Nullable PlayerSkin skin) {
        this.skin = skin;
        PerSkin.clear();
        MinecraftServer.getConnectionManager().getOnlinePlayers().forEach((receiver)->{
            rebuild((PerPlayer) receiver);
        });
    }

    public void setSkin(@Nullable PlayerSkin skin, PerPlayer receiver) {
        PerSkin.put(receiver.getUuid(), skin);
        rebuild(receiver);
    }

    private PlayerSkin getSkin(PerPlayer receiver) {
        return (PerSkin.containsKey(receiver.getUuid()) ? PerSkin.get(receiver.getUuid()) : getSkin());
    }

    public void setUsername(@Nullable String username) {
        PerUsername.clear();
        MinecraftServer.getConnectionManager().getOnlinePlayers().forEach((receiver)->{
            rebuild((PerPlayer) receiver);
        });
    }
    public void setUsername(@Nullable String username, PerPlayer receiver) {
        PerUsername.put(receiver.getUuid(), username);
        rebuild(receiver);
    }

    private String getUsername(PerPlayer receiver) {
        return (PerUsername.containsKey(receiver.getUuid()) ? PerUsername.get(receiver.getUuid()) : getUsername());
    }


    public void setUuid(@Nullable UUID uuid, PerPlayer receiver) {
        PerUUID.put(receiver.getUuid(), uuid);
        rebuild(receiver);
    }

    private UUID getUuid(PerPlayer receiver) {
        return (PerUUID.containsKey(receiver.getUuid()) ? PerUUID.get(receiver.getUuid()) : getUuid());
    }

    @Override
    public void setDisplayName(@Nullable Component displayName) {
        PerDisplayname.clear();
        super.setDisplayName(displayName);
        MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(player -> {
            PacketUtils.broadcastPacket(new PlayerInfoUpdatePacket(PlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME, infoEntry((PerPlayer) player)));
        });
    }

    public void setDisplayName(@Nullable Component displayname, PerPlayer receiver) {
        PerDisplayname.put(receiver.getUuid(), displayname);
        receiver.sendPacket(new PlayerInfoUpdatePacket(PlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME, infoEntry(receiver)));
    }

    private Component getDisplayName(PerPlayer receiver) {
        return (PerDisplayname.containsKey(receiver.getUuid()) ? PerDisplayname.get(receiver.getUuid()) : getDisplayName());
    }

    public void setGameMode(GameMode gameMode, PerPlayer receiver) {
        PerGameMode.put(receiver.getUuid(), gameMode);
        receiver.sendPacket(new PlayerInfoUpdatePacket(PlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE, infoEntry(receiver)));
    }

    @Override
    public void setGameMode(GameMode gameMode){
        PerGameMode.clear();
        this.gameMode = gameMode;
        // Condition to prevent sending the packets before spawning the player
        if (isActive()) {
            sendPacket(new ChangeGameStatePacket(ChangeGameStatePacket.Reason.CHANGE_GAMEMODE, gameMode.id()));
            MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(receiver -> {
                receiver.sendPacket(new PlayerInfoUpdatePacket(PlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE, infoEntry((PerPlayer) receiver)));
            });
        }

        // The client updates their abilities based on the GameMode as follows
        switch (gameMode) {
            case CREATIVE -> {
                this.allowFlying = true;
                this.instantBreak = true;
                this.invulnerable = true;
            }
            case SPECTATOR -> {
                this.allowFlying = true;
                this.instantBreak = false;
                this.invulnerable = true;
                this.flying = true;
            }
            default -> {
                this.allowFlying = false;
                this.instantBreak = false;
                this.invulnerable = false;
                this.flying = false;
            }
        }
        // Make sure that the player is in the PLAY state and synchronize their flight
        // speed.
        if (isActive()) {
            refreshAbilities();
        }
    }

    private GameMode getGameMode(PerPlayer receiver) {
        return (PerGameMode.containsKey(receiver.getUuid()) ? PerGameMode.get(receiver.getUuid()) : getGameMode());
    }

    protected @NotNull PlayerInfoUpdatePacket getAddPlayerToList(PerPlayer receiver) {
        return new PlayerInfoUpdatePacket(
                EnumSet.of(PlayerInfoUpdatePacket.Action.ADD_PLAYER, PlayerInfoUpdatePacket.Action.UPDATE_LISTED),
                List.of(infoEntry(receiver)));
    }

    @Override
    public void refreshLatency(int latency) {
        MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(receiver -> {
            refreshLatency(latency, (PerPlayer) receiver);
        });
    }

    public void refreshLatency(int latency, PerPlayer receiver) {
        receiver.sendPacket(new PlayerInfoUpdatePacket(PlayerInfoUpdatePacket.Action.UPDATE_LATENCY, infoEntry(receiver)));
    }
}
