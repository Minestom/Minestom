package net.minestom.server.command.builder.arguments;

import net.minestom.server.command.StringReader;
import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.exception.CommandException;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a single word in the command.
 * <p>
 * You can specify the valid words with {@link #from(String...)} (do not abuse it or the client will not be able to join).
 * <p>
 * Example: hey
 */
public class ArgumentWord extends Argument<String> {

    protected String[] restrictions;

    public ArgumentWord(@NotNull String id) {
        super(id);
    }

    /**
     * Used to force the use of a few precise words instead of complete freedom.
     * <p>
     * WARNING: having an array too long would result in a packet too big or the client being stuck during login.
     *
     * @param restrictions the accepted words,
     *                     can be null but if an array is passed
     *                     you need to ensure that it is filled with non-null values
     * @return 'this' for chaining
     * @throws NullPointerException if {@code restrictions} is not null but contains null value(s)
     */
    @NotNull
    public ArgumentWord from(@Nullable String... restrictions) {
        if (restrictions != null) {
            for (String restriction : restrictions) {
                Check.notNull(restriction,
                        "ArgumentWord restriction cannot be null, you can pass 'null' instead of an empty array");
            }
        }

        this.restrictions = restrictions;
        return this;
    }

    @Override
    public @NotNull String parse(@NotNull StringReader input) throws CommandException {
        String result = input.readUnquotedString();

        if (hasRestrictions()){
            for (String restriction : restrictions){
                if (result.equals(restriction)){
                    return result;
                }
            }
            throw CommandException.COMMAND_UNKNOWN_ARGUMENT.generateException(input);
        }

        return result;
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        if (restrictions != null) {

            // Create a primitive array for mapping
            DeclareCommandsPacket.Node[] nodes = new DeclareCommandsPacket.Node[this.restrictions.length];

            // Create a node for each restriction as a literal
            for (int i = 0; i < nodes.length; i++) {
                DeclareCommandsPacket.Node argumentNode = new DeclareCommandsPacket.Node();

                argumentNode.flags = DeclareCommandsPacket.getFlag(DeclareCommandsPacket.NodeType.LITERAL,
                        executable, false, false);
                argumentNode.name = this.restrictions[i];
                nodes[i] = argumentNode;

            }
            nodeMaker.addNodes(nodes);
        } else {
            // Can be any word, add only one argument node
            DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(this, executable, false, false);
            argumentNode.parser = "brigadier:string";
            argumentNode.properties = BinaryWriter.makeArray(packetWriter -> {
                packetWriter.writeVarInt(0); // Single word
            });
            nodeMaker.addNodes(argumentNode);
        }
    }

    /**
     * Gets if this argument allow complete freedom in the word choice or if a list has been defined.
     *
     * @return true if the word selection is restricted
     */
    public boolean hasRestrictions() {
        return restrictions != null && restrictions.length > 0;
    }

    /**
     * Gets all the word restrictions.
     *
     * @return the word restrictions, can be null
     */
    @Nullable
    public String[] getRestrictions() {
        return restrictions;
    }

    @Override
    public String toString() {
        return String.format("Word<%s>", getId());
    }
}
