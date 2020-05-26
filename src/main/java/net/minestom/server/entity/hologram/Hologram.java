package net.minestom.server.entity.hologram;

import net.minestom.server.entity.type.EntityArmorStand;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.validate.Check;

public class Hologram {

    private static final float OFFSET_Y = -0.9875f;

    private HologramEntity entity;

    private Position position;
    private String text;

    private boolean removed;

    public Hologram(Instance instance, Position spawnPosition, String text, boolean autoViewable) {
        this.entity = new HologramEntity(spawnPosition.clone().add(0, OFFSET_Y, 0));
        this.entity.setInstance(instance);
        this.entity.setAutoViewable(autoViewable);

        this.position = spawnPosition;
        setText(text);
    }

    public Hologram(Instance instance, Position spawnPosition, String text) {
        this(instance, spawnPosition, text, true);
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        checkRemoved();
        position = position.add(0, OFFSET_Y, 0);
        this.position = position;
        this.entity.teleport(position);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        checkRemoved();
        this.text = text;
        this.entity.setCustomName(text);
    }

    public void remove() {
        this.removed = true;
        this.entity.remove();
    }

    public boolean isRemoved() {
        return removed;
    }

    private void checkRemoved() {
        Check.stateCondition(isRemoved(), "You cannot interact with a removed Hologram");
    }

    private class HologramEntity extends EntityArmorStand {

        public HologramEntity(Position spawnPosition) {
            super(spawnPosition);
            setSmall(true);

            setNoGravity(true);
            setCustomName("");
            setCustomNameVisible(true);
            setInvisible(true);
        }

    }
}
