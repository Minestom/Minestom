package net.minestom.server.command.builder.arguments.minecraft;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.Result;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.command.ArgumentParserType;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.registry.RegistryTranscoder;
import net.minestom.server.utils.json.JsonUtil;
import org.jetbrains.annotations.NotNull;

public class ArgumentComponent extends Argument<Component> {
    public static final int INVALID_JSON_ERROR = 1;

    public ArgumentComponent(@NotNull String id) {
        super(id, true);
    }

    @NotNull
    @Override
    public Component parse(@NotNull CommandSender sender, @NotNull String input) throws ArgumentSyntaxException {
        try {
            final Transcoder<JsonElement> coder = new RegistryTranscoder<>(Transcoder.JSON, MinecraftServer.process());
            final Result<Component> result = Codec.COMPONENT.decode(coder, JsonUtil.fromJson(input));
            return switch (result) {
                case Result.Ok(var component) -> component;
                case Result.Error(var message) ->
                        throw new ArgumentSyntaxException("Failed to parse component: " + message, input, INVALID_JSON_ERROR);
            };
        } catch (JsonParseException e) {
            throw new ArgumentSyntaxException("Invalid JSON", input, INVALID_JSON_ERROR);
        }
    }

    @Override
    public ArgumentParserType parser() {
        return ArgumentParserType.COMPONENT;
    }

    @Override
    public String toString() {
        return String.format("Component<%s>", getId());
    }
}
