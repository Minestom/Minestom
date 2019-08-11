package fr.themode.minestom.entity;

import fr.themode.minestom.instance.Chunk;
import fr.themode.minestom.instance.Instance;

import java.util.UUID;

public class Entity {

    private static volatile int lastEntityId;
    protected Instance instance;
    protected double lastX, lastY, lastZ;
    protected double x, y, z;
    private int id;
    protected UUID uuid;
    private boolean isActive; // False if entity has only been instanced without being added somewhere
    private boolean shouldRemove;

    private Object monitor = new Object();

    public Entity() {
        this.id = generateId();
        this.uuid = UUID.randomUUID();
    }

    private static int generateId() {
        return ++lastEntityId;
    }

    public int getEntityId() {
        return id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean isActive() {
        return isActive;
    }

    public Instance getInstance() {
        return instance;
    }

    public void setInstance(Instance instance) {
        if (instance == null)
            throw new IllegalArgumentException("instance cannot be null!");

        if (this.instance != null) {
            this.instance.removeEntity(this);
        }

        this.isActive = true;
        this.instance = instance;
        instance.addEntity(this);
    }

    public void refreshPosition(double x, double y, double z) {
        this.lastX = this.x;
        this.lastY = this.y;
        this.lastZ = this.z;
        this.x = x;
        this.y = y;
        this.z = z;

        Instance instance = getInstance();
        if (instance != null) {
            Chunk lastChunk = instance.getChunkAt(lastX, lastZ);
            Chunk newChunk = instance.getChunkAt(x, z);
            if (newChunk != null && lastChunk != newChunk) {
                synchronized (lastChunk) {
                    synchronized (newChunk) {
                        lastChunk.removeEntity(this);
                        newChunk.addEntity(this);
                    }
                }
            }
        }
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public void remove() {
        this.shouldRemove = true;
    }

    protected boolean shouldRemove() {
        return shouldRemove;
    }
}
