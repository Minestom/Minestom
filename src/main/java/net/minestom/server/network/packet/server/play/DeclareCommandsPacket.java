package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class DeclareCommandsPacket implements ServerPacket {

    public Node[] nodes;
    public int rootIndex;

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(nodes.length);
        for (Node node : nodes) {
            node.write(writer);
        }
        writer.writeVarInt(rootIndex);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.DECLARE_COMMANDS;
    }

    public static class Node implements Writeable {

        public byte flags;
        public int[] children;
        public int redirectedNode; // Only if flags & 0x08
        public String name; // Only for literal and argument
        public String parser; // Only for argument
        public Consumer<BinaryWriter> properties; // Only for argument
        public String suggestionsType; // Only if flags 0x10

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeByte(flags);

            writer.writeVarIntArray(children);

            if ((flags & 0x08) != 0) {
                writer.writeVarInt(redirectedNode);
            }

            if (isLiteral() || isArgument()) {
                writer.writeSizedString(name);
            }

            if (isArgument()) {
                writer.writeSizedString(parser);
                if (properties != null) {
                    properties.accept(writer);
                }
            }

            if ((flags & 0x10) != 0) {
                writer.writeSizedString(suggestionsType);
            }

        }

        private boolean isLiteral() {
            return (flags & 0b1) != 0;
        }

        private boolean isArgument() {
            return (flags & 0b10) != 0;
        }

    }

    public static byte getFlag(@NotNull NodeType type, boolean executable, boolean redirect, boolean suggestionType) {
        byte result = (byte) type.mask;

        if (executable) {
            result |= 0x04;
        }

        if (redirect) {
            result |= 0x08;
        }

        if (suggestionType) {
            result |= 0x10;
        }
        return result;
    }

    public enum NodeType {
        ROOT(0), LITERAL(0b1), ARGUMENT(0b10), NONE(0x11);

        private final int mask;

        NodeType(int mask) {
            this.mask = mask;
        }

    }

}
