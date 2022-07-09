package net.minestom.server.command;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minestom.server.command.builder.CommandExecutor;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentLiteral;
import net.minestom.server.command.builder.condition.CommandCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

record Node(int id, IntList children, boolean executable, Argument<?> arg, Argument<?> realArg, AtomicInteger redirectTarget,
            AtomicReference<ExecutionInfo> executionInfo) {

    public static Node root(int id) {
        return new Node(id, new IntArrayList(), false, null, null, null,
                new AtomicReference<>());
    }

    public static Node literal(int id, String name, boolean executable, @Nullable AtomicInteger redirectTarget) {
        final ArgumentLiteral literal = new ArgumentLiteral(name);
        return new Node(id, new IntArrayList(), executable, literal, literal, redirectTarget, new AtomicReference<>());
    }

    public static Node literal(int id, String name, boolean executable, @Nullable AtomicInteger redirectTarget, @NotNull Argument<?> backingArg) {
        return new Node(id, new IntArrayList(), executable, new ArgumentLiteral(name), backingArg, redirectTarget, new AtomicReference<>());
    }

    public static Node argument(int id, Argument<?> argument, boolean executable, @Nullable AtomicInteger redirectTarget) {
        return new Node(id, new IntArrayList(), executable, argument, argument, redirectTarget, new AtomicReference<>());
    }

    public void addChildren(Node ...nodes) {
        for (Node node : nodes) {
            children.add(node.id);
        }
    }

    public boolean isParentOf(Node node) {
        return children.contains(node.id());
    }

    public boolean isRoot() {
        return arg == null;
    }

    public record ExecutionInfo(@Nullable CommandCondition condition, @Nullable CommandExecutor executor) {}
}
