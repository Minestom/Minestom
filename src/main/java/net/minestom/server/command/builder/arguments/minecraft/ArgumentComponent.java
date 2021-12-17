package net.minestom.server.command.builder.arguments.minecraft;

import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minestom.server.command.StringReader;
import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.CommandException;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Field;

public class ArgumentComponent extends Argument<Component> {

    private static Field posField = null;

    public ArgumentComponent(@NotNull String id) {
        super(id);
    }

    @Override
    public @NotNull Component parse(@NotNull StringReader input) throws CommandException {

        JsonReader reader = new JsonReader(new java.io.StringReader(input.unread()));

        if (posField == null) {
            try {
                posField = JsonReader.class.getDeclaredField("pos");
            } catch (NoSuchFieldException exception) {
                throw CommandException.ARGUMENT_COMPONENT_INVALID.generateException(input, exception.getMessage());
            }
            posField.setAccessible(true);
        }

        var adapter = GsonComponentSerializer.gson().serializer().getAdapter(Component.class);

        try {
            Component read = adapter.read(reader);
            input.position(input.position() + posField.getInt(reader));
            return read;
        } catch (IllegalAccessException | IOException | JsonParseException exception) {
            throw CommandException.ARGUMENT_COMPONENT_INVALID.generateException(input, exception.getMessage());
        }
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(this, executable, false, false);

        argumentNode.parser = "minecraft:component";

        nodeMaker.addNodes(new DeclareCommandsPacket.Node[]{argumentNode});
    }

    @Override
    public String toString() {
        return String.format("Component<%s>", getId());
    }
}
