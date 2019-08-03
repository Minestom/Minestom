package fr.themode.minestom.net.packet.server.handshake;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.Utils;

public class ResponsePacket implements ServerPacket {

    private static final String JSON_EXAMPLE = "{\n" +
            "    \"version\": {\n" +
            "        \"name\": \"1.14.4\",\n" +
            "        \"protocol\": 498\n" +
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
            "        \"text\": \"Wallah les cubes\"\n" +
            "    },\n" +
            "    \"favicon\": \"data:image/png;base64,<data>\"\n" +
            "}";

    @Override
    public void write(Buffer buffer) {
        Utils.writeString(buffer, JSON_EXAMPLE);
    }

    @Override
    public int getId() {
        return 0x00;
    }
}
