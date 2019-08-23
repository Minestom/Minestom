package fr.themode.minestom.net.protocol;

import fr.adamaq01.ozao.net.Buffer;
import fr.adamaq01.ozao.net.packet.Packet;
import fr.adamaq01.ozao.net.protocol.Protocol;
import fr.themode.minestom.utils.PacketUtils;

import java.util.ArrayList;
import java.util.Collection;

import static fr.themode.minestom.utils.Utils.readVarInt;

public class MinecraftProtocol extends Protocol {

    public static final String PACKET_ID_IDENTIFIER = "id";

    public MinecraftProtocol() {
        super("minecraft");
    }

    @Override
    public boolean verify(Buffer buffer) {
        int length = readVarInt(buffer);
        int realLength = buffer.slice(buffer.readerIndex()).length();
        int id = readVarInt(buffer);
        buffer.readerIndex(0);
        return length == realLength && id >= 0;
    }

    @Override
    public boolean verify(Packet packet) {
        return PacketUtils.verify(packet);
    }

    @Override
    public Collection<Buffer> cut(Buffer buffer) {
        ArrayList<Buffer> buffers = new ArrayList<>();
        int read = 0;
        while (read < buffer.length()) {
            int lengthLength = buffer.readerIndex(read).readerIndex();
            int length = readVarInt(buffer);
            lengthLength = buffer.readerIndex() - lengthLength;
            buffers.add(buffer.sliceCopy(read, length + lengthLength));
            read += length + lengthLength;
        }
        return buffers;
    }

    @Override
    public Packet decode(Buffer buffer) {
        int length = readVarInt(buffer);
        int id = readVarInt(buffer);
        Buffer packetPayload = buffer.sliceCopy(buffer.readerIndex());
        return Packet.create(packetPayload).put(PACKET_ID_IDENTIFIER, id);
    }

    @Override
    public Buffer encode(Packet packet) {
        return PacketUtils.encode(packet);
    }
}
