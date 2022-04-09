package net.minestom.server.network.packet.client.login;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.extras.velocity.VelocityProxy;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.packet.client.ClientPreplayPacket;
import net.minestom.server.network.packet.server.login.LoginDisconnectPacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.network.player.PlayerSocketConnection;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.UUID;

public record LoginPluginResponsePacket(int messageId, byte @Nullable [] data) implements ClientPreplayPacket {
    private final static ConnectionManager CONNECTION_MANAGER = MinecraftServer.getConnectionManager();
    public static final Component INVALID_PROXY_RESPONSE = Component.text("Invalid proxy response!", NamedTextColor.RED);

    public LoginPluginResponsePacket(BinaryReader reader) {
        this(reader.readVarInt(), reader.readBoolean() ? reader.readRemainingBytes() : null);
    }

    @Override
    public void process(@NotNull PlayerConnection connection) {
        // Proxy support
        if (connection instanceof PlayerSocketConnection socketConnection) {
            final String channel = socketConnection.getPluginRequestChannel(messageId);
            if (channel != null) {
                boolean success = false;

                SocketAddress socketAddress = null;
                UUID playerUuid = null;
                String playerUsername = null;
                PlayerSkin playerSkin = null;

                // Velocity
                if (VelocityProxy.isEnabled() && channel.equals(VelocityProxy.PLAYER_INFO_CHANNEL)) {
                    if (data != null && data.length > 0) {
                        BinaryReader reader = new BinaryReader(data);
                        success = VelocityProxy.checkIntegrity(reader);
                        if (success) {
                            // Get the real connection address
                            final InetAddress address = VelocityProxy.readAddress(reader);
                            final int port = ((java.net.InetSocketAddress) connection.getRemoteAddress()).getPort();
                            socketAddress = new InetSocketAddress(address, port);

                            playerUuid = reader.readUuid();
                            playerUsername = reader.readSizedString(16);

                            playerSkin = VelocityProxy.readSkin(reader);
                        }
                    }
                }

                if (success) {
                    if (socketAddress != null) {
                        socketConnection.setRemoteAddress(socketAddress);
                    }
                    if (playerUsername != null) {
                        socketConnection.UNSAFE_setLoginUsername(playerUsername);
                    }

                    final String username = socketConnection.getLoginUsername();
                    final UUID uuid = playerUuid != null ?
                            playerUuid : CONNECTION_MANAGER.getPlayerConnectionUuid(connection, username);

                    Player player = CONNECTION_MANAGER.startPlayState(connection, uuid, username, true);
                    player.setSkin(playerSkin);
                } else {
                    LoginDisconnectPacket disconnectPacket = new LoginDisconnectPacket(INVALID_PROXY_RESPONSE);
                    socketConnection.sendPacket(disconnectPacket);
                }
            }
        }
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(messageId);
        writer.writeBoolean(data != null);
        if (data != null) writer.writeBytes(data);
    }
}
