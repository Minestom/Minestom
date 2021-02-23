package net.minestom.codegen.entitytypes;

import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

public class EntityTypeContainer implements Comparable<EntityTypeContainer> {

    private int id;
    private NamespaceID name;
    private double width;
    private double height;

    public EntityTypeContainer(int id, NamespaceID name, double width, double height) {
        this.id = id;
        this.name = name;
        this.width = width;
        this.height = height;
    }

    public int getId() {
        return id;
    }

    public NamespaceID getName() {
        return name;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    @Override
    public int compareTo(@NotNull EntityTypeContainer o) {
        return Integer.compare(id, o.id);
    }
}
