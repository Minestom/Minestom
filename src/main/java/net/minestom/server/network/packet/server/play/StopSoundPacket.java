package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.adventure.AdventurePacketConvertor;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minestom.server.network.NetworkBuffer.*;

public record StopSoundPacket(byte flags, @Nullable Sound.Source source,
                              @Nullable String sound) implements ServerPacket.Play {
    public StopSoundPacket(@NotNull NetworkBuffer reader) {
        this(read(reader));
    }

    private StopSoundPacket(StopSoundPacket packet) {
        this(packet.flags, packet.source, packet.sound);
    }

    private static StopSoundPacket read(@NotNull NetworkBuffer reader) {
        byte flags = reader.read(BYTE);
        var source = flags == 3 || flags == 1 ? reader.readEnum(Sound.Source.class) : null;
        var sound = flags == 2 || flags == 3 ? reader.read(STRING) : null;
        return new StopSoundPacket(flags, source, sound);
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(BYTE, flags);
        if (flags == 3 || flags == 1) {
            assert source != null;
            writer.write(VAR_INT, AdventurePacketConvertor.getSoundSourceValue(source));
        }
        if (flags == 2 || flags == 3) {
            assert sound != null;
            writer.write(STRING, sound);
        }
    }

}
