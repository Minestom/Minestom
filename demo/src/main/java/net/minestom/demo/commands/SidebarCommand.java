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
import net.minestom.server.scoreboard.NumberFormat;
import net.minestom.server.scoreboard.Sidebar;
import org.jetbrains.annotations.Nullable;

public class SidebarCommand extends Command {
    private final Sidebar sidebar = new Sidebar(Component.text("DEMO").decorate(TextDecoration.BOLD));
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
    }


    private void handleSidebar(CommandSender source, CommandContext context) {
        Player player = (Player) source;
        String option = context.get("option");
        String content = context.get("content");
        int targetLine = context.get("target line");
        if (targetLine == -1) targetLine = currentLine;
        Sidebar.LineIdentifier id = new Sidebar.LineIdentifier(String.valueOf(targetLine));
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
                updateLineContent(content, id);
                break;
            case "update-score":
                updateLineScore(Integer.parseInt(content), id);
                break;
        }
    }

    private void addLine(String content, @Nullable NumberFormat numberFormat) {
        if (currentLine < 16) {
            sidebar.addLine(new Sidebar.LineIdentifier(String.valueOf(currentLine)), new Sidebar.LineContent(
                    Component.text(content).color(NamedTextColor.WHITE),
                    numberFormat,
                    16 - currentLine
            ));
            currentLine++;
        }
    }

    private void removeLine() {
        if (currentLine > 0) {
            sidebar.removeLine(new Sidebar.LineIdentifier(String.valueOf(currentLine)));
            currentLine--;
        }
    }

    private void setTitle(String title) {
        sidebar.setTitle(Component.text(title).decorate(TextDecoration.BOLD));
    }

    private void toggleSidebar(Player player) {
        if (sidebar.getViewers().contains(player)) sidebar.removeViewer(player);
        else sidebar.addViewer(player);
    }

    private void updateLineContent(String content, Sidebar.LineIdentifier id) {
        Sidebar.LineContent current = sidebar.getLineContent(id);
        sidebar.setLineContent(id, new Sidebar.LineContent(
                Component.text(content).color(NamedTextColor.WHITE),
                current.numberFormat(),
                current.score()
        ));
    }

    private void updateLineScore(int score, Sidebar.LineIdentifier id) {
        Sidebar.LineContent current = sidebar.getLineContent(id);
        sidebar.setLineContent(id, new Sidebar.LineContent(
                current.content(),
                current.numberFormat(),
                score
        ));
    }
}
