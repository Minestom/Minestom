package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.resourcepack.ResourcePack;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record ResourcePackSendPacket(String url, String hash, boolean forced,
                                     Component prompt) implements ServerPacket {
    public ResourcePackSendPacket(BinaryReader reader) {
        this(reader.readSizedString(), reader.readSizedString(), reader.readBoolean(),
                reader.readBoolean() ? reader.readComponent() : null);
    }

    public ResourcePackSendPacket(@NotNull ResourcePack resourcePack) {
        this(resourcePack.getUrl(), resourcePack.getHash(), resourcePack.isForced(),
                resourcePack.getPrompt());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeSizedString(url);
        writer.writeSizedString(hash);
        writer.writeBoolean(forced);
        if (prompt != null) {
            writer.writeBoolean(true);
            writer.writeComponent(prompt);
        } else {
            writer.writeBoolean(false);
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.RESOURCE_PACK_SEND;
    }
}
