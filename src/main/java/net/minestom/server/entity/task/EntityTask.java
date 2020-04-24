package net.minestom.server.entity.task;

import net.minestom.server.entity.LivingEntity;

public abstract class EntityTask {

    /**
     * Whether the task should begin executing for this entity.
     *
     * @param entity the entity in question.
     * @return true if the task should start, false otherwise.
     */
    public abstract boolean shouldStart(LivingEntity entity);

    /**
     * Invoked when this task is about to start for this entity.
     *
     * @param entity the entity in question.
     */
    public abstract void start(LivingEntity entity);

    /**
     * Invoked when this task is being ended for this entity.
     *
     * @param entity the entity in question.
     */
    public abstract void end(LivingEntity entity);

    /**
     * Invoked each tick when this task is being executed for this entity.
     *
     * @param entity the entity in question.
     */
    public abstract void execute(LivingEntity entity);

}
