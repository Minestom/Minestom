package net.minestom.server.acquirable;

import net.minestom.server.utils.collection.CollectionView;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class AcquirableCollection<E> extends CollectionView<E, Acquirable<E>> {

    private final Collection<Acquirable<E>> acquirableEntityCollection;

    public AcquirableCollection(@NotNull Collection<Acquirable<E>> acquirableEntityCollection) {
        super(acquirableEntityCollection,
                null,
                //Entity::getAcquirable,
                acquirableEntity -> (E) acquirableEntity.unwrap());
        this.acquirableEntityCollection = acquirableEntityCollection;
    }
}
