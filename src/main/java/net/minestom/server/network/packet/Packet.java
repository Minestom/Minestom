package net.minestom.server.network.packet;

import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.server.ServerPacket;

public sealed interface Packet permits ClientPacket, ServerPacket {
}
