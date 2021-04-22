package net.minestom.server.entity.acquirable;

import net.minestom.server.entity.Entity;
import net.minestom.server.utils.collection.CollectionView;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.function.Consumer;

public class AcquirableCollection<E extends Entity> extends CollectionView<E, AcquirableEntity> {

    private final Collection<AcquirableEntity> acquirableEntityCollection;

    public AcquirableCollection(@NotNull Collection<AcquirableEntity> acquirableEntityCollection) {
        super(acquirableEntityCollection,
                Entity::getAcquirable,
                acquirableEntity -> (E) acquirableEntity.unwrap());
        this.acquirableEntityCollection = acquirableEntityCollection;
    }

    @Override
    public void forEach(Consumer<? super E> action) {
       // Acquisition.acquireForEach(acquirableEntityCollection, action);
    }
}
