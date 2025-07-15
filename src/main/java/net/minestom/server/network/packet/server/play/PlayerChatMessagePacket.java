package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.crypto.FilterMask;
import net.minestom.server.crypto.SignedMessageBody;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;

import static net.minestom.server.network.NetworkBuffer.*;

/**
 * Represents an outgoing chat message packet.
 */
public record PlayerChatMessagePacket(int globalIndex, UUID sender, int index, byte @Nullable [] signature,
                                      SignedMessageBody.Packed messageBody,
                                      @Nullable Component unsignedContent, FilterMask filterMask,
                                      int msgTypeId, Component msgTypeName,
                                      @Nullable Component msgTypeTarget) implements ServerPacket.Play, ServerPacket.ComponentHolding {

    public static final NetworkBuffer.Type<PlayerChatMessagePacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, PlayerChatMessagePacket::globalIndex,
            UUID, PlayerChatMessagePacket::sender,
            VAR_INT, PlayerChatMessagePacket::index,
            RAW_BYTES.optional(), PlayerChatMessagePacket::signature,
            SignedMessageBody.Packed.SERIALIZER, PlayerChatMessagePacket::messageBody,
            COMPONENT.optional(), PlayerChatMessagePacket::unsignedContent,
            FilterMask.SERIALIZER, PlayerChatMessagePacket::filterMask,
            VAR_INT, PlayerChatMessagePacket::msgTypeId,
            COMPONENT, PlayerChatMessagePacket::msgTypeName,
            COMPONENT, PlayerChatMessagePacket::msgTypeTarget,
            PlayerChatMessagePacket::new
    );

    @Override
    public Collection<Component> components() {
        final ArrayList<Component> list = new ArrayList<>();
        list.add(msgTypeName);
        if (unsignedContent != null) list.add(unsignedContent);
        if (msgTypeTarget != null) list.add(msgTypeTarget);
        return List.copyOf(list);
    }

    @Override
    public ServerPacket copyWithOperator(UnaryOperator<Component> operator) {
        return new PlayerChatMessagePacket(globalIndex, sender, index, signature,
                messageBody, operator.apply(unsignedContent), filterMask,
                msgTypeId, operator.apply(msgTypeName), operator.apply(msgTypeTarget));
    }
}
