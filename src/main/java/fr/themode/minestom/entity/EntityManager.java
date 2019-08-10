package fr.themode.minestom.entity;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EntityManager {

    private Set<LivingEntity> livingEntities = Collections.synchronizedSet(new HashSet<>());

    private ExecutorService pool = Executors.newFixedThreadPool(5);

    public void update() {

        livingEntities.removeIf(livingEntity -> livingEntity.shouldRemove());

        synchronized (livingEntities) {
            Iterator<LivingEntity> iterator = livingEntities.iterator();
            while (iterator.hasNext()) {
                LivingEntity entity = iterator.next();
                pool.submit(entity::update);
            }
        }
    }

    public Set<LivingEntity> getEntities() {
        return Collections.unmodifiableSet(livingEntities);
    }

    protected void addLivingEntity(LivingEntity livingEntity) {
        this.livingEntities.add(livingEntity);
    }
}
