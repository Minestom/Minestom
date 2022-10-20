package net.minestom.server.network.packet.client.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.crypto.ChatBound;
import net.minestom.server.crypto.FilterMask;
import net.minestom.server.crypto.MessageSignature;
import net.minestom.server.crypto.SignedMessageBody;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import static net.minestom.server.network.NetworkBuffer.COMPONENT;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record ClientChatMessagePacket(@NotNull UUID sender, int index,
                                      @Nullable MessageSignature signature,
                                      SignedMessageBody.@NotNull Packed body,
                                      @Nullable Component unsignedContent, FilterMask filterMask,
                                      @NotNull ChatBound chatBound) implements ClientPacket {

    public ClientChatMessagePacket(@NotNull NetworkBuffer reader) {
        this(reader.read(NetworkBuffer.UUID), reader.read(VAR_INT),
                reader.readOptional(MessageSignature::new),
                new SignedMessageBody.Packed(reader),
                reader.readOptional(COMPONENT), new FilterMask(reader),
                new ChatBound(reader));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(NetworkBuffer.UUID, sender);
        writer.write(VAR_INT, index);
        writer.writeOptional(signature);
        writer.write(body);
        writer.writeOptional(COMPONENT, unsignedContent);
        writer.write(filterMask);
        writer.write(chatBound);
    }
}
