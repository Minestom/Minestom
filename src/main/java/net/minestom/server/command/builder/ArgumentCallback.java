package net.minestom.server.command.builder;

import net.minestom.server.command.CommandSender;

/**
 * Callback executed when an error is found within the argument
 */
public interface ArgumentCallback {

    /**
     * Executed when an error is found
     *
     * @param source the sender which executed the command
     * @param value  the raw argument which gave the error
     * @param error  the error id (you can check its meaning in the specific argument class)
     */
    void apply(CommandSender source, String value, int error);
}
