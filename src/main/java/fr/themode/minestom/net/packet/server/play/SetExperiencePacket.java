package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.Utils;

public class SetExperiencePacket implements ServerPacket {

    public float percentage;
    public int level;
    public int totalExperience;

    @Override
    public void write(Buffer buffer) {
        buffer.putFloat(percentage);
        Utils.writeVarInt(buffer, level);
        Utils.writeVarInt(buffer, totalExperience);
    }

    @Override
    public int getId() {
        return 0x47;
    }
}
