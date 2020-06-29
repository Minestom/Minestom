package net.minestom.server.network.packet.server.login;

import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;

public class LoginDisconnect implements ServerPacket {
	private String kickMessage;

	public LoginDisconnect(String kickMessage) {
		this.kickMessage = kickMessage;
	}

	@Override
	public void write(PacketWriter writer) {
		writer.writeSizedString(kickMessage);
	}

	@Override
	public int getId() {
		return 0x00;
	}

}
