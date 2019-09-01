package fr.themode.minestom.net.player;

import fr.adamaq01.ozao.net.Buffer;
import fr.adamaq01.ozao.net.packet.Packet;
import fr.adamaq01.ozao.net.server.Connection;
import fr.themode.minestom.net.ConnectionState;
import fr.themode.minestom.net.PacketWriterUtils;
import fr.themode.minestom.net.packet.server.ServerPacket;
import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;

import java.lang.reflect.Field;

public class PlayerConnection {

    private Connection connection;
    private ConnectionState connectionState;

    public PlayerConnection(Connection connection) {
        this.connection = connection;
        this.connectionState = ConnectionState.UNKNOWN;
    }

    public void sendPacket(Packet packet) {
        this.connection.sendPacket(packet);
    }


    // TODO make that proper (remove reflection)
    private static Field field;

    static {
        try {
            field = Class.forName("fr.adamaq01.ozao.net.server.backend.tcp.TCPConnection").getDeclaredField("channel");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        field.setAccessible(true);
    }

    public void sendUnencodedPacket(Buffer packet) {
        getChannel().writeAndFlush(packet.getData());
    }

    public void writeUnencodedPacket(Buffer packet) {
        getChannel().write(packet.getData());
    }

    public void sendPacket(ServerPacket serverPacket) {
        PacketWriterUtils.writeAndSend(this, serverPacket);
    }

    public void flush() {
        getChannel().flush();
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnectionState(ConnectionState connectionState) {
        this.connectionState = connectionState;
    }

    public ConnectionState getConnectionState() {
        return connectionState;
    }

    private Channel getChannel() {
        try {
            return ((SocketChannel) field.get(connection));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }
}
