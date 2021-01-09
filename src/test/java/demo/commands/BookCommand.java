package demo.commands;

import net.minestom.server.chat.ChatColor;
import net.minestom.server.chat.ColoredText;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Arguments;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import net.minestom.server.item.metadata.WrittenBookMeta;

import java.util.List;

public class BookCommand extends Command {
    public BookCommand() {
        super("book");

        setCondition(this::playerCondition);

        setDefaultExecutor(this::execute);
    }

    private boolean playerCondition(CommandSender sender, String commandString) {
        if (!sender.isPlayer()) {
            sender.sendMessage("The command is only available for players");
            return false;
        }
        return true;
    }

    private void execute(CommandSender sender, Arguments args) {
        Player player = sender.asPlayer();

        final WrittenBookMeta bookMeta = new WrittenBookMeta();
        bookMeta.setAuthor(player.getUsername());
        bookMeta.setGeneration(WrittenBookMeta.WrittenBookGeneration.ORIGINAL);
        bookMeta.setTitle(player.getUsername() + "'s Book");
        bookMeta.setPages(List.of(
                ColoredText.of(ChatColor.RED, "Page one"),
                ColoredText.of(ChatColor.BRIGHT_GREEN, "Page two"),
                ColoredText.of(ChatColor.BLUE, "Page three")
        ));

        player.openBook(bookMeta);
    }
}
