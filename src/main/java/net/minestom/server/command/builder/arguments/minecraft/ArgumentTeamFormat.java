package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.color.TeamFormat;
import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import org.jetbrains.annotations.NotNull;

/**
 * An argument that will give you a {@link TeamFormat} from it's name or the int code.
 */
public class ArgumentTeamFormat extends Argument<TeamFormat> {
    public static final int UNDEFINED_TEAM_FORMAT = -2;

    public ArgumentTeamFormat(@NotNull String id) {
        super(id);
    }

    @Override
    public @NotNull TeamFormat parse(@NotNull String input) throws ArgumentSyntaxException {
        try {
            return TeamFormat.valueOf(input.toUpperCase().trim().replace(' ', '_'));
        } catch (IllegalArgumentException ignored) {
            try {
                return TeamFormat.values()[Integer.parseInt(input)];
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException alsoIgnored) {
                throw new ArgumentSyntaxException("Undefined team format!", input, UNDEFINED_TEAM_FORMAT);
            }
        }
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(this, executable, false, false);
        argumentNode.parser = "minecraft:team_format";

        nodeMaker.addNodes(new DeclareCommandsPacket.Node[]{argumentNode});
    }
}
