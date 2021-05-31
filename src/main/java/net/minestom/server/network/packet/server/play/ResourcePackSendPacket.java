package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class ResourcePackSendPacket implements ServerPacket {

    public String url = "";
    public String hash = "0000000000000000000000000000000000000000"; // Size 40
    public boolean forced;
    public Component forcedMessage = Component.empty();

    public ResourcePackSendPacket() {
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeSizedString(url);
        writer.writeSizedString(hash);
        writer.writeBoolean(forced);
        writer.writeComponent(forcedMessage);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.url = reader.readSizedString(Integer.MAX_VALUE);
        this.hash = reader.readSizedString(Integer.MAX_VALUE);
        this.forced = reader.readBoolean();
        this.forcedMessage = reader.readComponent();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.RESOURCE_PACK_SEND;
    }
}
