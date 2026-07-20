package net.minestom.demo.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.entity.Player;
import net.minestom.server.scoreboard.Sidebar;

import java.util.ArrayList;

import static net.minestom.server.command.builder.arguments.ArgumentType.Literal;

public class SidebarCommand extends Command {
    private final Sidebar sidebar;

    public SidebarCommand() {
        super("sidebar");
        this.sidebar = Sidebar.create(Component.text("DEMO").decorate(TextDecoration.BOLD));
        this.setCondition(Conditions::playerOnly);
        this.setDefaultExecutor((source, _) -> source.sendMessage(Component.text("Usage: /sidebar toggle|add|remove|title|set")));

        var content = ArgumentType.Component("content");
        var line = ArgumentType.Integer("line");

        this.addSyntax((source, _) -> {
            var player = (Player) source;

            if (this.sidebar.isViewer(player)) {
                this.sidebar.removeViewer(player);
            } else {
                this.sidebar.addViewer(player);
            }
        }, Literal("toggle"));

        this.addSyntax((source, context) -> {
            var lines = new ArrayList<>(this.sidebar.getLines());

            if (lines.size() >= Sidebar.MAX_LINES) {
                source.sendMessage(Component.text("The sidebar is full"));
                return;
            }

            lines.add(context.get(content));
            this.sidebar.update(lines);
        }, Literal("add"), content);

        this.addSyntax((_, _) -> {
            var lines = new ArrayList<>(this.sidebar.getLines());

            if (lines.isEmpty()) {
                return;
            }

            lines.removeLast();
            this.sidebar.update(lines);
        }, Literal("remove"));

        this.addSyntax((source, context) -> {
            int index = context.get(line);
            var lines = new ArrayList<>(this.sidebar.getLines());

            if (index < 0 || index >= lines.size()) {
                source.sendMessage(Component.text("No line at index " + index));
                return;
            }

            lines.remove(index);
            this.sidebar.update(lines);
        }, Literal("remove"), line);

        this.addSyntax((_, context) -> this.sidebar.setTitle(context.get(content)), Literal("title"), content);

        this.addSyntax((source, context) -> {
            int index = context.get(line);

            if (index < 0 || index >= this.sidebar.getLines().size()) {
                source.sendMessage(Component.text("No line at index " + index));
                return;
            }

            this.sidebar.setLine(index, context.get(content));
        }, Literal("set"), line, content);
    }
}
