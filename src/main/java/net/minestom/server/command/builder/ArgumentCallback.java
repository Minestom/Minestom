package net.minestom.server.command.builder;

import net.minestom.server.command.CommandSender;

public interface ArgumentCallback {
    void apply(CommandSender source, String value, int error);
}
