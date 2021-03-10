package demo.commands;

import net.minestom.server.chat.ChatColor;
import net.minestom.server.chat.ColoredText;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Arguments;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

import static net.minestom.server.command.builder.arguments.ArgumentType.Integer;
import static net.minestom.server.command.builder.arguments.ArgumentType.Word;

public class TestCommand extends Command {

    public TestCommand() {
        super("testcmd");
        setDefaultExecutor(this::usage);

        addSubcommand(new Sub());
    }

    private void usage(CommandSender sender, Arguments arguments) {
        sender.sendMessage("Incorrect usage");
    }

    private static class Sub extends Command {

        public Sub() {
            super("sub");

            var test1 = Word("msg").setSuggestionCallback((sender, context, suggestion) -> {
                final String input = suggestion.getInput();
                if (!input.isEmpty()) {
                    int num = Integer.valueOf(input) * 2;
                    suggestion.addEntry(new SuggestionEntry(String.valueOf(num), ColoredText.of(ChatColor.RED, "Hover")));
                    System.out.println("test: "+context.get("msg3"));
                }
            });

            var test3 = Integer("msg3");

            addSyntax((sender, context) -> {
                System.out.println("input: " + context.getInput());
            }, test3, test1);
        }
    }

}
