package net.minestom.server.command.builder.arguments;

import net.minestom.server.command.StringReader;
import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.exception.CommandException;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.function.UnaryOperator;

@SuppressWarnings("rawtypes")
public class ArgumentEnum<E extends Enum> extends Argument<E> {

    private final Class<E> enumClass;
    private final E[] values;
    private Format format = Format.DEFAULT;

    public ArgumentEnum(@NotNull String id, @NotNull Class<E> enumClass) {
        super(id);
        this.enumClass = enumClass;
        this.values = enumClass.getEnumConstants();
    }

    public ArgumentEnum<E> setFormat(@NotNull Format format) {
        this.format = format;
        return this;
    }

    @Override
    public @NotNull E parse(@NotNull StringReader input) throws CommandException {
        int pos = input.position();
        String next = input.readString();
        for (E value : this.values){
            if (this.format.formatter.apply(value.name()).equals(next)){
                return value;
            }
        }
        throw CommandException.COMMAND_UNKNOWN_ARGUMENT.generateException(input.all(), pos);
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        // Create a primitive array for mapping
        DeclareCommandsPacket.Node[] nodes = new DeclareCommandsPacket.Node[this.values.length];

        // Create a node for each restrictions as literal
        for (int i = 0; i < nodes.length; i++) {
            DeclareCommandsPacket.Node argumentNode = new DeclareCommandsPacket.Node();

            argumentNode.flags = DeclareCommandsPacket.getFlag(DeclareCommandsPacket.NodeType.LITERAL,
                    executable, false, false);
            argumentNode.name = this.format.formatter.apply(this.values[i].name());
            nodes[i] = argumentNode;
        }
        nodeMaker.addNodes(nodes);
    }

    public enum Format {
        DEFAULT(name -> name),
        LOWER_CASED(name -> name.toLowerCase(Locale.ROOT)),
        UPPER_CASED(name -> name.toUpperCase(Locale.ROOT));

        private final UnaryOperator<String> formatter;

        Format(@NotNull UnaryOperator<String> formatter) {
            this.formatter = formatter;
        }
    }

    @Override
    public String toString() {
        return String.format("Enum<%s>", getId());
    }
}
