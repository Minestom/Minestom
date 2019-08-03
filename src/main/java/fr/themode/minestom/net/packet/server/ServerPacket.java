package fr.themode.minestom.net.packet.server;

import fr.adamaq01.ozao.net.Buffer;

public interface ServerPacket {

    void write(Buffer buffer);

    int getId();

}
