package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.resourcepack.ResourcePack;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class ResourcePackSendPacket implements ServerPacket {

    public String url = "";
    public String hash = "0000000000000000000000000000000000000000"; // Size 40
    public boolean forced;
    public Component forcedMessage;

    public ResourcePackSendPacket() {
    }

    public ResourcePackSendPacket(@NotNull ResourcePack resourcePack) {
        this.url = resourcePack.getUrl();
        this.hash = resourcePack.getHash();
        this.forced = resourcePack.isForced();
        this.forcedMessage = resourcePack.getForcedMessage();
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeSizedString(url);
        writer.writeSizedString(hash);
        writer.writeBoolean(forced);
        if (forcedMessage != null) {
            writer.writeBoolean(true);
            writer.writeComponent(forcedMessage);
        } else {
            writer.writeBoolean(false);
        }
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.url = reader.readSizedString();
        this.hash = reader.readSizedString();
        this.forced = reader.readBoolean();

        final boolean hasMessage = reader.readBoolean();
        if (hasMessage) {
            this.forcedMessage = reader.readComponent();
        } else {
            this.forcedMessage = null;
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.RESOURCE_PACK_SEND;
    }
}
