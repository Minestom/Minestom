package net.minestom.server.command.builder.arguments.minecraft;

import it.unimi.dsi.fastutil.chars.CharArrayList;
import it.unimi.dsi.fastutil.chars.CharList;
import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.temporal.TemporalUnit;

/**
 * Represents an argument giving a time (day/second/tick).
 * <p>
 * Example: 50d, 25s, 75t
 */
public class ArgumentTime extends Argument<Duration> {

    public static final int INVALID_TIME_FORMAT = -2;
    public static final int NO_NUMBER = -3;

    private static final CharList SUFFIXES = new CharArrayList(new char[]{'d', 's', 't'});

    public ArgumentTime(String id) {
        super(id);
    }

    @NotNull
    @Override
    public Duration parse(@NotNull String input) throws ArgumentSyntaxException {
        final char lastChar = input.charAt(input.length() - 1);

        TemporalUnit timeUnit;
        if (Character.isDigit(lastChar))
            timeUnit = TimeUnit.SERVER_TICK;
        else if (SUFFIXES.contains(lastChar)) {
            input = input.substring(0, input.length() - 1);

            if (lastChar == 'd') {
                timeUnit = TimeUnit.DAY;
            } else if (lastChar == 's') {
                timeUnit = TimeUnit.SECOND;
            } else if (lastChar == 't') {
                timeUnit = TimeUnit.SERVER_TICK;
            } else {
                throw new ArgumentSyntaxException("Time needs to have the unit d, s, t, or none", input, NO_NUMBER);
            }
        } else
            throw new ArgumentSyntaxException("Time needs to have a unit", input, NO_NUMBER);

        try {
            // Check if value is a number
            final int time = Integer.parseInt(input);
            return Duration.of(time, timeUnit);
        } catch (NumberFormatException e) {
            throw new ArgumentSyntaxException("Time needs to be a number", input, NO_NUMBER);
        }

    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(this, executable, false, false);
        argumentNode.parser = "minecraft:time";

        nodeMaker.addNodes(new DeclareCommandsPacket.Node[]{argumentNode});
    }

    @Override
    public String toString() {
        return String.format("Time<%s>", getId());
    }
}
