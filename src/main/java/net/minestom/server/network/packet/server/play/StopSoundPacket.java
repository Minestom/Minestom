package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.adventure.AdventurePacketConvertor;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record StopSoundPacket(byte flags, @Nullable Sound.Source source,
                              @Nullable String sound) implements ServerPacket {
    public StopSoundPacket(BinaryReader reader) {
        this(read(reader));
    }

    private StopSoundPacket(StopSoundPacket packet) {
        this(packet.flags, packet.source, packet.sound);
    }

    private static StopSoundPacket read(BinaryReader reader) {
        var flags = reader.readByte();
        var source = flags == 3 || flags == 1 ? Sound.Source.values()[reader.readVarInt()] : null;
        var sound = flags == 2 || flags == 3 ? reader.readSizedString() : null;
        return new StopSoundPacket(flags, source, sound);
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeByte(flags);
        if (flags == 3 || flags == 1) {
            assert source != null;
            writer.writeVarInt(AdventurePacketConvertor.getSoundSourceValue(source));
        }
        if (flags == 2 || flags == 3) {
            assert sound != null;
            writer.writeSizedString(sound);
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.STOP_SOUND;
    }
}
