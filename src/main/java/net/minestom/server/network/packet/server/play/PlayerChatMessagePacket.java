package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.crypto.FilterMask;
import net.minestom.server.crypto.SignedMessageBody;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;

import static net.minestom.server.network.NetworkBuffer.*;

/**
 * Represents an outgoing chat message packet.
 */
public record PlayerChatMessagePacket(UUID sender, int index, byte @Nullable [] signature,
                                      SignedMessageBody.@NotNull Packed messageBody,
                                      @Nullable Component unsignedContent, FilterMask filterMask,
                                      int msgTypeId, Component msgTypeName,
                                      @Nullable Component msgTypeTarget) implements ServerPacket.Play, ServerPacket.ComponentHolding {

    public static final NetworkBuffer.Type<PlayerChatMessagePacket> SERIALIZER = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer writer, @NotNull PlayerChatMessagePacket value) {
            writer.write(UUID, value.sender);
            writer.write(VAR_INT, value.index);
            writer.writeOptional(RAW_BYTES, value.signature);
            writer.write(value.messageBody);
            writer.writeOptional(COMPONENT, value.unsignedContent);
            writer.write(value.filterMask);
            writer.write(VAR_INT, value.msgTypeId);
            writer.write(COMPONENT, value.msgTypeName);
            writer.writeOptional(COMPONENT, value.msgTypeTarget);
        }

        @Override
        public @NotNull PlayerChatMessagePacket read(@NotNull NetworkBuffer reader) {
            return new PlayerChatMessagePacket(reader.read(UUID), reader.read(VAR_INT), reader.readOptional(r -> r.readBytes(256)),
                    new SignedMessageBody.Packed(reader),
                    reader.readOptional(COMPONENT), new FilterMask(reader),
                    reader.read(VAR_INT), reader.read(COMPONENT),
                    reader.readOptional(COMPONENT));
        }
    };

    @Override
    public @NotNull Collection<Component> components() {
        final ArrayList<Component> list = new ArrayList<>();
        list.add(msgTypeName);
        if (unsignedContent != null) list.add(unsignedContent);
        if (msgTypeTarget != null) list.add(msgTypeTarget);
        return List.copyOf(list);
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        return new PlayerChatMessagePacket(sender, index, signature,
                messageBody,
                operator.apply(unsignedContent), filterMask,
                msgTypeId, operator.apply(msgTypeName),
                operator.apply(msgTypeTarget));
    }
}
