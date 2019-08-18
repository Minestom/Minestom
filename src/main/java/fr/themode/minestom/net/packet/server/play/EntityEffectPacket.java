package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.Utils;

public class EntityEffectPacket implements ServerPacket {

    public int entityId;
    public byte effectId;
    public byte amplifier;
    public int duration;
    public byte flags;

    @Override
    public void write(Buffer buffer) {
        Utils.writeVarInt(buffer, entityId);
        buffer.putByte(effectId);
        buffer.putByte(amplifier);
        Utils.writeVarInt(buffer, duration);
        buffer.putByte(flags);
    }

    @Override
    public int getId() {
        return 0x59;
    }
}
