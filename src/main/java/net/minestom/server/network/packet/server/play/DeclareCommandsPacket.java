package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Readable;
import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.NotNull;

public class DeclareCommandsPacket implements ServerPacket {

    public Node[] nodes = new Node[0];
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
    public void read(@NotNull BinaryReader reader) {
        int nodeCount = reader.readVarInt();
        nodes = new Node[nodeCount];
        for (int i = 0; i < nodeCount; i++) {
            nodes[i] = new Node();
            nodes[i].read(reader);
        }
        rootIndex = reader.readVarInt();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.DECLARE_COMMANDS;
    }

    public static class Node implements Writeable, Readable {

        public byte flags;
        public int[] children = new int[0];
        public int redirectedNode; // Only if flags & 0x08
        public String name = ""; // Only for literal and argument
        public String parser = ""; // Only for argument
        public byte[] properties; // Only for argument
        public String suggestionsType = ""; // Only if flags 0x10

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
                    writer.writeBytes(properties);
                }
            }

            if ((flags & 0x10) != 0) {
                writer.writeSizedString(suggestionsType);
            }
        }

        @Override
        public void read(@NotNull BinaryReader reader) {
            flags = reader.readByte();
            children = reader.readVarIntArray();
            if ((flags & 0x08) != 0) {
                redirectedNode = reader.readVarInt();
            }

            if (isLiteral() || isArgument()) {
                name = reader.readSizedString(Integer.MAX_VALUE);
            }

            if(isArgument()) {
                parser = reader.readSizedString(Integer.MAX_VALUE);
                properties = getProperties(reader, parser);
            }

            if ((flags & 0x10) != 0) {
                suggestionsType = reader.readSizedString(Integer.MAX_VALUE);
            }
        }

        private byte[] getProperties(BinaryReader reader, String parser) {
            switch (parser) {
                case "brigadier:double":
                    return reader.extractBytes(() -> {
                        byte flags = reader.readByte();
                        if((flags & 0x01) == 0x01) {
                            reader.readDouble(); // min
                        }
                        if((flags & 0x02) == 0x02) {
                            reader.readDouble(); // max
                        }
                    });

                case "brigadier:integer":
                    return reader.extractBytes(() -> {
                        byte flags = reader.readByte();
                        if((flags & 0x01) == 0x01) {
                            reader.readInt(); // min
                        }
                        if((flags & 0x02) == 0x02) {
                            reader.readInt(); // max
                        }
                    });

                case "brigadier:string":
                    return reader.extractBytes(reader::readVarInt);

                case "brigadier:entity":
                case "brigadier:score_holder":
                    return reader.extractBytes(reader::readByte);

                case "brigadier:range":
                    return reader.extractBytes(reader::readBoolean); // https://wiki.vg/Command_Data#minecraft:range, looks fishy

                default:
                    return new byte[0]; // unknown
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
