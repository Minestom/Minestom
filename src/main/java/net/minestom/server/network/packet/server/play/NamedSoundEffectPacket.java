package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.sound.Sound.Source;
import net.minestom.server.adventure.AdventurePacketConvertor;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record NamedSoundEffectPacket(String soundName, Source source, int x, int y, int z,
                                     float volume, float pitch, long seed) implements ServerPacket {
    public NamedSoundEffectPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(STRING), Source.values()[reader.read(VAR_INT)],
                reader.read(INT) / 8, reader.read(INT) / 8, reader.read(INT) / 8,
                reader.read(FLOAT), reader.read(FLOAT), reader.read(LONG));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(STRING, soundName);
        writer.write(VAR_INT, AdventurePacketConvertor.getSoundSourceValue(source));
        writer.write(INT, x * 8);
        writer.write(INT, y * 8);
        writer.write(INT, z * 8);
        writer.write(FLOAT, volume);
        writer.write(FLOAT, pitch);
        writer.write(LONG, seed);
    }

    @Override
    public int getId() {
        return 0;
    }
}
