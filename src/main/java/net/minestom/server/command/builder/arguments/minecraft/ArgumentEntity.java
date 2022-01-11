package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.command.StringReader;
import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.CommandException;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.entity.EntityFinder;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the target selector argument.
 * https://minecraft.gamepedia.com/Commands#Target_selectors
 */
public class ArgumentEntity extends Argument<EntityFinder> {

    private boolean onlySingleEntity;
    private boolean onlyPlayers;

    public ArgumentEntity(@NotNull String id) {
        super(id);
    }

    public ArgumentEntity singleEntity(boolean singleEntity) {
        this.onlySingleEntity = singleEntity;
        return this;
    }

    public ArgumentEntity onlyPlayers(boolean onlyPlayers) {
        this.onlyPlayers = onlyPlayers;
        return this;
    }

    @Override
    public @NotNull EntityFinder parse(@NotNull StringReader input) throws CommandException {
        // FIXME: Complete
        throw CommandException.COMMAND_UNKNOWN_ARGUMENT.generateException(input.all(), input.position());
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(this, executable, false, false);
        argumentNode.parser = "minecraft:entity";
        argumentNode.properties = BinaryWriter.makeArray(packetWriter -> {
            byte mask = 0;
            if (this.isOnlySingleEntity()) {
                mask |= 0x01;
            }
            if (this.isOnlyPlayers()) {
                mask |= 0x02;
            }
            packetWriter.writeByte(mask);
        });

        nodeMaker.addNodes(argumentNode);
    }

    public boolean isOnlySingleEntity() {
        return onlySingleEntity;
    }

    public boolean isOnlyPlayers() {
        return onlyPlayers;
    }

    @Override
    public String toString() {
        if (onlySingleEntity) {
            if (onlyPlayers) {
                return String.format("Player<%s>", getId());
            }
            return String.format("Entity<%s>", getId());
        }
        if (onlyPlayers) {
            return String.format("Players<%s>", getId());
        }
        return String.format("Entities<%s>", getId());
    }
}
