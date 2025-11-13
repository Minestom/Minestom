package net.minestom.server.network.packet.server.play;

import net.minestom.server.command.ArgumentParserType;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static net.minestom.server.network.NetworkBuffer.*;

public record DeclareCommandsPacket(List<Node> nodes,
                                    int rootIndex) implements ServerPacket.Play {
    public static final int MAX_NODES = Short.MAX_VALUE;

    public DeclareCommandsPacket {
        nodes = List.copyOf(nodes); // TODO deep copy?
    }

    public static final NetworkBuffer.Type<DeclareCommandsPacket> SERIALIZER = NetworkBufferTemplate.template(
            Node.SERIALIZER.list(MAX_NODES), DeclareCommandsPacket::nodes,
            VAR_INT, DeclareCommandsPacket::rootIndex,
            DeclareCommandsPacket::new
    );

    public static final int NODE_TYPE = 0x03;
    public static final int IS_EXECUTABLE = 0x04;
    public static final int HAS_REDIRECT = 0x08;
    public static final int HAS_SUGGESTION_TYPE = 0x10;

    public static final class Node {
        public byte flags;
        public int[] children = new int[0];
        public int redirectedNode; // Only if flags & 0x08
        public String name = ""; // Only for literal and argument
        public @Nullable ArgumentParserType parser; // Only for argument
        public byte @Nullable [] properties; // Only for argument
        public String suggestionsType = ""; // Only if flags 0x10

        public static final NetworkBuffer.Type<Node> SERIALIZER = new NetworkBuffer.Type<>() {
            @Override
            public void write(NetworkBuffer writer, Node value) {
                writer.write(BYTE, value.flags);

                if (value.children != null && value.children.length > 262114) {
                    throw new RuntimeException("Children length " + value.children.length + " is bigger than the maximum allowed " + 262114);
                }
                writer.write(VAR_INT_ARRAY, value.children);

                if ((value.flags & HAS_REDIRECT) != 0) {
                    writer.write(VAR_INT, value.redirectedNode);
                }

                if (value.isLiteral() || value.isArgument()) {
                    writer.write(STRING, value.name);
                }

                if (value.isArgument()) {
                    writer.write(ArgumentParserType.NETWORK_TYPE, value.parser);
                    if (value.properties != null) {
                        writer.write(RAW_BYTES, value.properties);
                    }
                }

                if ((value.flags & HAS_SUGGESTION_TYPE) != 0) {
                    writer.write(STRING, value.suggestionsType);
                }
            }

            public Node read(NetworkBuffer reader) {
                Node node = new Node();
                node.flags = reader.read(BYTE);
                node.children = reader.read(VAR_INT_ARRAY);
                if ((node.flags & HAS_REDIRECT) != 0) {
                    node.redirectedNode = reader.read(VAR_INT);
                }

                if (node.isLiteral() || node.isArgument()) {
                    node.name = reader.read(STRING);
                }

                if (node.isArgument()) {
                    node.parser = reader.read(ArgumentParserType.NETWORK_TYPE);
                    node.properties = node.getProperties(reader, node.parser);
                }

                if ((node.flags & HAS_SUGGESTION_TYPE) != 0) {
                    node.suggestionsType = reader.read(STRING);
                }
                return node;
            }
        };

        private byte[] getProperties(NetworkBuffer reader, ArgumentParserType parser) {
            return switch (parser) {
                case DOUBLE, FLOAT, INTEGER, LONG -> reader.extractReadBytes(extractor -> {
                    byte flags1 = extractor.read(NetworkBuffer.BYTE);
                    if ((flags1 & 0x01) != 0x01 || (flags1 & 0x02) != 0x02) return;
                    final Type<?> type = switch (parser) {
                        case DOUBLE -> NetworkBuffer.DOUBLE;
                        case FLOAT -> NetworkBuffer.FLOAT;
                        case INTEGER -> NetworkBuffer.INT;
                        case LONG -> NetworkBuffer.LONG;
                        default -> throw new IllegalArgumentException("Unknown parser " + parser);
                    };

                    if ((flags1 & 0x01) == 0x01) {
                        extractor.read(type); // min
                    }
                    if ((flags1 & 0x02) == 0x02) {
                        extractor.read(type); // max
                    }
                });
                case STRING -> reader.extractReadBytes(VAR_INT);
                case ENTITY, SCORE_HOLDER -> reader.extractReadBytes(BYTE);
                case TIME -> reader.extractReadBytes(INT);
                case RESOURCE_OR_TAG, RESOURCE_OR_TAG_KEY, RESOURCE, RESOURCE_KEY -> reader.extractReadBytes(STRING);
                default -> new byte[0]; // unknown
            };
        }

        private boolean isLiteral() {
            return (flags & 0b1) != 0;
        }

        private boolean isArgument() {
            return (flags & 0b10) != 0;
        }

        @Override
        public boolean equals(Object object) {
            if (!(object instanceof Node node)) return false;
            return flags == node.flags && redirectedNode == node.redirectedNode && Arrays.equals(children, node.children) && Objects.equals(name, node.name) && parser == node.parser && Arrays.equals(properties, node.properties) && Objects.equals(suggestionsType, node.suggestionsType);
        }

        @Override
        public int hashCode() {
            int result = flags;
            result = 31 * result + Arrays.hashCode(children);
            result = 31 * result + redirectedNode;
            result = 31 * result + Objects.hashCode(name);
            result = 31 * result + Objects.hashCode(parser);
            result = 31 * result + Arrays.hashCode(properties);
            result = 31 * result + Objects.hashCode(suggestionsType);
            return result;
        }
    }

    public static byte getFlag(NodeType type, boolean executable, boolean redirect, boolean suggestionType) {
        byte result = (byte) type.ordinal();
        if (executable) result |= 0x04;
        if (redirect) result |= 0x08;
        if (suggestionType) result |= 0x10;
        return result;
    }

    public enum NodeType {
        ROOT, LITERAL, ARGUMENT, NONE
    }
}
