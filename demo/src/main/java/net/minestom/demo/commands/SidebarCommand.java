package net.minestom.demo.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.scoreboard.NumberFormat;
import net.minestom.server.scoreboard.ScoreEntry;
import net.minestom.server.scoreboard.Scoreboard;
import org.jetbrains.annotations.Nullable;

public class SidebarCommand extends Command {
    private final Scoreboard sidebar = Scoreboard.create("demo", Component.text("DEMO").decorate(TextDecoration.BOLD));
    private int currentLine = 0;

    public SidebarCommand() {
        super("sidebar");

        addLine("BLANK ", NumberFormat.blank());
        addLine("STYLE ", NumberFormat.styled(Component.empty().decorate(TextDecoration.STRIKETHROUGH).color(NamedTextColor.GRAY)));
        addLine("FIXED ", NumberFormat.fixed(Component.text("FIXED").color(NamedTextColor.GRAY)));
        addLine("NULL ", null);

        setDefaultExecutor((source, args) -> source.sendMessage(Component.text("Unknown syntax (note: title must be quoted)")));
        setCondition(Conditions::playerOnly);

        var option = ArgumentType.Word("option").from("add-line", "remove-line", "set-title", "toggle", "update-content", "update-score");
        var content = ArgumentType.String("content").setDefaultValue("");
        var targetLine = ArgumentType.Integer("target line").setDefaultValue(-1);

        addSyntax(this::handleSidebar, option);
        addSyntax(this::handleSidebar, option, content);
        addSyntax(this::handleSidebar, option, content, targetLine);

        MinecraftServer.getGlobalEventHandler().addListener(
                PlayerDisconnectEvent.class,
                event -> sidebar.removeViewer(event.getPlayer())
        );
    }

    private void handleSidebar(CommandSender source, CommandContext context) {
        Player player = (Player) source;
        String option = context.get("option");
        String content = context.get("content");
        int targetLine = context.get("target line");
        if (targetLine == -1) targetLine = currentLine;
        switch (option) {
            case "add-line":
                addLine(content, null);
                break;
            case "remove-line":
                removeLine();
                break;
            case "set-title":
                setTitle(content);
                break;
            case "toggle":
                toggleSidebar(player);
                break;
            case "update-content":
                updateLineContent(content, String.valueOf(targetLine));
                break;
            case "update-score":
                updateLineScore(Integer.parseInt(content), String.valueOf(targetLine));
                break;
        }
    }

    private void addLine(String content, @Nullable NumberFormat numberFormat) {
        if (currentLine < 16) {
            sidebar.updateEntry(String.valueOf(currentLine), new ScoreEntry(currentLine, Component.text(content).color(NamedTextColor.WHITE), numberFormat));
            currentLine++;
        }
    }

    private void removeLine() {
        if (currentLine > 0) {
            sidebar.removeEntry(String.valueOf(currentLine));
            currentLine--;
        }
    }

    private void setTitle(String title) {
        sidebar.setDisplayName(Component.text(title).decorate(TextDecoration.BOLD));
    }

    private void toggleSidebar(Player player) {
        if (sidebar.getViewers().containsKey(player)) sidebar.removeViewer(player);
        else sidebar.addViewer(player, Scoreboard.Position.SIDEBAR);
    }

    private void updateLineContent(String content, String lineId) {
        sidebar.updateDisplayName(lineId, Component.text(content).color(NamedTextColor.WHITE));
    }

    private void updateLineScore(int score, String lineId) {
        sidebar.updateScore(lineId, score);
    }
}
