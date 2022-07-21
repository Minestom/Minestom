package net.minestom.server.command;

import net.minestom.server.command.Graph.Node;
import net.minestom.server.command.builder.ArgumentCallback;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.CommandData;
import net.minestom.server.command.builder.CommandExecutor;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.condition.CommandCondition;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.command.builder.suggestion.Suggestion;
import net.minestom.server.command.builder.suggestion.SuggestionCallback;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        final List<CommandCondition> conditions = new ArrayList<>();
        final List<CommandExecutor> globalListeners = new ArrayList<>();
        CommandExecutor defaultExecutor = null;

        NodeResult result;
        Node parent = graph.root();

        while ((result = parseChild(parent, reader)) != null) {
            syntax.add(result);
            final Graph.Execution execution = result.node.execution();
            if (execution != null) {
                // Create condition chain
                final CommandCondition condition = execution.condition();
                if (condition != null) conditions.add(condition);
                // Track default executor
                final CommandExecutor defExec = execution.defaultExecutor();
                if (defExec != null) defaultExecutor = defExec;
                // Merge global listeners
                final CommandExecutor globalListener = execution.globalListener();
                if (globalListener != null) globalListeners.add(globalListener);
            }
            // Check parse result
            if (result.io.output instanceof SyntaxError e) {
                // Syntax error stop at this arg
                final ArgumentCallback argumentCallback = parent.argument().getCallback();
                if (argumentCallback == null && defaultExecutor != null) {
                    return new ValidCommand(input, chainConditions(conditions), defaultExecutor, syntaxMapper(syntax),
                            mergeExecutors(globalListeners), extractSuggestionCallback(syntax));
                } else {
                    return new InvalidCommand(input, chainConditions(conditions),
                            argumentCallback, e, syntaxMapper(syntax), mergeExecutors(globalListeners),
                            extractSuggestionCallback(syntax));
                }
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
                if (defaultExecutor != null) {
                    return new ValidCommand(input, chainConditions(conditions), defaultExecutor, syntaxMapper(syntax),
                            mergeExecutors(globalListeners), extractSuggestionCallback(syntax));
                } else {
                    return new InvalidCommand(input, chainConditions(conditions),
                            null/*todo command syntax callback*/,
                            ArgumentResult.syntaxError("INTERNAL ERROR: Couldn't locate executor.", null, -1),
                            args, mergeExecutors(globalListeners), extractSuggestionCallback(syntax));
                }
            }
            if (reader.hasRemaining()) {
                // Command had trailing data
                if (defaultExecutor != null) {
                    return new ValidCommand(input, chainConditions(conditions), defaultExecutor,
                            syntaxMapper(syntax), mergeExecutors(globalListeners), extractSuggestionCallback(syntax));
                } else {
                    return new InvalidCommand(input, chainConditions(conditions),
                            null/*todo command syntax callback*/,
                            ArgumentResult.syntaxError("Command has trailing data.", null, -1),
                            args, mergeExecutors(globalListeners), extractSuggestionCallback(syntax));
                }
            }
            return new ValidCommand(input, chainConditions(conditions), executor, args, mergeExecutors(globalListeners),
                    extractSuggestionCallback(syntax));
        }
    }

    private static SuggestionCallback extractSuggestionCallback(List<NodeResult> syntax) {
        return syntax.get(syntax.size()-1).callback;
    }

    private static Map<String, InputOutputPair<Object>> syntaxMapper(List<NodeResult> syntax) {
        return syntax.stream()
                .skip(1) // skip root
                .collect(Collectors.toMap(NodeResult::name, NodeResult::io));
    }

    private static CommandCondition chainConditions(List<CommandCondition> conditions) {
        return (sender, commandString) -> {
            for (CommandCondition condition : conditions) {
                if (!condition.canUse(sender, commandString)) return false;
            }
            return true;
        };
    }

    private static CommandExecutor mergeExecutors(List<CommandExecutor> executors) {
        return (sender, context) -> executors.forEach(x -> x.apply(sender, context));
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
        if (!reader.hasRemaining()) return null;
        for (Node child : parent.next()) {
            final int start = reader.cursor();
            final ArgumentResult<?> parse = ArgumentParser.parse(child.argument(), reader);
            if (parse instanceof ArgumentResult.Success<?> success) {
                return new NodeResult(child, new InputOutputPair<>(success.value(), ""/*todo get consumed string*/),
                        child.argument().getSuggestionCallback());
            } else if (parse instanceof ArgumentResult.SyntaxError<?> syntaxError) {
                return new NodeResult(child, new InputOutputPair<>(syntaxError, ""/*todo get consumed string*/),
                        child.argument().getSuggestionCallback());
            } else {
                // Reset cursor & try next
                reader.cursor(start);
            }
        }
        final SuggestionCallback suggestionCallback = nullSafeGetter(parent.next()
                        .stream()
                        .map(Node::argument)
                        .filter(argument -> argument.getSuggestionCallback() != null)
                        .findFirst()
                        .orElse(null),
                Argument::getSuggestionCallback
        );
        if (suggestionCallback != null) {
            return new NodeResult(parent,
                    new InputOutputPair<>(new ArgumentParser.SyntaxErrorResult<>(-1,
                            "None of the arguments were compatible, but a suggestion callback was found.", ""), ""),
                    suggestionCallback);
        } else {
            return null;
        }
    }

    record UnknownCommandResult() implements ParseResult.UnknownCommand {
        private static final ParseResult INSTANCE = new UnknownCommandResult();

        @Override
        public @NotNull ExecutionResult execute(@NotNull CommandSender sender) {
            return ExecutionResultImpl.UNKNOWN;
        }

        @Override
        public @Nullable Suggestion suggestion(CommandSender sender) {
            return null;
        }
    }

    sealed interface InternalKnownCommand extends ParseResult.KnownCommand {
        String input();

        @Nullable CommandCondition condition();

        @NotNull Map<String, InputOutputPair<Object>> arguments();

        CommandExecutor globalListener();

        @Nullable SuggestionCallback suggestionCallback();

        @Override
        default @Nullable Suggestion suggestion(CommandSender sender) {
            final SuggestionCallback callback = suggestionCallback();
            if (callback == null) return null;
            final int lastSpace = input().lastIndexOf(" ");
            final Suggestion suggestion = new Suggestion(input(), lastSpace+2, input().length()-lastSpace-1);
            final CommandContext context = new CommandContext(input());
            for (var entry : arguments().entrySet()) {
                final String identifier = entry.getKey();
                final var value = entry.getValue();
                context.setArg(identifier, value.output(), value.input());
            }
            callback.apply(sender, context, suggestion);
            return suggestion;
        }

        @Override
        default @NotNull ExecutionResult execute(@NotNull CommandSender sender) {
            final CommandCondition condition = condition();

            final CommandContext context = new CommandContext(input());
            for (var entry : arguments().entrySet()) {
                final String identifier = entry.getKey();
                final var value = entry.getValue();
                context.setArg(identifier, value.output(), value.input());
            }

            globalListener().apply(sender, context);

            if (condition != null && !condition.canUse(sender, input())) {
                return ExecutionResultImpl.PRECONDITION_FAILED;
            }
            if (this instanceof ValidCommand valid) {
                try {
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

    record InvalidCommand(String input, CommandCondition condition, ArgumentCallback callback, SyntaxError<?> error,
                          @NotNull Map<String, InputOutputPair<Object>> arguments, CommandExecutor globalListener,
                          @Nullable SuggestionCallback suggestionCallback)
            implements InternalKnownCommand, ParseResult.KnownCommand.Invalid {
    }

    record ValidCommand(String input, CommandCondition condition, CommandExecutor executor,
                        @NotNull Map<String, InputOutputPair<Object>> arguments,
                        CommandExecutor globalListener, @Nullable SuggestionCallback suggestionCallback)
            implements InternalKnownCommand, ParseResult.KnownCommand.Valid {
    }

    record ExecutionResultImpl(Type type, CommandData commandData) implements ExecutionResult {
        static final ExecutionResult CANCELLED = new ExecutionResultImpl(Type.CANCELLED, null);
        static final ExecutionResult UNKNOWN = new ExecutionResultImpl(Type.UNKNOWN, null);
        static final ExecutionResult EXECUTOR_EXCEPTION = new ExecutionResultImpl(Type.EXECUTOR_EXCEPTION, null);
        static final ExecutionResult PRECONDITION_FAILED = new ExecutionResultImpl(Type.PRECONDITION_FAILED, null);
        static final ExecutionResult INVALID_SYNTAX = new ExecutionResultImpl(Type.INVALID_SYNTAX, null);
    }

    private record NodeResult(Node node, InputOutputPair<Object> io, SuggestionCallback callback) {
        public String name() {
            return node.argument().getId();
        }
    }

    private record InputOutputPair<R>(R output, String input) {
    }
}
