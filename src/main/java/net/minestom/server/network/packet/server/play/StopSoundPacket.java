package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.adventure.AdventurePacketConvertor;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.Nullable;

import static net.minestom.server.network.NetworkBuffer.*;

public record StopSoundPacket(byte flags, @Nullable Sound.Source source,
                              @Nullable String sound) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<StopSoundPacket> SERIALIZER = new Type<>() {
        @Override
        public void write(NetworkBuffer buffer, StopSoundPacket value) {
            buffer.write(BYTE, value.flags());
            if (value.flags == 3 || value.flags == 1) {
                if (value.source == null) throw new NullPointerException("source cannot be null");
                buffer.write(VAR_INT, AdventurePacketConvertor.getSoundSourceValue(value.source));
            }
            if (value.flags == 2 || value.flags == 3) {
                if (value.sound == null) throw new NullPointerException("sound cannot be null");
                buffer.write(STRING, value.sound);
            }
        }

        @Override
        public StopSoundPacket read(NetworkBuffer buffer) {
            byte flags = buffer.read(BYTE);
            var source = flags == 3 || flags == 1 ? buffer.read(NetworkBuffer.Enum(Sound.Source.class)) : null;
            var sound = flags == 2 || flags == 3 ? buffer.read(STRING) : null;
            return new StopSoundPacket(flags, source, sound);
        }
    };
}
