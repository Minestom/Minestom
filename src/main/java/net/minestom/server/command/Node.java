package net.minestom.server.command;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentLiteral;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;

record Node(int id, IntList children, boolean executable, Argument<?> arg, AtomicInteger redirectTarget) {

    public static Node root(int id) {
        return new Node(id, new IntArrayList(), false, null, null);
    }

    public static Node literal(int id, String name, boolean executable, @Nullable AtomicInteger redirectTarget) {
        return literal(id, executable, new ArgumentLiteral(name), redirectTarget);
    }

    public static Node literal(int id, boolean executable, @NotNull Argument<?> backingArg, @Nullable AtomicInteger redirectTarget) {
        return new Node(id, new IntArrayList(), executable, backingArg, redirectTarget);
    }

    public static Node argument(int id, Argument<?> argument, boolean executable, @Nullable AtomicInteger redirectTarget) {
        return new Node(id, new IntArrayList(), executable, argument, redirectTarget);
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

}
