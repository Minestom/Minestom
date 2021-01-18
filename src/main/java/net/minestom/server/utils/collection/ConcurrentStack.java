package net.minestom.server.utils.collection;

import java.util.concurrent.atomic.AtomicReference;

/**
 * ConcurrentStack
 *
 * Nonblocking stack using Treiber's algorithm
 *
 * @author Brian Goetz and Tim Peierls
 */
public final class ConcurrentStack<E> {
    private final AtomicReference<Node<E>> top = new AtomicReference<>();

    public ConcurrentStack() {

    }

    /**
     * Adds a new element to the top of the stack
     *
     * @param item The item to add to the top of the stack
     */
    public void push(E item) {
        Node<E> newHead = new Node<>(item);
        Node<E> oldHead;
        do {
            oldHead = top.get();
            newHead.next = oldHead;
        } while (!top.compareAndSet(oldHead, newHead));
    }

    /**
     * Removes an element from the top of the stack
     *
     * @return The removed element.
     */
    public E pop() {
        Node<E> oldHead;
        Node<E> newHead;
        do {
            oldHead = top.get();
            if (oldHead == null)
                return null;
            newHead = oldHead.next;
        } while (!top.compareAndSet(oldHead, newHead));
        return oldHead.item;
    }

    public void clear() {
        top.set(null);
    }

    private static class Node<E> {
        public final E item;
        public Node<E> next;

        public Node(E item) {
            this.item = item;
        }
    }
}