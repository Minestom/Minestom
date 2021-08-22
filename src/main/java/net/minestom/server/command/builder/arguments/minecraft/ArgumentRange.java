package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import net.minestom.server.utils.math.Range;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Abstract class used by {@link ArgumentIntRange} and {@link ArgumentFloatRange}.
 *
 * @param <T> the type of the range
 */
public abstract class ArgumentRange<T extends Range<N>, N extends Number> extends Argument<T> {

    public static final int FORMAT_ERROR = -1;
    private final N min;
    private final N max;
    private final Function<String, N> parser;
    private final String parserName;
    private final BiFunction<N, N, T> rangeConstructor;

    public ArgumentRange(@NotNull String id, String parserName, N min, N max, Function<String, N> parser, BiFunction<N, N, T> rangeConstructor) {
        super(id);
        this.min = min;
        this.max = max;
        this.parser = parser;
        this.parserName = parserName;
        this.rangeConstructor = rangeConstructor;
    }

    @NotNull
    @Override
    public T parse(@NotNull String input) throws ArgumentSyntaxException {
        try {
            final String[] split = input.split(Pattern.quote(".."), -1);

            if (split.length == 2) {
                final N min;
                final N max;
                if (split[0].length() == 0 && split[1].length() > 0) {
                    // Format ..NUMBER
                    min = this.min;
                    max = parser.apply(split[1]);
                } else if (split[0].length() > 0 && split[1].length() == 0) {
                    // Format NUMBER..
                    min = parser.apply(split[0]);
                    max = this.max;
                } else if (split[0].length() > 0) {
                    // Format NUMBER..NUMBER
                    min = parser.apply(split[0]);
                    max = parser.apply(split[1]);
                } else {
                    // Format ..
                    throw new ArgumentSyntaxException("Invalid range format", input, FORMAT_ERROR);
                }
                return rangeConstructor.apply(min, max);
            } else if (split.length == 1) {
                final N number = parser.apply(input);
                return rangeConstructor.apply(number, number);
            }
        } catch (NumberFormatException e2) {
            throw new ArgumentSyntaxException("Invalid number", input, FORMAT_ERROR);
        }
        throw new ArgumentSyntaxException("Invalid range format", input, FORMAT_ERROR);
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(this, executable, false, false);
        argumentNode.parser = parserName;

        nodeMaker.addNodes(new DeclareCommandsPacket.Node[]{argumentNode});
    }

}
