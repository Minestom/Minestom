package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Readable;
import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record DeclareCommandsPacket(@NotNull List<Node> nodes,
                                    int rootIndex) implements ServerPacket {
    public DeclareCommandsPacket {
        nodes = List.copyOf(nodes);
    }

    public DeclareCommandsPacket(@NotNull BinaryReader reader) {
        this(reader.readVarIntList(r -> {
            Node node = new Node();
            node.read(r);
            return node;
        }), reader.readVarInt());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarIntList(nodes, BinaryWriter::write);
        writer.writeVarInt(rootIndex);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.DECLARE_COMMANDS;
    }

    public static final class Node implements Writeable, Readable {
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

            if (children != null && children.length > 262114) {
                throw new RuntimeException("Children length " + children.length + " is bigger than the maximum allowed " + 262114);
            }
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
                name = reader.readSizedString();
            }

            if (isArgument()) {
                parser = reader.readSizedString();
                properties = getProperties(reader, parser);
            }

            if ((flags & 0x10) != 0) {
                suggestionsType = reader.readSizedString();
            }
        }

        private byte[] getProperties(BinaryReader reader, String parser) {
            return switch (parser) {
                case "brigadier:double" -> reader.extractBytes(() -> {
                    byte flags = reader.readByte();
                    if ((flags & 0x01) == 0x01) {
                        reader.readDouble(); // min
                    }
                    if ((flags & 0x02) == 0x02) {
                        reader.readDouble(); // max
                    }
                });
                case "brigadier:integer" -> reader.extractBytes(() -> {
                    byte flags = reader.readByte();
                    if ((flags & 0x01) == 0x01) {
                        reader.readInt(); // min
                    }
                    if ((flags & 0x02) == 0x02) {
                        reader.readInt(); // max
                    }
                });
                case "brigadier:string" -> reader.extractBytes(reader::readVarInt);
                case "brigadier:entity", "brigadier:score_holder" -> reader.extractBytes(reader::readByte);
                case "brigadier:range" -> reader.extractBytes(reader::readBoolean); // https://wiki.vg/Command_Data#minecraft:range, looks fishy
                default -> new byte[0]; // unknown
            };
        }

        private boolean isLiteral() {
            return (flags & 0b1) != 0;
        }

        private boolean isArgument() {
            return (flags & 0b10) != 0;
        }
    }

    public static byte getFlag(@NotNull NodeType type, boolean executable, boolean redirect, boolean suggestionType) {
        byte result = (byte) type.ordinal();
        if (executable) result |= 0x04;
        if (redirect) result |= 0x08;
        if (suggestionType) result |= 0x10;
        return result;
    }

    public enum NodeType {
        ROOT, LITERAL, ARGUMENT, NONE;
    }
}
