package net.minestom.server.command.builder.arguments.minecraft;

import com.google.gson.JsonParseException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import org.jetbrains.annotations.NotNull;

public class ArgumentComponent extends Argument<Component> {

    public static final int INVALID_JSON_ERROR = 1;

    public ArgumentComponent(@NotNull String id) {
        super(id, true);
    }

    @NotNull
    @Override
    public Component parse(@NotNull String input) throws ArgumentSyntaxException {
        try {
            return GsonComponentSerializer.gson().deserialize(input);
        } catch (JsonParseException e) {
            throw new ArgumentSyntaxException("Invalid JSON", input, INVALID_JSON_ERROR);
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
