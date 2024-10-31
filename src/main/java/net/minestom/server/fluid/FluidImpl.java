package net.minestom.server.fluid;

import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

public record FluidImpl(Registry.FluidEntry registry, NamespaceID namespace, int id) implements Fluid {

    private static final AtomicInteger INDEX = new AtomicInteger();
    private static final Registry.Container<Fluid> CONTAINER = Registry.createStaticContainer(Registry.Resource.FLUIDS, FluidImpl::createImpl);

    private static FluidImpl createImpl(String namespace, Registry.Properties properties) {
        return new FluidImpl(Registry.fluidEntry(namespace, properties));
    }

    private FluidImpl(Registry.FluidEntry registry) {
        this(registry, registry.namespace(), INDEX.getAndIncrement());
    }

    static Collection<Fluid> values() {
        return CONTAINER.values();
    }

    public static Fluid get(@NotNull String namespace) {
        return CONTAINER.get(namespace);
    }

    static Fluid getSafe(@NotNull String namespace) {
        return CONTAINER.getSafe(namespace);
    }
}
