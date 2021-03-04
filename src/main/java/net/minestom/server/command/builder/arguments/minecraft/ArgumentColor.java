package net.minestom.server.command.builder.arguments.minecraft;

import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.color.Color;
import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an argument that will give you a {@link Color}. Input is parsed
 * first as a hex string ({@code #int}), then as a CSS hex string ({@code #rrggbb} or
 * {@code #rgb}), then as an integer and finally as a named text colour. The values for
 * the named text colours can be found in {@link NamedTextColor}.
 * <br><br>
 * This class is essentially a wrapper around {@link ArgumentTextColor}.
 */
public class ArgumentColor extends Argument<Color> {
    private final ArgumentTextColor argumentTextColor;

    public static int UNDEFINED_COLOR = ArgumentTextColor.UNDEFINED_COLOR;

    public ArgumentColor(@NotNull String id) {
        super(id);
        argumentTextColor = new ArgumentTextColor(id);
    }

    @Override
    public @NotNull Color parse(@NotNull String input) throws ArgumentSyntaxException {
        return new Color(this.argumentTextColor.parse(input));
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(this, executable, false, false);
        argumentNode.parser = "minecraft:color";

        nodeMaker.addNodes(new DeclareCommandsPacket.Node[]{argumentNode});
    }
}
