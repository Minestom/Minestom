package net.minestom.server.command.builder.arguments;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.command.builder.suggestion.SuggestionCallback;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.callback.validator.StringArrayValidator;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Same as {@link ArgumentStringArray} with the exception
 * that this argument can trigger {@link net.minestom.server.command.builder.Command#onDynamicWrite(CommandSender, String)}.
 *
 * @deprecated Use {@link Argument#setSuggestionCallback(SuggestionCallback)} with any argument type you want
 */
@Deprecated
public class ArgumentDynamicStringArray extends Argument<String[]> {

    public static final int RESTRICTION_ERROR = 1;

    private StringArrayValidator dynamicRestriction;

    public ArgumentDynamicStringArray(String id) {
        super(id, true, true);
    }

    @NotNull
    @Override
    public String[] parse(@NotNull String input) throws ArgumentSyntaxException {
        final String[] value = input.split(StringUtils.SPACE);

        // true if 'value' is valid based on the dynamic restriction
        final boolean restrictionCheck = dynamicRestriction == null || dynamicRestriction.isValid(value);

        if (!restrictionCheck) {
            throw new ArgumentSyntaxException("Argument does not respect the dynamic restriction", input, RESTRICTION_ERROR);
        }

        return value;
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(this, executable, false, true);

        argumentNode.parser = "brigadier:string";
        argumentNode.properties = BinaryWriter.makeArray(packetWriter -> {
            packetWriter.writeVarInt(2); // Greedy phrase
        });
        argumentNode.suggestionsType = "minecraft:ask_server";

        nodeMaker.addNodes(new DeclareCommandsPacket.Node[]{argumentNode});
    }

    /**
     * Sets the dynamic restriction of this dynamic argument.
     * <p>
     * Will be called once the argument condition is checked.
     *
     * @param dynamicRestriction the dynamic restriction, can be null to disable
     * @return 'this' for chaining
     */
    public ArgumentDynamicStringArray fromRestrictions(@Nullable StringArrayValidator dynamicRestriction) {
        this.dynamicRestriction = dynamicRestriction;
        return this;
    }

}
