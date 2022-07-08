package net.minestom.server.command;

import net.minestom.server.command.builder.*;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.condition.CommandCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static net.minestom.server.command.builder.arguments.Argument.Result.SyntaxError;

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
            if (result.getValue() instanceof SyntaxError e) {
                // Syntax error stop at this arg
                return new SyntaxErrorResult(withoutPrefix.toString(), result.getKey().executionInfo().get().condition(),
                        result.getKey().arg().getCallback(), e, syntaxMapper(syntax));
            }
            result = parseChild(graph, result.getKey(), reader);
        }

        if (syntax.size() < 1) {
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
                .map(x -> Map.entry(x.getKey().realArg().getId(), x.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static @Nullable Map.Entry<Node, Object> parseChild(NodeGraph graph, Node parent, CommandReader reader) {
        for (Node child : graph.getChildren(Objects.requireNonNullElse(graph.getRedirectTarget(parent), parent))) {
            final int start = reader.cursor();
            try {
                final Argument.Result<?> parse = child.realArg().parse(reader);
                if (parse instanceof Argument.Result.Success<?> success) {
                    return Map.entry(child, success.value());
                } else if (parse instanceof Argument.Result.SyntaxError<?> syntaxError) {
                    return Map.entry(child, syntaxError);
                } else {
                    // Reset cursor & try next
                    reader.setCursor(start);
                }
            } catch (Exception e) {
                // Reset cursor & try next
                reader.setCursor(start);
            }
        }
        return null;
    }

    public sealed interface Result {
        @NotNull String input();
        @NotNull Map<String, Object> arguments();

        default CommandResult execute(CommandSender sender, CommandContext context) {
            if (this instanceof UnknownCommandResult) {
                return new CommandResult(CommandResult.Type.UNKNOWN, input(), null);
            } else if (this instanceof KnownCommandResult result) {
                final CommandCondition condition = result.condition();
                final CommandData data = new CommandData(arguments());
                if (condition != null && !condition.canUse(sender, input())) {
                    return new CommandResult(CommandResult.Type.PRECONDITION_FAILED, input(), data);
                }
                if (result instanceof ValidCommandResult valid) {
                    valid.executor().apply(sender, context);
                    return new CommandResult(CommandResult.Type.SUCCESS, input(), data);
                } else if (result instanceof SyntaxErrorResult invalid) {
                    invalid.callback().apply(sender, invalid.error);
                    return new CommandResult(CommandResult.Type.INVALID_SYNTAX, input(), data);
                }
            }
            return null;
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
                                     SyntaxError<?> error, Map<String, Object> arguments) implements KnownCommandResult {
    }

    private record ValidCommandResult(String input, CommandCondition condition, CommandExecutor executor,
                                      Map<String, Object> arguments) implements KnownCommandResult {
    }
}
