package net.minestom.server.command.builder.arguments.minecraft;

import it.unimi.dsi.fastutil.chars.CharArrayList;
import it.unimi.dsi.fastutil.chars.CharList;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.StringReader;
import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.command.builder.exception.CommandException;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import net.minestom.server.utils.time.Tick;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.temporal.TemporalUnit;

/**
 * An argument that returns a number of ticks (as a Long)
 * <p>
 * Example: "50d" returns 1200000, 25s returns 500, and 75t returns 75.
 */
public class ArgumentTime extends Argument<Long> {

    /**
     * Static constant that represents the default value for the number of ticks per day. This is defined as 20 minutes
     * converted to server ticks.
     */
    public static final long TICKS_PER_DAY = Tick.SERVER_TICKS.fromDuration(Duration.ofMinutes(20));

    public static final int INVALID_TIME_FORMAT = -2;
    public static final int NO_NUMBER = -3;

    private static final CharList SUFFIXES = new CharArrayList(new char[]{'d', 's', 't'});

    public ArgumentTime(String id) {
        super(id);
    }

    @Override
    public @NotNull Long parse(@NotNull StringReader input) throws CommandException {

        double amount = input.readDouble();

        if (amount < 0){
            throw CommandException.ARGUMENT_TIME_INVALID_TICK_COUNT.generateException(input);
        }

        String stringUnit = input.readUnquotedString();

        return switch (stringUnit) {
            case "d" -> Math.round(TICKS_PER_DAY * amount);
            case "s" -> Math.round(MinecraftServer.TICK_PER_SECOND * amount);
            case "t", "" -> Math.round(amount);
            default -> throw CommandException.ARGUMENT_TIME_INVALID_UNIT.generateException(input);
        };
    }

    @NotNull
    @Override
    public Long parse(@NotNull String input) throws ArgumentSyntaxException {
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
            return Duration.of(time, timeUnit).toMillis() / MinecraftServer.TICK_MS;
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
