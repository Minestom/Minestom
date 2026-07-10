package net.minestom.server.command.builder.arguments;

import net.minestom.server.command.builder.arguments.minecraft.ArgumentComponent;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentItemStack;

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
