package net.minestom.server.command;

import net.minestom.server.command.Graph.Node;
import net.minestom.server.command.builder.ArgumentCallback;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.CommandData;
import net.minestom.server.command.builder.CommandExecutor;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentLiteral;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.condition.CommandCondition;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.command.builder.suggestion.Suggestion;
import net.minestom.server.command.builder.suggestion.SuggestionCallback;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

final class CommandParserImpl implements CommandParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandParserImpl.class);
    static final CommandParserImpl PARSER = new CommandParserImpl();

    static final class Chain {
        @Nullable CommandExecutor defaultExecutor = null;
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

                // Track default executor
                final CommandExecutor defExec = execution.defaultExecutor();
                if (defExec != null) defaultExecutor = defExec;

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
                    .skip(2) // skip root node and command
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
         * values [, foo, bar, baz] given the command input "foo bar" will have a successful depth of 2.
         *
         * @return The successful result depth
         * @see #size() getting the size of all results
         */
        int depth() {
            int depth = 0;

            for (NodeResult node : this.nodeResults) {
                if (depth++ == 0) {
                    // If we're on the first node, skip it and increment, we don't care about the empty first node
                    continue;
                }

                // If this node isn't a success, we're going to stop counting the depth and stop here
                if (!(node.argumentResult() instanceof ArgumentResult.Success<?>)) {
                    depth--;
                    break;
                }
            }

            // The chain will always contain a empty node at the start, we don't care about it so we'll remove one
            return depth - 1;
        }

        /**
         * Gets the last successful argument result in the chain (breaking if hitting a non-successful result). This
         * method is very similar in how {@link #depth()}'s functions, and is used to get the last successful result
         *
         * @return The last successful result, or null if there isn't a good result to give back, such as if
         * the depth of the chain is zero (containing only an empty node result, or if no node results exist).
         * @see #depth() the depth size of the chain
         * @see #nodeResults all the node results in the chain
         */
        @Nullable NodeResult lastSuccessfulResult() {
            // Early exit if node results is empty or has only the empty node element
            if (this.nodeResults.size() <= 1) return null;

            NodeResult previousNode = null;
            for (NodeResult node : this.nodeResults) {
                // We want to just skip the initial node, we never want to return it
                if (previousNode == null) {
                    previousNode = node;
                    continue;
                }

                // If this node isn't a success, we're going to stop counting the depth and stop here
                if (!(node.argumentResult() instanceof ArgumentResult.Success<?>)) {
                    return previousNode;
                }

                previousNode = node;
            }

            return previousNode;
        }

        Chain() {}

        Chain(@Nullable CommandExecutor defaultExecutor,
              @Nullable SuggestionCallback suggestionCallback,
              ArrayDeque<NodeResult> nodeResults,
              List<CommandCondition> conditions,
              List<CommandExecutor> globalListeners) {
            this.defaultExecutor = defaultExecutor;
            this.suggestionCallback = suggestionCallback;
            this.nodeResults.addAll(nodeResults);
            this.conditions.addAll(conditions);
            this.globalListeners.addAll(globalListeners);
        }

        Chain fork() {
            return new Chain(defaultExecutor, suggestionCallback, nodeResults, conditions, globalListeners);
        }
    }

    @Override
    public CommandParser.Result parse(CommandSender sender, Graph graph, String input) {
        final CommandStringReader reader = new CommandStringReader(input);
        Node parent = graph.root();

        NodeResult result = parseNode(sender, parent, new Chain(), reader);
        Chain chain = result.chain();

        NodeResult lastNodeResult = chain.nodeResults.peekLast();
        if (lastNodeResult == null) return UnknownCommandResult.INSTANCE;
        Node lastNode = lastNodeResult.node;

        if (result.argumentResult instanceof ArgumentResult.Success<?>) {
            CommandExecutor executor = nullSafeGetter(lastNode.execution(), Graph.Execution::executor);
            if (executor != null) return ValidCommand.executor(input, chain, executor);
        }

        // If here, then the command failed or didn't have an executor, then this isn't a known command
        if (chain.depth() < 1) return UnknownCommandResult.INSTANCE;

        // Look for a default executor, or give up if we got nowhere
        if (lastNode.equals(parent)) return UnknownCommandResult.INSTANCE;

        final @Nullable ValidCommand defaultExecutor = ValidCommand.defaultExecutor(input, chain);
        if (defaultExecutor != null) return defaultExecutor;

        return InvalidCommand.invalid(input, chain);
    }

    @Contract("null, _ -> null; !null, null -> fail; !null, !null -> _")
    private static <R, T> @Nullable R nullSafeGetter(@Nullable T obj, Function<T, R> getter) {
        return obj == null ? null : getter.apply(obj);
    }

    private static NodeResult parseNode(CommandSender sender, Node node, Chain chain, CommandStringReader reader) {
        chain = chain.fork();
        Argument<?> argument = node.argument();
        int start = reader.cursor();

        if (reader.hasRemaining()) {
            SuggestionCallback suggestionCallback = argument.getSuggestionCallback();
            ArgumentResult<?> result = parseArgument(sender, argument, reader);
            NodeResult nodeResult = new NodeResult(node, chain, (ArgumentResult<Object>) result, suggestionCallback);
            chain.append(nodeResult);
            if (suggestionCallback != null) chain.suggestionCallback = suggestionCallback;
            if (chain.size() == 1) { // If this is the root node (usually "Literal<>")
                reader.cursor(start);
            } else {
                if (!(result instanceof ArgumentResult.Success<?>)) {
                    reader.cursor(start);
                    return nodeResult;
                }
            }
        } else {
            // Nothing left, yet we're still being asked to parse? There must be defaults then
            Function<CommandSender, ?> defaultSupplier = node.argument().getDefaultValue();
            if (defaultSupplier != null) {
                Object value = defaultSupplier.apply(sender);
                ArgumentResult<Object> argumentResult = new ArgumentResult.Success<>(value, "");
                chain.append(new NodeResult(node, chain, argumentResult, argument.getSuggestionCallback()));
                // Add the default to the chain, and then carry on dealing with this node
            } else {
                // Still being asked to parse yet there's nothing left, syntax error.
                return new NodeResult(
                        node,
                        chain,
                        new ArgumentResult.SyntaxError<>("Not enough arguments","",-1),
                        argument.getSuggestionCallback()
                );
            }
        }
        // Successfully matched this node's argument
        start = reader.cursor();
        if (!reader.hasRemaining()) start--; // This is needed otherwise the reader throws an AssertionError

        NodeResult error = null;
        for (Node child : node.next()) {
            NodeResult childResult = parseNode(sender, child, chain, reader);
            if (childResult.argumentResult instanceof ArgumentResult.Success<Object>) {
                // Assume that there is only one successful node for a given chain of arguments
                return childResult;
            } else {
                // Traverse through the node results to find the last
                // node with a valid argument
                final int childDepth = childResult.chain().depth();
                final boolean isDeeper = error != null && childDepth > error.chain().depth();

                if (childDepth > 0 && (error == null || isDeeper)) {
                    // If this is the base argument (e.g. "teleport" in /teleport) then
                    // do not report an argument to be incompatible, since the more
                    // correct thing would be to say that the command is unknown.
                    if (!(childResult.chain.size() == 2 && childResult.argumentResult instanceof ArgumentResult.IncompatibleType<?>)) {
                        // If the last successful result is null, throw an exception instead of having unintended behaviour
                        error = Objects.requireNonNull(childResult.chain().lastSuccessfulResult());
                    }
                }
                reader.cursor(start);
            }
        }
        // None were successful. Either incompatible types, or syntax error. It doesn't matter to us, though
        // Try to execute this node
        CommandExecutor executor = nullSafeGetter(node.execution(), Graph.Execution::executor);
        if (executor == null) {
            // Stuck here with no executor
            if (error != null) {
                return error;
            } else {
                return chain.nodeResults.peekLast();
            }
        }

        if (reader.hasRemaining()) {
            // Trailing data is a syntax error
            // Can get to here if there's a default executor even if the user is still typing the command
            // So let's supply the next argument's suggestion callback if it exists
            Node returnNode = node;
            SuggestionCallback suggestionCallback = argument.getSuggestionCallback();
            List<Node> nextNodes = node.next();
            if (!nextNodes.isEmpty()) {
                returnNode = nextNodes.getFirst();
                suggestionCallback = returnNode.argument().getSuggestionCallback();
            }
            NodeResult nodeResult = new NodeResult(
                    returnNode,
                    error == null ? chain : error.chain,
                    new ArgumentResult.SyntaxError<>("Command has trailing data", "", -1),
                    suggestionCallback
            );
            chain.suggestionCallback = suggestionCallback;
            // prevent duplicates from being added (Fixes CommandParseTest#singleCommandWithMultipleSyntax() failure)
            if (chain.getArgs().stream().noneMatch(arg -> arg.getId().equals(argument.getId()))) {
                chain.append(nodeResult);
            }
            return nodeResult;
        }

        // Command was successful!
        return chain.nodeResults.peekLast();
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

        static @Nullable ValidCommand defaultExecutor(String input, Chain chain) {
            CommandExecutor defaultExecutor = null;

            for (Iterator<NodeResult> it = chain.nodeResults.descendingIterator(); it.hasNext();) {
                final NodeResult node = it.next();
                defaultExecutor = node.chain().defaultExecutor;
                if (defaultExecutor != null) break;
            }

            if (defaultExecutor == null) return null;
            return new ValidCommand(input, chain.mergedConditions(), defaultExecutor, chain.collectArguments(),
                    chain.mergedGlobalExecutors(), chain.suggestionCallback, chain.getArgs());
        }

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

    private record NodeResult(Node node, Chain chain, ArgumentResult<Object> argumentResult, SuggestionCallback callback) {
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

            final int i = input.indexOf(' ', cursor);
            if (i == -1) {
                this.cursor = input.length() + 1;
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
            if (cursor < 0 || cursor > input.length()) throw new IllegalArgumentException("Cursor must be between 0 and " + input.length() + " (inclusive)");
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
        } catch (ArgumentSyntaxException ignored) {
            return new ArgumentResult.IncompatibleType<>();
        }
        // Bruteforce
        if (!argument.allowSpace() || argument.useRemaining()) throw new IllegalArgumentException("Invalid argument");
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
