package demo.commands;

import net.kyori.adventure.text.Component;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.condition.conditions.MaxUsageCondition;
import org.jetbrains.annotations.NotNull;

public class TestCommand extends Command {
    ArgumentString str = new ArgumentString("str");

    public TestCommand() {
        super("testcmd");
        setDefaultExecutor(this::defaultExecutor);

        addConditions(new MaxUsageCondition(5));
        addConditionalSyntax(new MaxUsageCondition(2), this::syntaxExecutor, str);
    }

    private void syntaxExecutor(@NotNull CommandSender commandSender, @NotNull CommandContext context) {
        commandSender.sendMessage(context.get(str));
    }

    private void defaultExecutor(CommandSender sender, CommandContext context) {
        sender.sendMessage(Component.text("Can be used " + getCondition(MaxUsageCondition.class).getRemainingUsage() + "times"));
    }
}
