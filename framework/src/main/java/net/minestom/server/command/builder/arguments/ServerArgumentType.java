package net.minestom.server.command.builder.arguments;

import net.minestom.server.command.builder.arguments.minecraft.ArgumentComponent;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentItemStack;
import net.minestom.server.command.builder.parser.ArgumentParser;
import org.jetbrains.annotations.ApiStatus;

/**
 * Factory for {@link Argument}s whose parsing is bound to a running server
 * (command dispatch, entity finder, live registries).
 * <p>
 * These factories were previously exposed on {@link ArgumentType}; they live here
 * because the arguments they produce require framework services and therefore
 * cannot sit alongside the unopinionated structural arguments.
 * <p>
 * Please see the specific class documentation for further info.
 */
public class ServerArgumentType {

    static {
        registerFormatArguments();
    }

    /**
     * Installs the server-bound identifiers of {@link ArgumentType#generate(String)}
     * format strings. Called from server startup (and this class's initializer);
     * idempotent.
     */
    @ApiStatus.Internal
    public static void registerFormatArguments() {
        ArgumentParser.registerArgument("command", ArgumentCommand::new);
        ArgumentParser.registerArgument("entity", s -> new ArgumentEntity(s).singleEntity(true));
        ArgumentParser.registerArgument("entities", ArgumentEntity::new);
        ArgumentParser.registerArgument("player", s -> new ArgumentEntity(s).singleEntity(true).onlyPlayers(true));
        ArgumentParser.registerArgument("players", s -> new ArgumentEntity(s).onlyPlayers(true));
        ArgumentParser.registerArgument("itemstack", ArgumentItemStack::new);
        ArgumentParser.registerArgument("component", ArgumentComponent::new);
    }

    /**
     * @see ArgumentCommand
     */
    public static ArgumentCommand Command(String id) {
        return new ArgumentCommand(id);
    }

    /**
     * @see ArgumentEntity
     */
    public static ArgumentEntity Entity(String id) {
        return new ArgumentEntity(id);
    }

    /**
     * @see ArgumentItemStack
     */
    public static ArgumentItemStack ItemStack(String id) {
        return new ArgumentItemStack(id);
    }

    /**
     * @see ArgumentComponent
     */
    public static ArgumentComponent Component(String id) {
        return new ArgumentComponent(id);
    }

    /**
     * @see ArgumentEntity
     * @deprecated use {@link #Entity(String)}
     */
    @Deprecated
    public static ArgumentEntity Entities(String id) {
        return new ArgumentEntity(id);
    }
}
