package net.minestom.server.network.packet.client;

import net.minestom.server.network.packet.Packet;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Readable;
import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a packet received from a client.
 */
public interface ClientPacket extends Packet { }
