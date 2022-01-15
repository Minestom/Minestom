package demo.commands;

import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.CommandManager;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.entity.Player;

public class BookCommand extends Command {
    public BookCommand() {
        super("book");

        setCondition(Conditions::playerOnly);
        setDefaultExecutor(CommandManager.STANDARD_DEFAULT_EXECUTOR);
        addSyntax((origin, context) -> {
            Player player = (Player) origin.sender();

            player.openBook(Book.builder()
                    .author(Component.text(player.getUsername()))
                    .title(Component.text(player.getUsername() + "'s Book"))
                    .pages(Component.text("Page one", NamedTextColor.RED),
                            Component.text("Page two", NamedTextColor.GREEN),
                            Component.text("Page three", NamedTextColor.BLUE)));
        });

    }
}
