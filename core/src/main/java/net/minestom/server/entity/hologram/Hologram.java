package net.minestom.server.entity.hologram;

import net.kyori.adventure.text.Component;
import net.minestom.server.Viewable;
import net.minestom.server.chat.JsonMessage;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.type.decoration.EntityArmorStand;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Represents an invisible armor stand showing a {@link Component}.
 */
public class Hologram implements Viewable {

    private static final float OFFSET_Y = -0.9875f;

    private final HologramEntity entity;

    private Position position;
    private Component text;

    private boolean removed;

    /**
     * Constructs a new {@link Hologram} with the given parameters.
     *
     * @param instance      The instance where the hologram should be spawned.
     * @param spawnPosition The spawn position of this hologram.
     * @param text          The text of this hologram.
     * @param autoViewable  {@code true}if the hologram should be visible automatically, otherwise {@code false}.
     * @deprecated Use {@link #Hologram(Instance, Position, Component, boolean)}
     */
    @Deprecated
    public Hologram(Instance instance, Position spawnPosition, JsonMessage text, boolean autoViewable) {
        this(instance, spawnPosition, text.asComponent(), autoViewable);
    }

    /**
     * Constructs a new {@link Hologram} with the given parameters.
     *
     * @param instance      The instance where the hologram should be spawned.
     * @param spawnPosition The spawn position of this hologram.
     * @param text          The text of this hologram.
     * @deprecated Use {@link #Hologram(Instance, Position, Component)}
     */
    @Deprecated
    public Hologram(Instance instance, Position spawnPosition, JsonMessage text) {
        this(instance, spawnPosition, text, true);
    }

    /**
     * Constructs a new {@link Hologram} with the given parameters.
     *
     * @param instance      The instance where the hologram should be spawned.
     * @param spawnPosition The spawn position of this hologram.
     * @param text          The text of this hologram.
     */
    public Hologram(Instance instance, Position spawnPosition, Component text) {
        this(instance, spawnPosition, text, true);
    }

    /**
     * Constructs a new {@link Hologram} with the given parameters.
     *
     * @param instance      The instance where the hologram should be spawned.
     * @param spawnPosition The spawn position of this hologram.
     * @param text          The text of this hologram.
     * @param autoViewable  {@code true}if the hologram should be visible automatically, otherwise {@code false}.
     */
    public Hologram(Instance instance, Position spawnPosition, Component text, boolean autoViewable) {
        this.entity = new HologramEntity(spawnPosition.clone().add(0, OFFSET_Y, 0));
        this.entity.setInstance(instance);
        this.entity.setAutoViewable(autoViewable);

        this.position = spawnPosition;
        setText(text);
    }

    /**
     * Gets the position of the hologram.
     *
     * @return the hologram's position
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Changes the position of the hologram.
     *
     * @param position the new hologram's position
     */
    public void setPosition(Position position) {
        checkRemoved();
        position.add(0, OFFSET_Y, 0);
        this.position = position;
        this.entity.teleport(position);
    }

    /**
     * Gets the hologram text.
     *
     * @return the hologram text
     * @deprecated Use {@link #getText()}
     */
    @Deprecated
    public JsonMessage getTextJson() {
        return JsonMessage.fromComponent(text);
    }

    /**
     * Gets the hologram text.
     *
     * @return the hologram text
     */
    public Component getText() {
        return text;
    }

    /**
     * Changes the hologram text.
     *
     * @param text the new hologram text
     * @deprecated Use {@link #setText(Component)}
     */
    @Deprecated
    public void setText(JsonMessage text) {
        this.setText(text.asComponent());
    }

    /**
     * Changes the hologram text.
     *
     * @param text the new hologram text
     */
    public void setText(Component text) {
        checkRemoved();
        this.text = text;
        this.entity.setCustomName(text);
    }

    /**
     * Removes the hologram.
     */
    public void remove() {
        this.removed = true;
        this.entity.remove();
    }

    /**
     * Checks if the hologram is still present.
     *
     * @return true if the hologram is present, false otherwise
     */
    public boolean isRemoved() {
        return removed;
    }

    /**
     * Gets the hologram entity (armor stand).
     *
     * @return the hologram entity
     */
    public HologramEntity getEntity() {
        return entity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addViewer(@NotNull Player player) {
        return entity.addViewer(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeViewer(@NotNull Player player) {
        return entity.removeViewer(player);
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public Set<Player> getViewers() {
        return entity.getViewers();
    }

    /**
     * @see #isRemoved()
     */
    private void checkRemoved() {
        Check.stateCondition(isRemoved(), "You cannot interact with a removed Hologram");
    }


    public static class HologramEntity extends EntityArmorStand {

        /**
         * Constructs a new {@link HologramEntity} with the given {@code spawnPosition}.
         *
         * @param spawnPosition The spawn position of this hologram entity.
         */
        public HologramEntity(Position spawnPosition) {
            super(spawnPosition);
            setSmall(true);

            setNoGravity(true);
            setCustomName(Component.empty());
            setCustomNameVisible(true);
            setInvisible(true);
        }

    }
}
