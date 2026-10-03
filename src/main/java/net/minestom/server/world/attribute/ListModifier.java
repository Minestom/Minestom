package net.minestom.server.world.attribute;

import net.minestom.server.codec.Codec;

import java.util.ArrayList;
import java.util.List;

/**
 * Appends the argument to the inherited list rather than replacing it.
 */
record ListModifier<E>(Codec<List<E>> argumentCodec) implements EnvironmentAttribute.Modifier<List<E>, List<E>> {

    @java.lang.Override
    public List<E> modify(List<E> subject, List<E> argument) {
        if (argument.isEmpty()) return subject;
        if (subject.isEmpty()) return argument;
        final List<E> joined = new ArrayList<>(subject.size() + argument.size());
        joined.addAll(subject);
        joined.addAll(argument);
        return List.copyOf(joined);
    }
}
