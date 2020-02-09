package fr.themode.minestom.net.packet.server.handshake;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;

public class ResponsePacket implements ServerPacket {

    private static final String JSON_EXAMPLE = "{\n" +
            "    \"version\": {\n" +
            "        \"name\": \"1.15.2\",\n" +
            "        \"protocol\": 578\n" +
            "    },\n" +
            "    \"players\": {\n" +
            "        \"max\": 100,\n" +
            "        \"online\": 1,\n" +
            "        \"sample\": [\n" +
            "            {\n" +
            "                \"name\": \"TheMode\",\n" +
            "                \"id\": \"4566e69f-c907-48ee-8d71-d7ba5aa00d20\"\n" +
            "            }\n" +
            "        ]\n" +
            "    },\t\n" +
            "    \"description\": {\n" +
            "        \"text\": \"Hey guys!\"\n" +
            "    },\n" +
            "    \"favicon\": \"data:image/png;base64,<data>\"\n" +
            "}";

    @Override
    public void write(PacketWriter writer) {
        writer.writeSizedString(JSON_EXAMPLE);
    }

    @Override
    public int getId() {
        return 0x00;
    }
}
