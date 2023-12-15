package net.minestom.server.network.packet.server.common;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ComponentHoldingServerPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.resourcepack.ResourcePack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

import static net.minestom.server.network.NetworkBuffer.*;

public record ResourcePackSendPacket(
        @NotNull String url,
        @NotNull String hash,
        boolean forced,
        @Nullable Component prompt
) implements ComponentHoldingServerPacket {
    public ResourcePackSendPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(STRING), reader.read(STRING), reader.read(BOOLEAN),
                reader.read(BOOLEAN) ? reader.read(COMPONENT) : null);
    }

    public ResourcePackSendPacket(@NotNull ResourcePack resourcePack) {
        this(resourcePack.getUrl(), resourcePack.getHash(), resourcePack.isForced(),
                resourcePack.getPrompt());
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(STRING, url);
        writer.write(STRING, hash);
        writer.write(BOOLEAN, forced);
        if (prompt != null) {
            writer.write(BOOLEAN, true);
            writer.write(COMPONENT, prompt);
        } else {
            writer.write(BOOLEAN, false);
        }
    }

    @Override
    public int getId(@NotNull ConnectionState state) {
        return switch (state) {
            case PLAY -> ServerPacketIdentifier.RESOURCE_PACK_SEND;
            case CONFIGURATION -> ServerPacketIdentifier.CONFIGURATION_RESOURCE_PACK_SEND;
            default -> throw new IllegalArgumentException();
        };
    }

    @Override
    public @NotNull Collection<Component> components() {
        return List.of(this.prompt);
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        return new ResourcePackSendPacket(this.url, this.hash, this.forced, operator.apply(this.prompt));
    }
}
