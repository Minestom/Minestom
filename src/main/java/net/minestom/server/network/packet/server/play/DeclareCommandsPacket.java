package net.minestom.server.network.packet.server.play;

import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.registry.StaticProtocolObject;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

import static net.minestom.server.network.NetworkBuffer.*;

public record DeclareCommandsPacket(@NotNull List<Node> nodes,
                                    int rootIndex) implements ServerPacket.Play {
    public static final int MAX_NODES = Short.MAX_VALUE;

    public DeclareCommandsPacket {
        nodes = List.copyOf(nodes);
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
        public String parser; // Only for argument
        public byte[] properties; // Only for argument
        public String suggestionsType = ""; // Only if flags 0x10

        public static final NetworkBuffer.Type<Node> SERIALIZER = new Type<>() {
            @Override
            public void write(@NotNull NetworkBuffer writer, Node value) {
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
                    final int parserId = Argument.CONTAINER.toId(value.parser);
                    writer.write(VAR_INT, parserId);
                    if (value.properties != null) {
                        writer.write(RAW_BYTES, value.properties);
                    }
                }

                if ((value.flags & HAS_SUGGESTION_TYPE) != 0) {
                    writer.write(STRING, value.suggestionsType);
                }
            }

            public Node read(@NotNull NetworkBuffer reader) {
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
                    final StaticProtocolObject object = Argument.CONTAINER.getId(reader.read(VAR_INT));
                    node.parser = object.name();
                    node.properties = node.getProperties(reader, node.parser);
                }

                if ((node.flags & HAS_SUGGESTION_TYPE) != 0) {
                    node.suggestionsType = reader.read(STRING);
                }
                return node;
            }
        };

        private byte[] getProperties(@NotNull NetworkBuffer reader, String parser) {
            final Function<Function<NetworkBuffer, ?>, byte[]> minMaxExtractor = (via) -> reader.extractBytes((extractor) -> {
                byte flags = extractor.read(BYTE);
                if ((flags & 0x01) == 0x01) {
                    via.apply(extractor); // min
                }
                if ((flags & 0x02) == 0x02) {
                    via.apply(extractor); // max
                }
            });
            return switch (parser) {
                case "brigadier:double" -> minMaxExtractor.apply(b -> b.read(DOUBLE));
                case "brigadier:integer" -> minMaxExtractor.apply(b -> b.read(INT));
                case "brigadier:float" -> minMaxExtractor.apply(b -> b.read(FLOAT));
                case "brigadier:long" -> minMaxExtractor.apply(b -> b.read(LONG));
                case "brigadier:string" -> reader.extractBytes(b -> b.read(VAR_INT));
                case "minecraft:entity", "minecraft:score_holder" -> reader.extractBytes(b -> b.read(BYTE));
                case "minecraft:range" ->
                        reader.extractBytes(b -> b.read(BOOLEAN)); // https://wiki.vg/Command_Data#minecraft:range, looks fishy
                case "minecraft:resource_or_tag", "minecraft:registry" -> reader.extractBytes(b -> b.read(STRING));
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
