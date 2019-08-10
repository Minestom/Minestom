package fr.themode.minestom.entity;

import fr.themode.minestom.Main;

import java.util.UUID;

public class Entity {

    private static volatile int lastEntityId;
    protected double x, y, z;
    private int id;
    private UUID uuid;
    private boolean isActive; // False if entity has only been instanced without being added somewhere
    private boolean shouldRemove;

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

    public void addToWorld() {
        this.isActive = true;
        EntityManager entityManager = Main.getEntityManager();
        if (this instanceof LivingEntity) {
            entityManager.addLivingEntity((LivingEntity) this);
        }
    }

    public void setPosition(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
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
