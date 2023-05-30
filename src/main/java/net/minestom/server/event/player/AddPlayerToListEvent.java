package net.minestom.server.event.player;

import net.kyori.adventure.text.Component;
import net.minestom.server.crypto.PlayerPublicKey;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * Called at the player connection to initialize his Tab Entry.
 */
public class AddPlayerToListEvent implements PlayerEvent {

    private final Player player;
    private UUID uuid;
    private String username;
    private List<PlayerInfoUpdatePacket.Property> properties;
    private GameMode gameMode;
    private int ping;
    private Component displayName;
    private PlayerPublicKey publicKey;
    private final Player receiver;

    public AddPlayerToListEvent(@NotNull Player player, UUID uuid, String username, List
            <PlayerInfoUpdatePacket.Property> properties, GameMode gameMode, int ping, Component displayName, PlayerPublicKey publicKey, @Nullable Player receiver) {
        this.player = player;
        this.uuid = uuid;
        this.username = username;
        this.properties = properties;
        this.gameMode = gameMode;
        this.ping = ping;
        this.displayName = displayName;
        this.publicKey = publicKey;
        this.receiver = receiver;
    }

    /**
     * Sets the spawning skin of the player.
     *
     * @param skin the new player skin
     */
    public void setSkin(@Nullable PlayerSkin skin) {
        properties = List.of(new PlayerInfoUpdatePacket.Property("textures", skin.textures(), skin.signature()));
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setProperties(List<PlayerInfoUpdatePacket.Property> properties) {
        this.properties = properties;
    }

    public List<PlayerInfoUpdatePacket.Property> getProperties() {
        return properties;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public int getPing() {
        return ping;
    }

    public void setPing(int ping) {
        this.ping = ping;
    }

    public Component getDisplayName() {
        return displayName;
    }

    public void setDisplayName(Component displayName) {
        this.displayName = displayName;
    }

    public PlayerPublicKey getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(PlayerPublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public @Nullable Player getReceiver() {
        return receiver;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }
}
