package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.color.TeamColor;
import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import org.jetbrains.annotations.NotNull;

/**
 * An argument that will give you a {@link TeamColor} from it's name or the int code.
 */
public class ArgumentTeamColor extends Argument<TeamColor> {
    public static final int UNDEFINED_TEAM_FORMAT = -2;

    public ArgumentTeamColor(@NotNull String id) {
        super(id);
    }

    @Override
    public @NotNull TeamColor parse(@NotNull String input) throws ArgumentSyntaxException {
        try {
            return TeamColor.valueOf(input.toUpperCase().trim().replace(' ', '_'));
        } catch (IllegalArgumentException ignored) {
            try {
                return TeamColor.values()[Integer.parseInt(input)];
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
