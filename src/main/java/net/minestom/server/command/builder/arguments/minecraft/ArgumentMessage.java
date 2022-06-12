package net.minestom.server.command.builder.arguments.minecraft;

import net.kyori.adventure.text.Component;
import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.crypto.MessageSignature;
import net.minestom.server.crypto.SignatureValidator;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import org.jetbrains.annotations.NotNull;

/**
 * This argument type enables chat preview of the entire command while editing
 * the node with this type. Although the protocol allows multiple signed arguments this is the only
 * type that supports it and the clientside parser takes the remaining string for this type, so
 * it is impossible to have any more arguments after this one.<p>
 * The signature verification happens by first acquiring a {@link net.minestom.server.crypto.SignatureValidator
 * SignatureValidator} for the sender by using {@link net.minestom.server.crypto.SignatureValidator#from(Player)},
 * after that the verification happens with {@link net.minestom.server.crypto.SignatureValidator#validate(SignatureValidator, MessageSignature, Component)}
 * where the component is either the string value of this argument wrapped in {@link Component#text(String)} or
 * {@link Player#getLastPreviewedMessage()} depending on {@link net.minestom.server.command.ArgumentsSignature#signedPreview()}
 */
public final class ArgumentMessage extends Argument<String> implements SignableArgument {
    public ArgumentMessage(String id) {
        super(id);
    }

    @Override
    public @NotNull String parse(@NotNull String input) throws ArgumentSyntaxException {
        return input;
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(this, executable, false, false);
        argumentNode.parser = "minecraft:message";
        nodeMaker.addNodes(new DeclareCommandsPacket.Node[]{argumentNode});
    }
}
