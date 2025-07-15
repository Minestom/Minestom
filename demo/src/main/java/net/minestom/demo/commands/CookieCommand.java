package net.minestom.demo.commands;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;

public class CookieCommand extends Command {
    public CookieCommand() {
        super("cookie");

        addSubcommand(new Store());
        addSubcommand(new Fetch());
    }

    public static class Store extends Command {
        private final Argument<String> keyArg = ArgumentType.ResourceLocation("key");
        private final Argument<String[]> valueArg = ArgumentType.StringArray("value");

        public Store() {
            super("store");

            addSyntax(this::store, keyArg, valueArg);
        }

        private void store(CommandSender sender, CommandContext context) {
            if (!(sender instanceof Player player)) return;

            String key = context.get(keyArg);
            byte[] value = String.join(" ", context.get(valueArg)).getBytes();

            player.getPlayerConnection().storeCookie(key, value);
            player.sendMessage(key + " stored");
        }
    }

    public static class Fetch extends Command {
        private final Argument<String> keyArg = ArgumentType.ResourceLocation("key");

        public Fetch() {
            super("fetch");

            addSyntax(this::fetch, keyArg);
        }

        private void fetch(CommandSender sender, CommandContext context) {
            if (!(sender instanceof Player player)) return;

            String key = context.get(keyArg);

            player.getPlayerConnection().fetchCookie(key).thenAccept(value -> {
                if (value == null) {
                    player.sendMessage(key + ": null");
                } else {
                    player.sendMessage(key + ": " + new String(value));
                }
            });
        }
    }
}
