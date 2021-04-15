package net.minestom.server.lock;

import net.minestom.server.utils.collection.CollectionView;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.function.Consumer;

public class AcquirableCollection<E extends LockedElement> extends CollectionView<E, Acquirable<E>> {

    private final Collection<Acquirable<E>> acquirableCollection;

    public AcquirableCollection(@NotNull Collection<Acquirable<E>> acquirableCollection) {
        super(acquirableCollection,
                LockedElement::getAcquiredElement,
                Acquirable::unwrap);
        this.acquirableCollection = acquirableCollection;
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        Acquisition.acquireForEach(acquirableCollection, action);
    }
}
