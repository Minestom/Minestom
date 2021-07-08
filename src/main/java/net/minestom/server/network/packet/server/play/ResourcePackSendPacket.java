package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.packet.server.ComponentHoldingServerPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.resourcepack.ResourcePack;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.UnaryOperator;

public class ResourcePackSendPacket implements ComponentHoldingServerPacket {

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

    @Override
    public @NotNull Collection<Component> components() {
        List<Component> components = new ArrayList<>();
        if (forcedMessage != null) {
            components.add(forcedMessage);
        }
        return components;
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        ResourcePackSendPacket packet = new ResourcePackSendPacket();
        packet.url = this.url;
        packet.hash = this.hash;
        packet.forced = this.forced;
        packet.forcedMessage = this.forcedMessage == null ? null : operator.apply(this.forcedMessage);
        return packet;
    }
}
