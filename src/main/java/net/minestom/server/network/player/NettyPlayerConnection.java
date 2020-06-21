package net.minestom.server.network.player;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import net.minestom.server.extras.mojangAuth.Decrypter;
import net.minestom.server.extras.mojangAuth.Encrypter;
import net.minestom.server.extras.mojangAuth.MojangCrypt;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.utils.PacketUtils;

import javax.crypto.SecretKey;
import java.net.SocketAddress;

/**
 * Represent a networking connection with Netty
 * It is the implementation used for all server connection client
 */
public class NettyPlayerConnection extends PlayerConnection {

	private ChannelHandlerContext channel;
	@Getter
	private boolean encrypted = false;


	public NettyPlayerConnection(ChannelHandlerContext channel) {
		super();
		this.channel = channel;
	}

	public void setEncryptionKey(SecretKey secretKey) {
		this.encrypted = true;
		getChannel().pipeline().addBefore("decoder", "decrypt", new Decrypter(MojangCrypt.getCipher(2, secretKey)));
		getChannel().pipeline().addBefore("encoder", "encrypt", new Encrypter(MojangCrypt.getCipher(1, secretKey)));
	}

	@Override
	public void sendPacket(ByteBuf buffer) {
		if (encrypted) {
			buffer = buffer.copy();
			buffer.retain();
			getChannel().writeAndFlush(buffer);
			buffer.release();
		} else {
			buffer.retain();
			getChannel().writeAndFlush(buffer);
		}
	}

	@Override
	public void writePacket(ByteBuf buffer) {
		if (encrypted) {
		buffer = buffer.copy();
		buffer.retain();
		getChannel().write(buffer);
		buffer.release();
		} else {
			buffer.retain();
			getChannel().write(buffer);
		}
	}

	@Override
	public void sendPacket(ServerPacket serverPacket) {
		ByteBuf buffer = PacketUtils.writePacket(serverPacket);
		sendPacket(buffer);
		buffer.release();
	}

	@Override
	public void flush() {
		getChannel().flush();
	}

	@Override
	public SocketAddress getRemoteAddress() {
		return getChannel().channel().remoteAddress();
	}

	@Override
	public void disconnect() {
		getChannel().close();
	}

	public ChannelHandlerContext getChannel() {
		return channel;
	}

}
