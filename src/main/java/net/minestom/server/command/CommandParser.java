package net.minestom.server.command;

import net.minestom.server.command.Graph.Node;
import net.minestom.server.command.builder.*;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.condition.CommandCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static net.minestom.server.command.builder.arguments.Argument.Result.SyntaxError;

public final class CommandParser {
    private static final String COMMAND_PREFIX = "/";

    private CommandParser() {
        //no instance
    }

    public static Result parse(Graph graph, String input) {
        final CharSequence withoutPrefix = input.trim().startsWith(COMMAND_PREFIX) ?
                input.subSequence(COMMAND_PREFIX.length(), input.length()) : input;
        // Create reader & parse
        final CommandReader reader = new CommandReader(withoutPrefix);
        final List<NodeResult> syntax = new ArrayList<>();
        final Node root = graph.root();
        final Set<CommandCondition> conditions = new HashSet<>();

        NodeResult result;
        Node parent = root;

        while ((result = parseChild(parent, reader)) != null) {
            syntax.add(result);
            // Create condition chain
            final CommandCondition condition = result.node().executor().condition();
            if (condition != null) conditions.add(condition);
            // Check parse result
            if (result.value instanceof SyntaxError e) {
                // Syntax error stop at this arg
                return new SyntaxErrorResult(withoutPrefix.toString(), chainConditions(conditions),
                        parent.argument().getCallback(), e, syntaxMapper(syntax));
            }
            parent = result.node;
        }

        if (syntax.size() < 1) {
            return new UnknownCommandResult(withoutPrefix.toString());
        } else {
            final Node lastNode = syntax.get(syntax.size() - 1).node;
            if (lastNode.executor().executor() == null) {
                // Syntax error
                return new SyntaxErrorResult(withoutPrefix.toString(), chainConditions(conditions),
                        lastNode.executor().syntaxErrorCallback(), null, syntaxMapper(syntax));
            }
            return new ValidCommandResult(withoutPrefix.toString(), chainConditions(conditions),
                    lastNode.executor().executor(), syntaxMapper(syntax));
        }
    }

    private static Map<String, Object> syntaxMapper(List<NodeResult> syntax) {
        final Map<String, Object> providedArgs = syntax.stream()
                .skip(1) // skip root
                .collect(Collectors.toMap(NodeResult::name, NodeResult::value));
        final Map<String, Supplier<?>> defaultValueSuppliers = syntax.get(syntax.size() - 1).node.executor().defaultValueSuppliers();
        if (defaultValueSuppliers != null) {
            final Map<String, ?> defaults = defaultValueSuppliers.entrySet()
                    .stream()
                    .map(x -> Map.entry(x.getKey(), x.getValue().get()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            providedArgs.putAll(defaults);
        }
        return providedArgs;
    }

    private static CommandCondition chainConditions(Set<CommandCondition> conditions) {
        return (sender, commandString) -> {
            for (CommandCondition condition : conditions) {
                if (!condition.canUse(sender, commandString)) return false;
            }
            return true;
        };
    }

    private static NodeResult parseChild(Node parent, CommandReader reader) {
        for (Node child : parent.next()) {
            final int start = reader.cursor();
            try {
                final Argument.Result<?> parse = child.argument().parse(reader);
                if (parse instanceof Argument.Result.Success<?> success) {
                    return new NodeResult(child, success.value());
                } else if (parse instanceof Argument.Result.SyntaxError<?> syntaxError) {
                    return new NodeResult(child, syntaxError);
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

    private record NodeResult(Node node, Object value) {
        public String name() {
            return node.argument().getId();
        }
    }
}
