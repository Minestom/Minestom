package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.adventure.AdventurePacketConvertor;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class StopSoundPacket implements ServerPacket {
    private final byte flags;
    private final Sound.Source source;
    private final String sound;

    public StopSoundPacket(byte flags, Sound.Source source, String sound) {
        this.flags = flags;
        this.source = source;
        this.sound = sound;
    }

    public StopSoundPacket(BinaryReader reader) {
        this.flags = reader.readByte();
        if (flags == 3 || flags == 1) {
            this.source = Sound.Source.values()[reader.readVarInt()];
        } else this.source = null;
        if (flags == 2 || flags == 3) {
            this.sound = reader.readSizedString();
        } else this.sound = null;
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeByte(flags);
        if (flags == 3 || flags == 1)
            writer.writeVarInt(AdventurePacketConvertor.getSoundSourceValue(source));
        if (flags == 2 || flags == 3)
            writer.writeSizedString(sound);
    }

    public byte flags() {
        return flags;
    }

    public Sound.Source source() {
        return source;
    }

    public String sound() {
        return sound;
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.STOP_SOUND;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StopSoundPacket that)) return false;
        return flags == that.flags && source == that.source &&
                Objects.equals(sound, that.sound);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flags, source, sound);
    }
}
