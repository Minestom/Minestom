package net.minestom.server.command.builder.arguments;

import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.function.Consumer;

@SuppressWarnings("rawtypes")
public class ArgumentEnum<E extends Enum> extends Argument<E> {

    public final static int NOT_ENUM_VALUE_ERROR = 1;

    private final Class<E> enumClass;
    private final E[] values;

    public ArgumentEnum(@NotNull String id, Class<E> enumClass) {
        super(id);
        this.enumClass = enumClass;
        this.values = enumClass.getEnumConstants();
    }

    @NotNull
    @Override
    public E parse(@NotNull String input) throws ArgumentSyntaxException {
        for (E value : this.values) {
            if (value.name().equalsIgnoreCase(input)) {
                return value;
            }
        }
        throw new ArgumentSyntaxException("Not a " + this.enumClass.getSimpleName() + " value", input, NOT_ENUM_VALUE_ERROR);
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        // Add the single word properties + parser
        final Consumer<DeclareCommandsPacket.Node> wordConsumer = node -> {
            node.parser = "brigadier:string";
            node.properties = packetWriter -> {
                packetWriter.writeVarInt(0); // Single word
            };
        };

        // Create a primitive array for mapping
        DeclareCommandsPacket.Node[] nodes = new DeclareCommandsPacket.Node[this.values.length];

        // Create a node for each restrictions as literal
        for (int i = 0; i < nodes.length; i++) {
            DeclareCommandsPacket.Node argumentNode = new DeclareCommandsPacket.Node();

            argumentNode.flags = DeclareCommandsPacket.getFlag(DeclareCommandsPacket.NodeType.LITERAL,
                    executable, false, false);
            argumentNode.name = this.values[i].name().toLowerCase(Locale.ROOT);
            wordConsumer.accept(argumentNode);
            nodes[i] = argumentNode;
        }
        nodeMaker.addNodes(nodes);
    }
}
