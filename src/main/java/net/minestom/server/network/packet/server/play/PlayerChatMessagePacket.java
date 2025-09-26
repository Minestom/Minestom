package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.crypto.FilterMask;
import net.minestom.server.crypto.SignedMessageBody;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.Nullable;

import java.util.*;
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
            COMPONENT.optional(), PlayerChatMessagePacket::msgTypeTarget,
            PlayerChatMessagePacket::new
    );

    public PlayerChatMessagePacket {
        signature = signature != null ? signature.clone() : null;
    }

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
                messageBody, unsignedContent != null ? operator.apply(unsignedContent) : null, filterMask,
                msgTypeId, operator.apply(msgTypeName), msgTypeTarget != null ? operator.apply(msgTypeTarget) : null);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof PlayerChatMessagePacket(int globalIndex1, UUID sender1, int index1, byte[] signature1, SignedMessageBody.Packed body, Component content, FilterMask mask, int typeId, Component typeName, Component typeTarget))) return false;
        return index() == index1 && msgTypeId() == typeId && globalIndex() == globalIndex1 && Objects.equals(sender(), sender1) && Objects.equals(filterMask(), mask) && Objects.equals(msgTypeName(), typeName) && Objects.equals(msgTypeTarget(), typeTarget) && Objects.equals(unsignedContent(), content) && Arrays.equals(signature(), signature1) && messageBody().equals(body);
    }

    @Override
    public int hashCode() {
        int result = globalIndex();
        result = 31 * result + Objects.hashCode(sender());
        result = 31 * result + index();
        result = 31 * result + Arrays.hashCode(signature());
        result = 31 * result + messageBody().hashCode();
        result = 31 * result + Objects.hashCode(unsignedContent());
        result = 31 * result + Objects.hashCode(filterMask());
        result = 31 * result + msgTypeId();
        result = 31 * result + Objects.hashCode(msgTypeName());
        result = 31 * result + Objects.hashCode(msgTypeTarget());
        return result;
    }
}
