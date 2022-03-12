package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.StringReader;
import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.CommandException;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import net.minestom.server.utils.time.Tick;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

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

    public ArgumentTime(@NotNull String id) {
        super(id);
    }

    @Override
    public @NotNull Long parse(@NotNull StringReader input) throws CommandException {
        int pos = input.position();
        double amount = input.readDouble();

        if (amount < 0){
            throw CommandException.ARGUMENT_TIME_INVALID_TICK_COUNT.generateException(input.all(), pos);
        }

        String stringUnit = input.readUnquotedString();

        return switch (stringUnit) {
            case "d" -> Math.round(TICKS_PER_DAY * amount);
            case "s" -> Math.round(MinecraftServer.TICK_PER_SECOND * amount);
            case "t", "" -> Math.round(amount);
            default -> throw CommandException.ARGUMENT_TIME_INVALID_UNIT.generateException(input.all(), pos);
        };
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(this, executable, false, false);
        argumentNode.parser = "minecraft:time";

        nodeMaker.addNodes(argumentNode);
    }

    @Override
    public String toString() {
        return String.format("Time<%s>", getId());
    }
}
