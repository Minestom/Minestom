package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;

import java.util.function.Consumer;

public class DeclareCommandsPacket implements ServerPacket {


    public Node[] nodes;
    public int rootIndex;

    @Override
    public void write(BinaryWriter writer) {
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

    public static class Node {

        public byte flags;
        public int[] children;
        public int redirectedNode; // Only if flags & 0x08
        public String name; // Only for literal and argument
        public String parser; // Only for argument
        public Consumer<BinaryWriter> properties; // Only for argument
        public String suggestionsType; // Only if flags 0x10

        private void write(BinaryWriter writer) {
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

}
