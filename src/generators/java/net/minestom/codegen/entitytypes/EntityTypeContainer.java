package net.minestom.codegen.entitytypes;

import com.google.common.base.CaseFormat;
import net.minestom.server.entity.EntitySpawnType;
import net.minestom.server.entity.metadata.LivingEntityMeta;
import net.minestom.server.entity.metadata.PlayerMeta;
import net.minestom.server.entity.metadata.other.ExperienceOrbMeta;
import net.minestom.server.entity.metadata.other.PaintingMeta;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class EntityTypeContainer implements Comparable<EntityTypeContainer> {

    private int id;
    private NamespaceID name;
    private double width;
    private double height;
    private Class<?> metaClass;
    private EntitySpawnType spawnType;
    double gravityAcceleration;
    double gravityDrag;

    public EntityTypeContainer(int id, NamespaceID name, double width, double height) {
        this.id = id;
        this.name = name;
        this.width = width;
        this.height = height;
        String metaClassName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, name.getPath());
        // special cases
        switch (metaClassName) {
            case "Item":
                metaClassName = "ItemEntity";
                break;
            case "Tnt":
                metaClassName = "PrimedTnt";
                break;
            case "FishingBobber":
                metaClassName = "FishingHook";
                break;
            case "Egg":
            case "EnderPearl":
            case "ExperienceBottle":
            case "Potion":
            case "Trident":
                metaClassName = "Thrown" + metaClassName;
                break;
            default:
                break;
        }
        metaClassName += "Meta";
        this.metaClass = findClassIn("net.minestom.server.entity.metadata", metaClassName);

        if (this.metaClass == PlayerMeta.class) {
            this.spawnType = EntitySpawnType.PLAYER;
        } else if (this.metaClass == PaintingMeta.class) {
            this.spawnType = EntitySpawnType.PAINTING;
        } else if (this.metaClass == ExperienceOrbMeta.class) {
            this.spawnType = EntitySpawnType.EXPERIENCE_ORB;
        } else if (LivingEntityMeta.class.isAssignableFrom(this.metaClass)) {
            this.spawnType = EntitySpawnType.LIVING;
        } else {
            this.spawnType = EntitySpawnType.BASE;
        }

        // Default gravity values
        // according to https://minecraft.gamepedia.com/Entity#Motion_of_entities
        switch (name.getPath()) {
            // 0
            case "item_frame":
                this.gravityAcceleration = 0;
                break;
            // 0.03
            case "egg":
            case "fishing_bobber":
            case "experience_orb":
            case "ender_pearl":
            case "potion":
            case "snowball":
                this.gravityAcceleration = 0.03;
                break;
            // 0.04
            case "boat":
            case "tnt":
            case "falling_block":
            case "item":
            case "minecart":
                this.gravityAcceleration = 0.04;
                break;
            // 0.05
            case "arrow":
            case "spectral_arrow":
            case "trident":
                this.gravityAcceleration = 0.05;
                break;
            // 0.06
            case "llama_spit":
                this.gravityAcceleration = 0.06;
                break;
            // 0.1
            case "fireball":
            case "wither_skull":
            case "dragon_fireball":
                this.gravityAcceleration = 0.1;
                break;
            // 0.08
            default:
                this.gravityAcceleration = 0.08;
                break;
        }
        switch (name.getPath()) {
            // 0
            case "boat":
                this.gravityDrag = 0;
                break;
            // 0.01
            case "llama_spit":
            case "ender_pearl":
            case "potion":
            case "snowball":
            case "egg":
            case "trident":
            case "spectral_arrow":
            case "arrow":
                this.gravityDrag = 0.01;
                break;
            // 0.05
            case "minecart":
                this.gravityDrag = 0.05;
                break;
            // 0.08
            case "fishing_bobber":
                this.gravityDrag = 0.08;
                break;
            // 0.02
            default:
                this.gravityDrag = 0.02;
                break;
        }
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

    public Class<?> getMetaClass() {
        return metaClass;
    }

    public EntitySpawnType getSpawnType() {
        return spawnType;
    }

    public double getGravityAcceleration() {
        return gravityAcceleration;
    }

    public double getGravityDrag() {
        return gravityDrag;
    }

    @Override
    public int compareTo(@NotNull EntityTypeContainer o) {
        return Integer.compare(id, o.id);
    }

    private static Class<?> findClassIn(String pkg, String className) {
        try {
            return getClasses(pkg).stream()
                    .filter(clazz -> clazz.getSimpleName().equals(className))
                    .findAny()
                    .orElseThrow();
        } catch (Throwable t) {
            throw new IllegalStateException("Could not find class " + className + " in " + pkg, t);
        }
    }

    private static List<Class<?>> getClasses(String packageName)
            throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        List<Class<?>> classes = new ArrayList<>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes;
    }

    /**
     * Recursive method used to find all classes in a given directory and subdirs.
     *
     * @param directory   The base directory
     * @param packageName The package name for classes found inside the base directory
     * @return The classes
     * @throws ClassNotFoundException
     */
    private static List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }
}
