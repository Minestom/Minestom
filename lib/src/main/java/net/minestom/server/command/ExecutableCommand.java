package net.minestom.server.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandData;
import net.minestom.server.command.builder.CommandSyntax;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface ExecutableCommand {
    Result execute(CommandSender sender);

    interface Result {
        Type type();

        CommandData commandData();

        enum Type {
            /**
             * Command executed successfully.
             */
            SUCCESS,
            /**
             * Command cancelled by an event listener.
             */
            CANCELLED,
            /**
             * Either {@link Command#getCondition()} or {@link CommandSyntax#getCommandCondition()} failed
             */
            PRECONDITION_FAILED,
            /**
             * Command couldn't be executed because of a syntax error
             */
            INVALID_SYNTAX,
            /**
             * The command executor threw an exception
             */
            EXECUTOR_EXCEPTION,
            /**
             * Unknown command
             */
            UNKNOWN
        }
    }
}
