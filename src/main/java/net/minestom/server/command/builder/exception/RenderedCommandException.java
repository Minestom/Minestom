package net.minestom.server.command.builder.exception;

import net.kyori.adventure.text.Component;
import net.minestom.server.command.FixedStringReader;
import org.jetbrains.annotations.NotNull;

/**
 * A CommandException that can be rendered as a component.
 */
public class RenderedCommandException extends CommandException {

    private final Component component;

    /**
     * Creates a new RenderedCommandException with the provided message, error code, string reader, and component.
     */
    public RenderedCommandException(@NotNull String message, int errorCode,
                                    @NotNull FixedStringReader stringReader, @NotNull Component component){
        super(message, errorCode, stringReader);
        this.component = component;
    }

    /**
     * Creates a new RenderedCommandException with the provided error code, string reader, and component.
     */
    public RenderedCommandException(int errorCode, @NotNull FixedStringReader stringReader, @NotNull Component component){
        super(errorCode, stringReader);
        this.component = component;
    }

    /**
     * @return the component that this exception will render
     */
    public @NotNull Component getComponent() {
        return component;
    }
}
