package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.Utils;

public class AnimationPacket implements ServerPacket {

    public int entityId;
    public Animation animation;

    @Override
    public void write(Buffer buffer) {
        Utils.writeVarInt(buffer, entityId);
        buffer.putByte((byte) animation.ordinal());
    }

    @Override
    public int getId() {
        return 0x06;
    }

    public enum Animation {
        SWING_MAIN_ARM,
        TAKE_DAMAGE,
        LEAVE_BED,
        SWING_OFF_HAND,
        CRITICAL_EFFECT,
        MAGICAL_CRITICAL_EFFECT;
    }
}
