package net.minestom.server.network.packet.client;

import net.minestom.server.utils.binary.BinaryReader;
import org.jetbrains.annotations.NotNull;

public interface ClientPacket {

    void read(@NotNull BinaryReader reader);

}
