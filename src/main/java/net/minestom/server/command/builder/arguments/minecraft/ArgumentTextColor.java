package net.minestom.server.command.builder.arguments.minecraft;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import net.minestom.server.utils.MathUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an argument that will give you a {@link TextColor}. Input is parsed
 * first as a hex string ({@code #int}), then as a CSS hex string ({@code #rrggbb} or
 * {@code #rgb}), then as an integer and finally as a named text colour. The values for
 * the named text colours can be found in {@link NamedTextColor}.
 */
public class ArgumentTextColor extends Argument<TextColor> {

    public static final int UNDEFINED_COLOR = -2;

    public ArgumentTextColor(@NotNull String id) {
        super(id);
    }

    @Override
    public @NotNull TextColor parse(@NotNull String input) throws ArgumentSyntaxException {
        TextColor textColor;

        // first try standard hex
        textColor = TextColor.fromHexString(input);
        if (textColor != null) {
            return textColor;
        }

        // now try CSS hex
        textColor = TextColor.fromCSSHexString(input);
        if (textColor != null) {
            return textColor;
        }

        // now try int
        Integer number = MathUtils.tryParse(input);
        if (number != null) {
            return TextColor.color(number);
        }

        // fallback to legacy colour names
        textColor = NamedTextColor.NAMES.value(input.toLowerCase());
        if (textColor != null) {
            return textColor;
        }

        // throw an error
        throw new ArgumentSyntaxException("Undefined color", input, UNDEFINED_COLOR);
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(this, executable, false, false);
        argumentNode.parser = "minecraft:text_color";

        nodeMaker.addNodes(new DeclareCommandsPacket.Node[]{argumentNode});
    }
}
