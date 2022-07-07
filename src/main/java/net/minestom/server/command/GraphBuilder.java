package net.minestom.server.command;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandSyntax;
import net.minestom.server.command.builder.arguments.*;
import net.minestom.server.command.builder.arguments.number.ArgumentDouble;
import net.minestom.server.command.builder.arguments.number.ArgumentFloat;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import net.minestom.server.command.builder.arguments.number.ArgumentLong;
import net.minestom.server.command.builder.condition.CommandCondition;
import net.minestom.server.command.builder.exception.IllegalCommandStructureException;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

final class GraphBuilder {
    private static final List<Class<? extends Argument<?>>> argPriorities = List.of(
            ArgumentInteger.class, ArgumentLong.class, ArgumentFloat.class, ArgumentDouble.class //TODO the rest
    );
    private final AtomicInteger idSource = new AtomicInteger();
    private final ObjectList<Node> nodes = new ObjectArrayList<>();
    private final ObjectSet<Supplier<Boolean>> redirectWaitList = new ObjectOpenHashSet<>();
    private final Node root = rootNode();

    private GraphBuilder() {
        //no instance
    }

    private Node rootNode() {
        final Node rootNode = Node.root(idSource.getAndIncrement());
        nodes.add(rootNode);
        return rootNode;
    }

    private Node createLiteralNode(String name, @Nullable Node parent, boolean executable, @Nullable String[] aliases, @Nullable AtomicInteger redirectTo) {
        if (aliases != null) {
            final Node node = createLiteralNode(name, parent, executable, null, null);
            for (String alias : aliases) {
                createLiteralNode(alias, parent, executable, null, new AtomicInteger(node.id()));
            }
            return node;
        } else {
            final Node literalNode = Node.literal(idSource.getAndIncrement(), name, executable, redirectTo);
            nodes.add(literalNode);
            if (parent != null) parent.addChild(literalNode);
            return literalNode;
        }
    }

    private Node[] createArgumentNode(Argument<?> argument, boolean executable) {
        final Node[] nodes;
        Integer overrideRedirectTarget = null;
        if (argument instanceof ArgumentEnum<?> argumentEnum) {
            nodes = argumentEnum.entries().stream().map(x -> createLiteralNode(x, null, executable, null, null)).toArray(Node[]::new);
        } else if (argument instanceof ArgumentGroup argumentGroup) {
            nodes = argumentGroup.group().stream().map(x -> createArgumentNode(x, executable)).flatMap(Stream::of).toArray(Node[]::new);
        } else if (argument instanceof ArgumentLoop<?> argumentLoop) {
            overrideRedirectTarget = idSource.get()-1;
            nodes = argumentLoop.arguments().stream().map(x -> createArgumentNode(x, executable)).flatMap(Stream::of).toArray(Node[]::new);
        } else {
            if (argument instanceof ArgumentCommand) {
                return new Node[]{createLiteralNode(argument.getId(), null, false, null, new AtomicInteger(0))};
            }
            final int id = idSource.getAndIncrement();
            nodes = new Node[] {argument instanceof ArgumentLiteral ?
                    Node.literal(id, argument.getId(), executable, null) :
                    Node.argument(id, argument, executable, null)};
        }
        for (Node node : nodes) {
            this.nodes.add(node);
            Integer finalOverrideRedirectTarget = overrideRedirectTarget;
            if (finalOverrideRedirectTarget != null) {
                redirectWaitList.add(() -> {
                    int target = finalOverrideRedirectTarget;
                    if (target != -1) {
                        node.redirectTarget().set(target);
                        return true;
                    }
                    return false;
                });
            }
        }
        return nodes;
    }

    private int tryResolveId(String[] path) {
        if (path.length == 0) {
            return root.id();
        } else {
            Node target = root;
            for (String next : path) {
                Node finalTarget = target;
                final Optional<Node> result = nodes.stream().filter(finalTarget::isParentOf)
                        .filter(x -> x.name().equals(next)).findFirst();
                if (result.isEmpty()) {
                    return -1;
                } else {
                    target = result.get();
                }
            }
            return target.id();
        }
    }

    private void finalizeStructure(boolean forParsing) {
        redirectWaitList.removeIf(Supplier::get);
        if (redirectWaitList.size() > 0)
            throw new IllegalCommandStructureException("Could not set redirects for all arguments! Did you provide a " +
                    "correct id path which doesn't rely on redirects?");

        nodes.sort(Comparator.comparing(Node::id));

        if (forParsing) {
            for (Node node : nodes) {
                node.children().sort((k1, k2) -> Integer.compare(argPriorities.indexOf(nodes.get(k1).arg().getClass()),
                        argPriorities.indexOf(nodes.get(k2).arg().getClass())));
            }
        }
    }


    /**
     * Creates the nodes for the given command
     *
     * @param command the command to add
     * @param parent where to append the command's root (literal) node
     * @param player a player if we should filter commands
     */
    private void createCommand(Command command, Node parent, @Nullable Player player) {
        if (player != null) {
            // Check if user can use the command
            final CommandCondition condition = command.getCondition();
            if (condition != null && !condition.canUse(player, null)) return;
        }

        // Create the command's root node
        final Node cmdNode = createLiteralNode(command.getName(), parent,
                command.getDefaultExecutor() != null, command.getAliases(), null);

        // Add syntax to the command
        for (CommandSyntax syntax : command.getSyntaxes()) {
            if (player != null) {
                // Check if user can use the syntax
                final CommandCondition condition = syntax.getCommandCondition();
                if (condition != null && !condition.canUse(player, null)) continue;
            }

            boolean executable = false;
            Node[] lastArgNodes = new Node[] {cmdNode}; // First arg links to cmd root
            @NotNull Argument<?>[] arguments = syntax.getArguments();
            for (int i = 0; i < arguments.length; i++) {
                Argument<?> argument = arguments[i];
                // Determine if command is executable here
                if (executable && argument.getDefaultValue() == null) {
                    // Optional arg was followed by a non-optional
                    throw new IllegalCommandStructureException("Optional argument was followed by a non-optional one.");
                }
                if (!executable && i < arguments.length-1 && arguments[i+1].getDefaultValue() != null || i+1 == arguments.length) {
                    executable = true;
                }
                // Append current node to previous
                final Node[] argNodes = createArgumentNode(argument, executable);
                for (Node lastArgNode : lastArgNodes) {
                    lastArgNode.addChild(argNodes);
                }
                lastArgNodes = argNodes;
            }
        }

        // Add subcommands
        for (Command subcommand : command.getSubcommands()) {
            createCommand(subcommand, cmdNode, player);
        }
    }

    public static NodeGraph forPlayer(@NotNull Set<Command> commands, Player player) {
        final GraphBuilder builder = new GraphBuilder();

        if (GraphBuilder.class.desiredAssertionStatus()) {
            // Detect infinite recursion
            for (Command command : commands) {
                final HashSet<Command> processed = new HashSet<>();
                final Stack<Command> stack = new Stack<>();
                stack.push(command);
                while (!stack.isEmpty()) {
                    final Command pop = stack.pop();
                    if (!processed.add(pop)) {
                        throw new IllegalCommandStructureException("Infinite recursion detected in command: "+command.getName());
                    } else {
                        stack.addAll(pop.getSubcommands());
                    }
                }

                builder.createCommand(command, builder.root, player);
            }
        } else {
            for (Command command : commands) {
                builder.createCommand(command, builder.root, player);
            }
        }

        builder.finalizeStructure(player == null);

        return new NodeGraph(builder.nodes, builder.root);
    }

    public static NodeGraph forServer(@NotNull Set<Command> commands) {
        return forPlayer(commands, null);
    }
}
