package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.Utils;

import java.util.function.Consumer;

public class DeclareCommandsPacket implements ServerPacket {


    public Node[] nodes;
    public int rootIndex;

    @Override
    public void write(Buffer buffer) {
        Utils.writeVarInt(buffer, nodes.length);
        for (Node node : nodes) {
            node.write(buffer);
        }
        Utils.writeVarInt(buffer, rootIndex);
    }

    @Override
    public int getId() {
        return 0x11;
    }

    public static class Node {

        public byte flags;
        public int[] children;
        public int redirectedNode; // Only if flags & 0x08
        public String name; // Only for literal and argument
        public String parser; // Only for argument
        public Consumer<Buffer> properties; // Only for argument
        public String suggestionsType; // Only if flags 0x10

        private void write(Buffer buffer) {
            buffer.putByte(flags);
            Utils.writeVarInt(buffer, children.length);
            for (int child : children) {
                Utils.writeVarInt(buffer, child);
            }

            if ((flags & 0x08) != 0) {
                Utils.writeVarInt(buffer, redirectedNode);
            }

            if (isLiteral() || isArgument()) {
                Utils.writeString(buffer, name);
            }

            if (isArgument()) {
                Utils.writeString(buffer, parser);
                if (properties != null) {
                    properties.accept(buffer);
                }
            }

            if ((flags & 0x10) != 0) {
                Utils.writeString(buffer, suggestionsType);
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
