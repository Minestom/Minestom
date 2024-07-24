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
        public void write(@NotNull NetworkBuffer buffer, @NotNull PlayerChatMessagePacket value) {
            buffer.write(UUID, value.sender);
            buffer.write(VAR_INT, value.index);
            buffer.writeOptional(RAW_BYTES, value.signature);
            buffer.write(value.messageBody);
            buffer.writeOptional(COMPONENT, value.unsignedContent);
            buffer.write(value.filterMask);
            buffer.write(VAR_INT, value.msgTypeId);
            buffer.write(COMPONENT, value.msgTypeName);
            buffer.writeOptional(COMPONENT, value.msgTypeTarget);
        }

        @Override
        public @NotNull PlayerChatMessagePacket read(@NotNull NetworkBuffer buffer) {
            return new PlayerChatMessagePacket(buffer.read(UUID), buffer.read(VAR_INT), buffer.readOptional(r -> r.readBytes(256)),
                    new SignedMessageBody.Packed(buffer),
                    buffer.readOptional(COMPONENT), new FilterMask(buffer),
                    buffer.read(VAR_INT), buffer.read(COMPONENT),
                    buffer.readOptional(COMPONENT));
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
