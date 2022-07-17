package net.minestom.server.command;

import net.minestom.server.command.Graph.Node;
import net.minestom.server.command.builder.ArgumentCallback;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.CommandData;
import net.minestom.server.command.builder.CommandExecutor;
import net.minestom.server.command.builder.condition.CommandCondition;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static net.minestom.server.command.ArgumentResult.SyntaxError;

final class CommandParserImpl implements CommandParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandParserImpl.class);
    static final CommandParserImpl PARSER = new CommandParserImpl();

    @Override
    public @NotNull ParseResult parse(@NotNull Graph graph, @NotNull String input) {
        // Create reader & parse
        final CommandStringReader reader = new CommandStringReader(input);
        final List<NodeResult> syntax = new ArrayList<>();
        final Node root = graph.root();
        final Set<CommandCondition> conditions = new HashSet<>();

        NodeResult result;
        Node parent = root;

        while ((result = parseChild(parent/*todo redirects*/, reader)) != null) {
            syntax.add(result);
            // Create condition chain
            final CommandCondition condition = nullSafeGetter(result.node.execution(), Graph.Execution::condition);
            if (condition != null) conditions.add(condition);
            // Check parse result
            if (result.io.output instanceof SyntaxError e) {
                // Syntax error stop at this arg
                return new InvalidCommand(input, chainConditions(conditions),
                        parent.argument().getCallback(), e, syntaxMapper(syntax));
            }
            parent = result.node;
        }

        if (syntax.size() < 1) {
            return UnknownCommandResult.INSTANCE;
        } else {
            final Map<String, InputOutputPair<Object>> args = syntaxMapper(syntax);
            final CommandExecutor executor = locateExecutor(syntax.get(syntax.size() - 1).node, args);
            if (executor == null) {
                // Syntax error
                return new InvalidCommand(input, chainConditions(conditions),
                        null/*todo command syntax callback*/,
                        ArgumentResult.syntaxError("INTERNAL ERROR: Couldn't locate executor.", null, -1),
                        args);
            }
            if (reader.hasRemaining()) {
                // Command had trailing data
                return new InvalidCommand(input, chainConditions(conditions),
                        null/*todo command syntax callback*/,
                        ArgumentResult.syntaxError("Command has trailing data.", null, -1),
                        args);
            }
            return new ValidCommand(input, chainConditions(conditions), executor, args);
        }
    }

    private static Map<String, InputOutputPair<Object>> syntaxMapper(List<NodeResult> syntax) {
        return syntax.stream()
                .skip(1) // skip root
                .collect(Collectors.toMap(NodeResult::name, NodeResult::io));
    }

    private static CommandCondition chainConditions(Set<CommandCondition> conditions) {
        return (sender, commandString) -> {
            for (CommandCondition condition : conditions) {
                if (!condition.canUse(sender, commandString)) return false;
            }
            return true;
        };
    }

    private static CommandExecutor locateExecutor(Node fromNode, Map<String, InputOutputPair<Object>> arguments) {
        CommandExecutor executor = nullSafeGetter(fromNode.execution(), Graph.Execution::executor);
        if (executor != null) return executor; //parsing ended with last args
        Map<String, Supplier<?>> defaultValueSupplier = new HashMap<>();
        fromNode = indexSafeGetter(fromNode.next(), 0); //skip the current arg as it is present, and the executor was checked above
        while (fromNode != null) {
            final Supplier<?> supplier = fromNode.argument().getDefaultValue();
            if (supplier == null) {
                //Required arg wasn't present
                return null;
            }
            executor = nullSafeGetter(fromNode.execution(), Graph.Execution::executor);
            defaultValueSupplier.put(fromNode.argument().getId(), supplier);
            fromNode = indexSafeGetter(fromNode.next(), 0);
        }
        if (executor == null) return null;
        arguments.putAll(defaultValueSupplier.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, x -> new InputOutputPair<>(x.getValue().get(), ""))));
        return executor;
    }

    @Contract("null, _ -> null; !null, null -> fail; !null, !null -> _")
    private static <R, T> @Nullable R nullSafeGetter(@Nullable T obj, Function<T, R> getter) {
        return obj == null ? null : getter.apply(obj);
    }

    private static <R, T extends List<R>> @Nullable R indexSafeGetter(@NotNull T list, int index) {
        return list.size() <= index || index < 0 ? null : list.get(index);
    }

    private static NodeResult parseChild(Node parent, CommandStringReader reader) {
        for (Node child : parent.next()) {
            final int start = reader.cursor();
            try {
                final ArgumentResult<?> parse = ArgumentParser.parse(child.argument(), reader);
                if (parse instanceof ArgumentResult.Success<?> success) {
                    return new NodeResult(child,  new InputOutputPair<>(success.value(), ""/*todo get consumed string*/));
                } else if (parse instanceof ArgumentResult.SyntaxError<?> syntaxError) {
                    return new NodeResult(child, new InputOutputPair<>(syntaxError, ""/*todo get consumed string*/));
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

    record UnknownCommandResult() implements ParseResult.UnknownCommand {
        private static final ParseResult INSTANCE = new UnknownCommandResult();

        @Override
        public @NotNull ExecutionResult execute(@NotNull CommandSender sender) {
            return ExecutionResultImpl.UNKNOWN;
        }
    }

    sealed interface InternalKnownCommand extends ParseResult.KnownCommand {
        String input();
        @Nullable CommandCondition condition();

        @NotNull Map<String, InputOutputPair<Object>> arguments();

        @Override
        default @NotNull ExecutionResult execute(@NotNull CommandSender sender) {
            final CommandCondition condition = condition();
            if (condition != null && !condition.canUse(sender, input())) {
                return ExecutionResultImpl.PRECONDITION_FAILED;
            }
            if (this instanceof ValidCommand valid) {
                try {
                    final CommandContext context = new CommandContext(input());
                    for (var entry : arguments().entrySet()) {
                        final String identifier = entry.getKey();
                        final var value = entry.getValue();
                        context.setArg(identifier, value.output(), value.input());
                    }
                    valid.executor().apply(sender, context);
                    return new ExecutionResultImpl(ExecutionResult.Type.SUCCESS, context.getReturnData());
                } catch (Exception e) {
                    LOGGER.error("An exception was encountered while executing command: " + input(), e);
                    return ExecutionResultImpl.EXECUTOR_EXCEPTION;
                }
            } else if (this instanceof InvalidCommand invalid) {
                final ArgumentCallback callback = invalid.callback();
                if (callback != null)
                    callback.apply(sender, new ArgumentSyntaxException(invalid.error.message(),
                            invalid.error.input(), invalid.error.code()));
                return ExecutionResultImpl.INVALID_SYNTAX;
            }
            throw new IllegalStateException("How did we get here?");
        }
    }

    record InvalidCommand(String input, CommandCondition condition, ArgumentCallback callback,
                                     SyntaxError<?> error,
                                     @NotNull Map<String, InputOutputPair<Object>> arguments)
            implements InternalKnownCommand, ParseResult.KnownCommand.Invalid {
    }

    record ValidCommand(String input, CommandCondition condition, CommandExecutor executor,
                                      @NotNull Map<String, InputOutputPair<Object>> arguments)
            implements InternalKnownCommand, ParseResult.KnownCommand.Valid {
    }

    record ExecutionResultImpl(Type type, CommandData commandData) implements ExecutionResult {
        static final ExecutionResult CANCELLED = new ExecutionResultImpl(Type.CANCELLED, null);
        static final ExecutionResult UNKNOWN = new ExecutionResultImpl(Type.UNKNOWN, null);
        static final ExecutionResult EXECUTOR_EXCEPTION = new ExecutionResultImpl(Type.EXECUTOR_EXCEPTION, null);
        static final ExecutionResult PRECONDITION_FAILED = new ExecutionResultImpl(Type.PRECONDITION_FAILED, null);
        static final ExecutionResult INVALID_SYNTAX = new ExecutionResultImpl(Type.INVALID_SYNTAX, null);
    }

    private record NodeResult(Node node, InputOutputPair<Object> io) {
        public String name() {
            return node.argument().getId();
        }
    }

    private record InputOutputPair<R>(R output, String input) {
    }
}
