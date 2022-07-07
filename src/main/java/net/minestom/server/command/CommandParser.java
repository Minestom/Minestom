package net.minestom.server.command;

import net.minestom.server.command.builder.ArgumentCallback;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.CommandExecutor;
import net.minestom.server.command.builder.condition.CommandCondition;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.utils.callback.CommandCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class CommandParser {
    private static final String COMMAND_PREFIX = "/";

    private CommandParser() {
        //no instance
    }

    public static Result parse(NodeGraph graph, String input) {
        final CharSequence withoutPrefix = input.trim().startsWith(COMMAND_PREFIX) ?
                input.subSequence(COMMAND_PREFIX.length(), input.length()) : input;
        // Create reader & parse
        final CommandReader reader = new CommandReader(withoutPrefix);
        List<Map.Entry<Node, Object>> syntax = new ArrayList<>();

        Map.Entry<Node, Object> result = parseChild(graph, graph.root(), reader);

        while (result != null) {
            syntax.add(result);
            if (result.getValue() instanceof ArgumentSyntaxException e) {
                // Syntax error stop at this arg
                return new SyntaxErrorResult(withoutPrefix.toString(), result.getKey().executionInfo().get().condition(),
                        result.getKey().arg().getCallback(), e, syntaxMapper(syntax));
            }
            result = parseChild(graph, result.getKey(), reader);
        }

        if (syntax.isEmpty()) {
            return new UnknownCommandResult(withoutPrefix.toString());
        } else {
            final Node lastNode = syntax.get(syntax.size() - 1).getKey();
            return new ValidCommandResult(withoutPrefix.toString(), lastNode.executionInfo().get().condition(),
                    lastNode.executionInfo().get().executor(), syntaxMapper(syntax));
        }
    }

    private static Map<String, Object> syntaxMapper(List<Map.Entry<Node, Object>> syntax) {
        return syntax.stream()
                .skip(1) // skip root
                .map(x -> Map.entry(x.getKey().arg().getId(), x.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static @Nullable Map.Entry<Node, Object> parseChild(NodeGraph graph, Node parent, CommandReader reader) {
        for (Node child : graph.getChildren(parent)) {
            final int remaining = reader.remaining();
            try {
                final Object parse = child.arg().parse(reader);
                return Map.entry(child, parse);
            } catch (ArgumentSyntaxException e) {
                if (remaining != reader.remaining()) {
                    // Node accepted the input, but it was malformed or otherwise failed validation
                    return Map.entry(child, e);
                }
            }
        }
        return null;
    }

    public sealed interface Result {
        @NotNull String input();
        @NotNull Map<String, Object> arguments();

        default void execute(CommandSender sender, CommandContext context, @Nullable CommandCallback unknownCommandCallback) {
            if (this instanceof UnknownCommandResult) {
                if (unknownCommandCallback == null) return;
                unknownCommandCallback.apply(sender, input());
            } else if (this instanceof KnownCommandResult result) {
                final CommandCondition condition = result.condition();
                if (condition != null && !condition.canUse(sender, input())) {
                    return; // TODO Should we call a callback here or just let the condition do the notifying?
                }
                if (result instanceof ValidCommandResult valid) {
                    valid.executor().apply(sender, context);
                } else if (result instanceof SyntaxErrorResult invalid) {
                    invalid.callback().apply(sender, invalid.exception());
                }
            }
        }
    }

    private sealed interface KnownCommandResult extends Result {
        @Nullable CommandCondition condition();
    }

    private record UnknownCommandResult(String input) implements Result {
        @Override
        public @NotNull Map<String, Object> arguments() {
            return Map.of();
        }
    }

    private record SyntaxErrorResult(String input, CommandCondition condition, ArgumentCallback callback,
                                     ArgumentSyntaxException exception, Map<String, Object> arguments) implements KnownCommandResult {
    }

    private record ValidCommandResult(String input, CommandCondition condition, CommandExecutor executor,
                                      Map<String, Object> arguments) implements KnownCommandResult {
    }
}
