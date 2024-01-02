package net.minestom.demo.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.entity.Player;
import net.minestom.server.scoreboard.Sidebar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SidebarCommand extends Command {
    private final Sidebar sidebar = new Sidebar(Component.text("DEMO").decorate(TextDecoration.BOLD));
    private int currentLine = 0;

    public SidebarCommand() {
        super("sidebar");

        addLine("BLANK ", Sidebar.NumberFormat.blank());
        addLine("STYLE ", Sidebar.NumberFormat.styled(Component.empty().decorate(TextDecoration.STRIKETHROUGH).color(NamedTextColor.GRAY)));
        addLine("FIXED ", Sidebar.NumberFormat.fixed(Component.text("FIXED").color(NamedTextColor.GRAY)));
        addLine("NULL ", null);

        setDefaultExecutor((source, args) -> source.sendMessage(Component.text("Unknown syntax (note: title must be quoted)")));
        setCondition(Conditions::playerOnly);

        var option = ArgumentType.Word("option").from("add-line", "remove-line", "set-title", "toggle", "update-content", "update-score");
        var content = ArgumentType.String("content").setDefaultValue("");
        var targetLine = ArgumentType.Integer("target line").setDefaultValue(-1);

        addSyntax(this::handleSidebar, option);
        addSyntax(this::handleSidebar, option, content);
        addSyntax(this::handleSidebar, option, content, targetLine);
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

    private void addLine(@NotNull String content, @Nullable Sidebar.NumberFormat numberFormat) {
        if (currentLine < 16) {
            sidebar.createLine(new Sidebar.ScoreboardLine(String.valueOf(currentLine), Component.text(content).color(NamedTextColor.WHITE), currentLine, numberFormat));
            currentLine++;
        }
    }

    private void removeLine() {
        if (currentLine > 0) {
            sidebar.removeLine(String.valueOf(currentLine));
            currentLine--;
        }
    }

    private void setTitle(@NotNull String title) {
        sidebar.setTitle(Component.text(title).decorate(TextDecoration.BOLD));
    }

    private void toggleSidebar(Player player) {
        if (sidebar.getViewers().contains(player)) sidebar.removeViewer(player);
        else sidebar.addViewer(player);
    }

    private void updateLineContent(@NotNull String content, @NotNull String lineId) {
        sidebar.updateLineContent(lineId, Component.text(content).color(NamedTextColor.WHITE));
    }

    private void updateLineScore(int score, @NotNull String lineId) {
        sidebar.updateLineScore(lineId, score);
    }
}
