package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.command.StringReader;
import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.CommandException;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import net.minestom.server.utils.NBTUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.*;

public class ArgumentBlockState extends Argument<Block> {

    public static final int OPEN_PROPERTIES = '[', SET_PROPERTY = '=', PROPERTY_SEPARATOR = ',', CLOSE_PROPERTIES = ']', TAG_MARKER = '#';

    public ArgumentBlockState(@NotNull String id) {
        super(id);
    }

    @Override
    public @NotNull Block parse(@NotNull StringReader input) throws CommandException {
        return read(input);
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(this, executable, false, false);
        argumentNode.parser = "minecraft:block_state";

        nodeMaker.addNodes(argumentNode);
    }

    public static @NotNull Block read(@NotNull StringReader input) throws CommandException {
        if (input.canRead() && input.peek() == TAG_MARKER) {
            throw CommandException.ARGUMENT_BLOCK_TAG_DISALLOWED.generateException(input.all(), input.position());
        }
        int start = input.position();
        Block block = Block.fromNamespaceId(input.readNamespaceID());

        // If the block is invalid, throw an exception and place the reader back where it was.
        if (block == null) {
            String text = input.all().substring(start, input.position());
            input.position(start);
            throw CommandException.ARGUMENT_BLOCK_ID_INVALID.generateException(input.all(), start, text);
        }
        if (input.canRead() && input.peek() == OPEN_PROPERTIES) {
            var specifiedProperties = readSpecifiedProperties(input, block, start);
            var states = filterValidBlocks(specifiedProperties, block, input, start);

            if (states.isEmpty()) {
                input.position(start);
                throw CommandException.COMMAND_UNKNOWN_ARGUMENT.generateException(input.all(), start);
            }

            Block lowestState = null;
            for (var value : states) {
                if (lowestState == null || value.stateId() < lowestState.stateId()) {
                    lowestState = value;
                }
            }
            block = lowestState;
        }

        if (input.canRead() && input.peek() == '{') {
            @SuppressWarnings("deprecation")
            NBT nbt = NBTUtils.readSNBT(input);
            if (!(nbt instanceof NBTCompound compound)) {
                input.position(start);
                throw CommandException.COMMAND_UNKNOWN_ARGUMENT.generateException(input.all(), start);
            }
            block = block.withNbt(compound);
        }

        return block;
    }

    public static @UnknownNullability Collection<Block> filterValidBlocks(@NotNull List<Map.Entry<String, String>> specifiedProperties,
                                                                          @NotNull Block block, @NotNull StringReader input,
                                                                          int start) throws CommandException {
        // TODO: Improve this when the 1.18 branch is merged.
        Map<Map<String, String>, Block> stateMap = new HashMap<>();
        for (Block state : block.possibleStates()) {
            stateMap.put(state.properties(), state);
        }

        for (var entry : specifiedProperties) {
            boolean hasKey = false;
            var iterator = stateMap.keySet().iterator();
            while (iterator.hasNext()) {
                var get = iterator.next().get(entry.getKey());
                if (get == null) {
                    iterator.remove();
                } else if (!get.equals(entry.getValue())) {
                    hasKey = true;
                    iterator.remove();
                }
            }
            if (stateMap.isEmpty()) {
                input.position(start);
                if (hasKey) {
                    throw CommandException.ARGUMENT_BLOCK_PROPERTY_INVALID.generateException(input.all(), start, block.name(), entry.getValue(), entry.getKey());
                } else {
                    throw CommandException.ARGUMENT_BLOCK_PROPERTY_UNKNOWN.generateException(input.all(), start, entry.getKey(), block.name());
                }
            }
        }
        return stateMap.values();
    }

    public static @UnknownNullability List<Map.Entry<String, String>> readSpecifiedProperties(@NotNull StringReader input,
                                                                                              @NotNull Block block, int start) throws CommandException {
        if (!input.canRead() || input.peek() != OPEN_PROPERTIES) {
            return null;
        }
        input.skip();
        input.skipWhitespace();

        List<Map.Entry<String, String>> entries = new ArrayList<>();

        while (input.canRead() && input.peek() != CLOSE_PROPERTIES) {
            input.skipWhitespace();
            String key = input.readString();

            for (var entry : entries) {
                if (entry.getKey().equals(key)) {
                    input.position(start);
                    throw CommandException.ARGUMENT_BLOCK_PROPERTY_DUPLICATE.generateException(input.all(), start, key, block.name());
                }
            }

            input.skipWhitespace();
            if (!input.canRead() || input.peek() != SET_PROPERTY) {
                input.position(start);
                throw CommandException.ARGUMENT_BLOCK_PROPERTY_NOVALUE.generateException(input.all(), start, key, block.name());
            }
            input.skip();
            input.skipWhitespace();

            entries.add(Map.entry(key, input.readString()));
            input.skipWhitespace();

            if (!input.canRead()) {
                input.position(start);
                throw CommandException.ARGUMENT_BLOCK_PROPERTY_UNCLOSED.generateException(input.all(), start);
            }

            if (input.peek() == CLOSE_PROPERTIES) {
                input.skip();
                break;
            }
            if (input.peek() != PROPERTY_SEPARATOR) {
                throw CommandException.ARGUMENT_BLOCK_PROPERTY_UNCLOSED.generateException(input.all(), start);
            }
            input.skip();
        }

        return entries;
    }

    @Override
    public String toString() {
        return String.format("BlockState<%s>", getId());
    }
}
