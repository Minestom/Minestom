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
import net.minestom.server.utils.StringUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

final class CommandParserImpl implements CommandParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandParserImpl.class);
    static final CommandParserImpl PARSER = new CommandParserImpl();

    static final class Chain {
        @Nullable SuggestionCallback suggestionCallback = null;
        final ArrayDeque<NodeResult> nodeResults = new ArrayDeque<>();
        final List<CommandCondition> conditions = new ArrayList<>();
        final List<CommandExecutor> globalListeners = new ArrayList<>();

        void append(NodeResult result) {
            this.nodeResults.add(result);
            final Graph.Execution execution = result.node.execution();
            if (execution != null) {
                // Create condition chain
                final CommandCondition condition = execution.condition();
                if (condition != null) conditions.add(condition);

                // Merge global listeners
                final CommandExecutor globalListener = execution.globalListener();
                if (globalListener != null) globalListeners.add(globalListener);
            }
        }

        CommandCondition mergedConditions() {
            return (sender, commandString) -> {
                for (CommandCondition condition : conditions) {
                    if (!condition.canUse(sender, commandString)) return false;
                }
                return true;
            };
        }

        CommandExecutor mergedGlobalExecutors() {
            return (sender, context) -> globalListeners.forEach(x -> x.apply(sender, context));
        }

        Map<String, ArgumentResult<Object>> collectArguments() {
            return nodeResults.stream()
                    .skip(1) // skip command node
                    .collect(Collectors.toUnmodifiableMap(NodeResult::name, NodeResult::argumentResult));
        }

        List<Argument<?>> getArgs() {
            return nodeResults.stream().map(x -> x.node.argument()).collect(Collectors.toList());
        }

        int size() {
            return nodeResults.size();
        }

        /**
         * Calculates the depth of the chain that is considered successful or valid, providing a more accurate measure
         * for deciding which chain is the most reliable to use. For example a chain that contains the following
         * values [foo, bar, baz] given the command input "foo bar" will have a successful depth of 2.
         *
         * @return The successful result depth
         * @see #size() getting the size of all results
         */
        int depth() {
            int depth = 0;

            for (NodeResult node : this.nodeResults) {
                depth++;
                // If this node isn't a success, we're going to stop counting the depth and stop here
                if (!(node.argumentResult() instanceof ArgumentResult.Success<?>)) {
                    depth--;
                    break;
                }
            }

            return depth;
        }

        Chain() {}

        Chain(@Nullable SuggestionCallback suggestionCallback,
              ArrayDeque<NodeResult> nodeResults,
              List<CommandCondition> conditions,
              List<CommandExecutor> globalListeners) {
            this.suggestionCallback = suggestionCallback;
            this.nodeResults.addAll(nodeResults);
            this.conditions.addAll(conditions);
            this.globalListeners.addAll(globalListeners);
        }

        Chain fork() {
            return new Chain(suggestionCallback, nodeResults, conditions, globalListeners);
        }
    }

    @Override
    public CommandParser.Result parse(CommandSender sender, Graph graph, String input) {
        final CommandStringReader reader = new CommandStringReader(input);
        Node parent = graph.root();

        NodeResult result = parseNode(sender, parent, new Chain(), reader);
        // If result is null, then no arguments were found.
        if (result == null) return UnknownCommandResult.INSTANCE;
        Chain chain = result.chain();

        if (result.argumentResult instanceof ArgumentResult.Success<?>) {
            CommandExecutor executor = nullSafeGetter(result.node().execution(), Graph.Execution::executor);
            if (executor != null) return ValidCommand.executor(input, chain, executor);
        }

        if (chain.depth() >= 1) return InvalidCommand.invalid(input, chain);
        // If here, then the command had no valid arguments, then this isn't a known command
        return UnknownCommandResult.INSTANCE;
    }

    @Contract("null, _ -> null; !null, null -> fail; !null, !null -> _")
    private static <R, T> @Nullable R nullSafeGetter(@Nullable T obj, Function<T, R> getter) {
        return obj == null ? null : getter.apply(obj);
    }

    private static @Nullable NodeResult parseNode(CommandSender sender, Node node, Chain chain, CommandStringReader reader) {
        NodeResult error = null;
        int start = reader.cursor();

        for (Node child : node.next()) {
            reader.cursor(start);
            var result = (ArgumentResult<Object>) parseArgument(sender, child, reader);

            var childChain = chain.fork();
            NodeResult childResult = new NodeResult(child, childChain, result);

            childChain.append(childResult);

            if (child.argument().getSuggestionCallback() != null) {
                childChain.suggestionCallback = child.argument().getSuggestionCallback();
            }

            if (!(result instanceof ArgumentResult.Success<?>)) {
                // Check to see if the result of this argument is one width long.
                // This allows arguments that don't allow spaces to give
                // suggestions when putting an initial space in an empty
                // argument.
                // Anything else removes the suggestions since the argument
                // was invalid.
                if (reader.cursor() - start > 1 || reader.hasRemaining()) {
                    childChain.suggestionCallback = null;
                }
                error = childResult;
                continue;
            }

            if (child.next().isEmpty()) {
                if (reader.hasRemaining()) {
                    // Trailing data is a syntax error
                    // Can get to here if there's a default executor even if the user is still typing the command
                    // So let's supply the next argument's suggestion callback if it exists
                    error = new NodeResult(
                            child,
                            childChain,
                            new ArgumentResult.SyntaxError<>("Command has trailing data", "", -1)
                    );
                    continue;
                } else {
                    return childResult;
                }
            }

            // If there's nothing left and this child can be executed, return it.
            // Otherwise, continue parsing for nodes with a default value and an executor.
            if (!reader.hasRemaining() && child.execution() != null && child.execution().executor() != null) {
                return childResult;
            }

            return parseNode(sender, child, childChain, reader);
        }
        // No successful node was parsed.
        return error;
    }

    private static ArgumentResult<?> parseArgument(CommandSender sender, Node node, CommandStringReader reader) {
        Argument<?> argument = node.argument();

        ArgumentResult<?> result;
        if (reader.hasRemaining()) {
            result = parseArgument(sender, argument, reader);
        } else {
            // Nothing left, yet we're still being asked to parse? There must be defaults then
            Function<CommandSender, ?> defaultSupplier = node.argument().getDefaultValue();
            if (defaultSupplier != null) {
                Object value = defaultSupplier.apply(sender);
                result = new ArgumentResult.Success<>(value, "");
                // Add the default to the chain, and then carry on dealing with this node
            } else {
                // Still being asked to parse yet there's nothing left, syntax error.
                result = new ArgumentResult.SyntaxError<>("Not enough arguments", "", -1);
            }
        }
        return result;
    }

    record UnknownCommandResult() implements Result.UnknownCommand {
        private static final Result INSTANCE = new UnknownCommandResult();

        @Override
        public ExecutableCommand executable() {
            return UnknownExecutableCmd.INSTANCE;
        }

        @Override
        public @Nullable Suggestion suggestion(CommandSender sender) {
            return null;
        }

        @Override
        public List<Argument<?>> args() {
            return null;
        }
    }

    sealed interface InternalKnownCommand extends Result.KnownCommand {
        String input();

        @Nullable CommandCondition condition();

        Map<String, ArgumentResult<Object>> arguments();

        CommandExecutor globalListener();

        @Nullable SuggestionCallback suggestionCallback();

        @Override
        default @Nullable Suggestion suggestion(CommandSender sender) {
            final SuggestionCallback callback = suggestionCallback();
            if (callback == null) return null;
            final int lastSpace = input().lastIndexOf(" ");
            final Suggestion suggestion = new Suggestion(input(), lastSpace + 2, input().length() - lastSpace - 1);
            final CommandContext context = createCommandContext(input(), arguments());
            callback.apply(sender, context, suggestion);
            return suggestion;
        }
    }

    record InvalidCommand(String input, CommandCondition condition, ArgumentCallback callback,
                          ArgumentResult.SyntaxError<?> error,
                          Map<String, ArgumentResult<Object>> arguments, CommandExecutor globalListener,
                          @Nullable SuggestionCallback suggestionCallback, List<Argument<?>> args)
            implements InternalKnownCommand, Result.KnownCommand.Invalid {

        static InvalidCommand invalid(String input, Chain chain) {
            return new InvalidCommand(input, chain.mergedConditions(),
                    null/*todo command syntax callback*/,
                    new ArgumentResult.SyntaxError<>("Command has trailing data.", null, -1),
                    chain.collectArguments(), chain.mergedGlobalExecutors(), chain.suggestionCallback, chain.getArgs());
        }

        @Override
        public ExecutableCommand executable() {
            return new InvalidExecutableCmd(condition, globalListener, callback, error, input, arguments);
        }
    }

    record ValidCommand(String input, CommandCondition condition, CommandExecutor executor,
                        Map<String, ArgumentResult<Object>> arguments,
                        CommandExecutor globalListener, @Nullable SuggestionCallback suggestionCallback, List<Argument<?>> args)
            implements InternalKnownCommand, Result.KnownCommand.Valid {
        static ValidCommand executor(String input, Chain chain, CommandExecutor executor) {
            return new ValidCommand(input, chain.mergedConditions(), executor, chain.collectArguments(), chain.mergedGlobalExecutors(),
                    chain.suggestionCallback, chain.getArgs());
        }

        @Override
        public ExecutableCommand executable() {
            return new ValidExecutableCmd(condition, globalListener, executor, input, arguments);
        }
    }

    record UnknownExecutableCmd() implements ExecutableCommand {
        static final ExecutableCommand INSTANCE = new UnknownExecutableCmd();

        @Override
        public Result execute(CommandSender sender) {
            return ExecutionResultImpl.UNKNOWN;
        }
    }

    record ValidExecutableCmd(CommandCondition condition, CommandExecutor globalListener, CommandExecutor executor,
                              String input,
                              Map<String, ArgumentResult<Object>> arguments) implements ExecutableCommand {
        @Override
        public Result execute(CommandSender sender) {
            final CommandContext context = createCommandContext(input, arguments);

            globalListener().apply(sender, context);

            if (condition != null && !condition.canUse(sender, input())) {
                return ExecutionResultImpl.PRECONDITION_FAILED;
            }
            try {
                executor().apply(sender, context);
                return new ExecutionResultImpl(ExecutableCommand.Result.Type.SUCCESS, context.getReturnData());
            } catch (Exception e) {
                LOGGER.error("An exception was encountered while executing command: " + input(), e);
                return ExecutionResultImpl.EXECUTOR_EXCEPTION;
            }
        }
    }

    record InvalidExecutableCmd(CommandCondition condition, CommandExecutor globalListener, ArgumentCallback callback,
                                ArgumentResult.SyntaxError<?> error, String input,
                                Map<String, ArgumentResult<Object>> arguments) implements ExecutableCommand {
        @Override
        public Result execute(CommandSender sender) {
            globalListener().apply(sender, createCommandContext(input, arguments));

            if (condition != null && !condition.canUse(sender, input())) {
                return ExecutionResultImpl.PRECONDITION_FAILED;
            }
            if (callback != null)
                callback.apply(sender, new ArgumentSyntaxException(error.message(), error.input(), error.code()));
            return ExecutionResultImpl.INVALID_SYNTAX;
        }
    }

    private static CommandContext createCommandContext(String input, Map<String, ArgumentResult<Object>> arguments) {
        final CommandContext context = new CommandContext(input);
        for (var entry : arguments.entrySet()) {
            final String identifier = entry.getKey();
            final ArgumentResult<Object> value = entry.getValue();

            final Object argOutput = value instanceof ArgumentResult.Success<Object> success ? success.value() : null;
            final String argInput = value instanceof ArgumentResult.Success<Object> success ? success.input() : "";

            context.setArg(identifier, argOutput, argInput);
        }
        return context;
    }

    record ExecutionResultImpl(Type type, CommandData commandData) implements ExecutableCommand.Result {
        static final ExecutableCommand.Result CANCELLED = new ExecutionResultImpl(Type.CANCELLED, null);
        static final ExecutableCommand.Result UNKNOWN = new ExecutionResultImpl(Type.UNKNOWN, null);
        static final ExecutableCommand.Result EXECUTOR_EXCEPTION = new ExecutionResultImpl(Type.EXECUTOR_EXCEPTION, null);
        static final ExecutableCommand.Result PRECONDITION_FAILED = new ExecutionResultImpl(Type.PRECONDITION_FAILED, null);
        static final ExecutableCommand.Result INVALID_SYNTAX = new ExecutionResultImpl(Type.INVALID_SYNTAX, null);
    }

    private record NodeResult(Node node, Chain chain, ArgumentResult<Object> argumentResult) {
        public String name() {
            return node.argument().getId();
        }
    }

    static final class CommandStringReader {
        private final String input;
        private int cursor = 0;

        CommandStringReader(String input) {
            this.input = input;
        }

        boolean hasRemaining() {
            return cursor < input.length();
        }

        String readWord() {
            final String input = this.input;
            final int cursor = this.cursor;

            final int i = StringUtils.indexOfLastDuplicate(input, ' ', cursor);
            if (i == -1) {
                this.cursor = input.length();
                return input.substring(cursor);
            }
            final String read = input.substring(cursor, i);
            this.cursor += read.length() + 1;
            return read;
        }

        String readRemaining() {
            final String input = this.input;
            final String result = input.substring(cursor);
            this.cursor = input.length();
            return result;
        }

        int cursor() {
            return cursor;
        }

        void cursor(int cursor) {
            assert cursor >= 0 && cursor <= input.length();
            this.cursor = cursor;
        }
    }

    // ARGUMENT

    private static <T> ArgumentResult<T> parseArgument(CommandSender sender, Argument<T> argument, CommandStringReader reader) {
        // Handle specific type without loop
        try {
            // Single word argument
            if (!argument.allowSpace()) {
                final String word = reader.readWord();
                return new ArgumentResult.Success<>(argument.parse(sender, word), word);
            }
            // Complete input argument
            if (argument.useRemaining()) {
                final String remaining = reader.readRemaining();
                return new ArgumentResult.Success<>(argument.parse(sender, remaining), remaining);
            }
        } catch (ArgumentSyntaxException e) {
            return new ArgumentResult.SyntaxError<>(e.getMessage(), e.getInput(), e.getErrorCode());
        }
        // Bruteforce
        assert argument.allowSpace() && !argument.useRemaining();
        StringBuilder current = new StringBuilder(reader.readWord());
        while (true) {
            try {
                final String input = current.toString();
                return new ArgumentResult.Success<>(argument.parse(sender, input), input);
            } catch (ArgumentSyntaxException ignored) {
                if (!reader.hasRemaining()) break;
                current.append(" ");
                current.append(reader.readWord());
            }
        }
        return new ArgumentResult.IncompatibleType<>();
    }

    private sealed interface ArgumentResult<R> {
        record Success<T>(T value, String input)
                implements ArgumentResult<T> {
        }

        record IncompatibleType<T>()
                implements ArgumentResult<T> {
        }

        record SyntaxError<T>(String message, String input, int code)
                implements ArgumentResult<T> {
        }
    }
}
