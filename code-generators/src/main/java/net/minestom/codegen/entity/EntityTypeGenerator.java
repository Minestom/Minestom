package net.minestom.codegen.entity;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.squareup.javapoet.*;
import net.minestom.codegen.MinestomCodeGenerator;
import net.minestom.codegen.util.NameUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public final class EntityTypeGenerator extends MinestomCodeGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(EntityTypeGenerator.class);
    private static final Map<String, String> metadata = new HashMap<>() {{
        // Class's name (without the Meta suffix) <--> Package
        // UPDATE: Handle new entity metadata
        // Ambient
        // put("AmbientCreature", "net.minestom.server.entity.metadata.ambient");
        put("Bat", "net.minestom.server.entity.metadata.ambient");
        // Animal
        // put("Animal", "net.minestom.server.entity.metadata.animal");
        put("AbstractHorse", "net.minestom.server.entity.metadata.animal");
        put("Bee", "net.minestom.server.entity.metadata.animal");
        put("ChestedHorse", "net.minestom.server.entity.metadata.animal");
        put("Chicken", "net.minestom.server.entity.metadata.animal");
        put("Cow", "net.minestom.server.entity.metadata.animal");
        put("Donkey", "net.minestom.server.entity.metadata.animal");
        put("Fox", "net.minestom.server.entity.metadata.animal");
        put("Goat", "net.minestom.server.entity.metadata.animal");
        put("Hoglin", "net.minestom.server.entity.metadata.animal");
        put("Horse", "net.minestom.server.entity.metadata.animal");
        put("Llama", "net.minestom.server.entity.metadata.animal");
        put("Mooshroom", "net.minestom.server.entity.metadata.animal");
        put("Mule", "net.minestom.server.entity.metadata.animal");
        put("Ocelot", "net.minestom.server.entity.metadata.animal");
        put("Panda", "net.minestom.server.entity.metadata.animal");
        put("Pig", "net.minestom.server.entity.metadata.animal");
        put("PolarBear", "net.minestom.server.entity.metadata.animal");
        put("Rabbit", "net.minestom.server.entity.metadata.animal");
        put("Sheep", "net.minestom.server.entity.metadata.animal");
        put("SkeletonHorse", "net.minestom.server.entity.metadata.animal");
        put("Strider", "net.minestom.server.entity.metadata.animal");
        put("Turtle", "net.minestom.server.entity.metadata.animal");
        put("ZombieHorse", "net.minestom.server.entity.metadata.animal");
        // Animal - Tameable
        // put("TameableAnimal", "net.minestom.server.entity.metadata.animal.tameable");
        put("Cat", "net.minestom.server.entity.metadata.animal.tameable");
        put("Parrot", "net.minestom.server.entity.metadata.animal.tameable");
        put("Wolf", "net.minestom.server.entity.metadata.animal.tameable");
        // Arrow
        // put("AbstractArrow", "net.minestom.server.entity.metadata.arrow");
        put("Arrow", "net.minestom.server.entity.metadata.arrow");
        put("SpectralArrow", "net.minestom.server.entity.metadata.arrow");
        put("ThrownTrident", "net.minestom.server.entity.metadata.arrow");
        // Flying
        // put("Flying", "net.minestom.server.entity.metadata.flying");
        put("Ghast", "net.minestom.server.entity.metadata.flying");
        put("Phantom", "net.minestom.server.entity.metadata.flying");
        // Golem
        // put("AbstractGolem", "net.minestom.server.entity.metadata.golem");
        put("IronGolem", "net.minestom.server.entity.metadata.golem");
        put("Shulker", "net.minestom.server.entity.metadata.golem");
        put("SnowGolem", "net.minestom.server.entity.metadata.golem");
        // Item
        put("EyeOfEnder", "net.minestom.server.entity.metadata.item");
        put("Fireball", "net.minestom.server.entity.metadata.item");
        put("ItemContaining", "net.minestom.server.entity.metadata.item");
        put("ItemEntity", "net.minestom.server.entity.metadata.item");
        put("SmallFireball", "net.minestom.server.entity.metadata.item");
        put("Snowball", "net.minestom.server.entity.metadata.item");
        put("ThrownEgg", "net.minestom.server.entity.metadata.item");
        put("ThrownEnderPearl", "net.minestom.server.entity.metadata.item");
        put("ThrownExperienceBottle", "net.minestom.server.entity.metadata.item");
        put("ThrownPotion", "net.minestom.server.entity.metadata.item");
        // Minecart
        // put("AbstractMinecart", "net.minestom.server.entity.metadata.minecart");
        // put("AbstractMinecartContainer", "net.minestom.server.entity.metadata.minecart");
        put("ChestMinecart", "net.minestom.server.entity.metadata.minecart");
        put("CommandBlockMinecart", "net.minestom.server.entity.metadata.minecart");
        put("FurnaceMinecart", "net.minestom.server.entity.metadata.minecart");
        put("HopperMinecart", "net.minestom.server.entity.metadata.minecart");
        put("Minecart", "net.minestom.server.entity.metadata.minecart");
        put("SpawnerMinecart", "net.minestom.server.entity.metadata.minecart");
        put("TntMinecart", "net.minestom.server.entity.metadata.minecart");
        // Monster
        // put("Monster", "net.minestom.server.entity.metadata.monster");
        // put("BasePiglin", "net.minestom.server.entity.metadata.monster");
        put("Blaze", "net.minestom.server.entity.metadata.monster");
        put("CaveSpider", "net.minestom.server.entity.metadata.monster");
        put("Creeper", "net.minestom.server.entity.metadata.monster");
        put("ElderGuardian", "net.minestom.server.entity.metadata.monster");
        put("Enderman", "net.minestom.server.entity.metadata.monster");
        put("Endermite", "net.minestom.server.entity.metadata.monster");
        put("Giant", "net.minestom.server.entity.metadata.monster");
        put("Guardian", "net.minestom.server.entity.metadata.monster");
        put("PiglinBrute", "net.minestom.server.entity.metadata.monster");
        put("Piglin", "net.minestom.server.entity.metadata.monster");
        put("Silverfish", "net.minestom.server.entity.metadata.monster");
        put("Spider", "net.minestom.server.entity.metadata.monster");
        put("Vex", "net.minestom.server.entity.metadata.monster");
        put("Wither", "net.minestom.server.entity.metadata.monster");
        put("Zoglin", "net.minestom.server.entity.metadata.monster");
        // Monster - Raider
        // put("AbstractIllager", "net.minestom.server.entity.metadata.monster.raider")
        put("Evoker", "net.minestom.server.entity.metadata.monster.raider");
        put("Illusioner", "net.minestom.server.entity.metadata.monster.raider");
        put("Pillager", "net.minestom.server.entity.metadata.monster.raider");
        put("Raider", "net.minestom.server.entity.metadata.monster.raider");
        put("Ravager", "net.minestom.server.entity.metadata.monster.raider");
        put("SpellcasterIllager", "net.minestom.server.entity.metadata.monster.raider");
        put("Vindicator", "net.minestom.server.entity.metadata.monster.raider");
        put("Witch", "net.minestom.server.entity.metadata.monster.raider");
        // Monster - Skeleton
        // put("AbstractSkeleton", "net.minestom.server.entity.metadata.monster.skeleton");
        put("Skeleton", "net.minestom.server.entity.metadata.monster.skeleton");
        put("Stray", "net.minestom.server.entity.metadata.monster.skeleton");
        put("WitherSkeleton", "net.minestom.server.entity.metadata.monster.skeleton");
        // Monster - Zombie
        put("Drowned", "net.minestom.server.entity.metadata.monster.zombie");
        put("Husk", "net.minestom.server.entity.metadata.monster.zombie");
        put("Zombie", "net.minestom.server.entity.metadata.monster.zombie");
        put("ZombieVillager", "net.minestom.server.entity.metadata.monster.zombie");
        put("ZombifiedPiglin", "net.minestom.server.entity.metadata.monster.zombie");
        // Other
        put("AreaEffectCloud", "net.minestom.server.entity.metadata.other");
        put("ArmorStand", "net.minestom.server.entity.metadata.other");
        put("Boat", "net.minestom.server.entity.metadata.other");
        put("DragonFireball", "net.minestom.server.entity.metadata.other");
        put("EndCrystal", "net.minestom.server.entity.metadata.other");
        put("EnderDragon", "net.minestom.server.entity.metadata.other");
        put("EvokerFangs", "net.minestom.server.entity.metadata.other");
        put("ExperienceOrb", "net.minestom.server.entity.metadata.other");
        put("FallingBlock", "net.minestom.server.entity.metadata.other");
        put("FireworkRocket", "net.minestom.server.entity.metadata.other");
        put("FishingHook", "net.minestom.server.entity.metadata.other");
        put("GlowItemFrame", "net.minestom.server.entity.metadata.other");
        put("ItemFrame", "net.minestom.server.entity.metadata.other");
        put("LeashKnot", "net.minestom.server.entity.metadata.other");
        put("LightningBolt", "net.minestom.server.entity.metadata.other");
        put("LlamaSpit", "net.minestom.server.entity.metadata.other");
        put("MagmaCube", "net.minestom.server.entity.metadata.other");
        put("Marker", "net.minestom.server.entity.metadata.other");
        put("Painting", "net.minestom.server.entity.metadata.other");
        put("PrimedTnt", "net.minestom.server.entity.metadata.other");
        put("ShulkerBullet", "net.minestom.server.entity.metadata.other");
        put("Slime", "net.minestom.server.entity.metadata.other");
        put("TraderLlama", "net.minestom.server.entity.metadata.other");
        put("WitherSkull", "net.minestom.server.entity.metadata.other");
        // Villager
        // put("AbstractVillager", "net.minestom.server.entity.metadata.villager");
        put("Villager", "net.minestom.server.entity.metadata.villager");
        put("WanderingTrader", "net.minestom.server.entity.metadata.villager");
        // Water
        // put("WaterAnimalMeta", "net.minestom.server.entity.metadata.water")
        put("Axolotl", "net.minestom.server.entity.metadata.water");
        put("Squid", "net.minestom.server.entity.metadata.water");
        put("GlowSquid", "net.minestom.server.entity.metadata.water");
        put("Dolphin", "net.minestom.server.entity.metadata.water");
        // Water - Fish
        // put("AbstractFish", "net.minestom.server.entity.metadata.water.fish");
        put("Cod", "net.minestom.server.entity.metadata.water.fish");
        put("Pufferfish", "net.minestom.server.entity.metadata.water.fish");
        put("Salmon", "net.minestom.server.entity.metadata.water.fish");
        put("TropicalFish", "net.minestom.server.entity.metadata.water.fish");
        // Player
        put("Player", "net.minestom.server.entity.metadata");

    }};
    private final InputStream entitiesFile;
    private final File outputFolder;

    public EntityTypeGenerator(@Nullable InputStream entitiesFile, @NotNull File outputFolder) {
        this.entitiesFile = entitiesFile;
        this.outputFolder = outputFolder;
    }

    @Override
    public void generate() {
        if (entitiesFile == null) {
            LOGGER.error("Failed to find entities.json.");
            LOGGER.error("Stopped code generation for entities.");
            return;
        }
        if (!outputFolder.exists() && !outputFolder.mkdirs()) {
            LOGGER.error("Output folder for code generation does not exist and could not be created.");
            return;
        }
        // Important classes we use alot
        ClassName namespaceIDClassName = ClassName.get("net.minestom.server.utils", "NamespaceID");
        ClassName registriesClassName = ClassName.get("net.minestom.server.registry", "Registries");

        JsonObject entities = GSON.fromJson(new InputStreamReader(entitiesFile), JsonObject.class);
        ClassName entityClassName = ClassName.get("net.minestom.server.entity", "EntityType");

        // Particle
        TypeSpec.Builder entityClass = TypeSpec.enumBuilder(entityClassName)
                .addSuperinterface(ClassName.get("net.kyori.adventure.key", "Keyed"))
                .addModifiers(Modifier.PUBLIC).addJavadoc("AUTOGENERATED by " + getClass().getSimpleName());

        entityClass.addField(
                FieldSpec.builder(namespaceIDClassName, "id")
                        .addModifiers(Modifier.PRIVATE, Modifier.FINAL).addAnnotation(NotNull.class).build()
        );
        entityClass.addField(
                FieldSpec.builder(TypeName.DOUBLE, "width")
                        .addModifiers(Modifier.PRIVATE, Modifier.FINAL).build()
        );
        entityClass.addField(
                FieldSpec.builder(TypeName.DOUBLE, "height")
                        .addModifiers(Modifier.PRIVATE, Modifier.FINAL).build()
        );
        entityClass.addField(
                FieldSpec.builder(
                        ParameterizedTypeName.get(
                                ClassName.get(BiFunction.class),
                                ClassName.get("net.minestom.server.entity", "Entity"),
                                ClassName.get("net.minestom.server.entity", "Metadata"),
                                ClassName.get("net.minestom.server.entity.metadata", "EntityMeta")
                        ),
                        "metaConstructor"
                )
                        .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                        .addAnnotation(NotNull.class)
                        .build()
        );
        entityClass.addField(
                FieldSpec.builder(
                        ClassName.get("net.minestom.server.entity", "EntitySpawnType"),
                        "spawnType"
                )
                        .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                        .addAnnotation(NotNull.class)
                        .build()
        );
        // static field
        entityClass.addField(
                FieldSpec.builder(ArrayTypeName.of(entityClassName), "VALUES")
                        .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                        .initializer("values()")
                        .build()
        );

        entityClass.addMethod(
                MethodSpec.constructorBuilder()
                        .addParameter(ParameterSpec.builder(namespaceIDClassName, "id").addAnnotation(NotNull.class).build())
                        .addParameter(ParameterSpec.builder(TypeName.DOUBLE, "width").build())
                        .addParameter(ParameterSpec.builder(TypeName.DOUBLE, "height").build())
                        .addParameter(
                                ParameterSpec.builder(
                                        ParameterizedTypeName.get(
                                                ClassName.get(BiFunction.class),
                                                ClassName.get("net.minestom.server.entity", "Entity"),
                                                ClassName.get("net.minestom.server.entity", "Metadata"),
                                                ClassName.get("net.minestom.server.entity.metadata", "EntityMeta")
                                        ),
                                        "metaConstructor"
                                )
                                        .addAnnotation(NotNull.class)
                                        .build()
                        )
                        .addParameter(
                                ParameterSpec.builder(
                                        ClassName.get("net.minestom.server.entity", "EntitySpawnType"),
                                        "spawnType"
                                )
                                        .addAnnotation(NotNull.class)
                                        .build()
                        )
                        .addStatement("this.id = id")
                        .addStatement("this.width = width")
                        .addStatement("this.height = height")
                        .addStatement("this.metaConstructor = metaConstructor")
                        .addStatement("this.spawnType = spawnType")
                        .addStatement("$T.entityTypes.put(id, this)", registriesClassName)
                        .build()
        );
        // Override key method (adventure)
        entityClass.addMethod(
                MethodSpec.methodBuilder("key")
                        .returns(ClassName.get("net.kyori.adventure.key", "Key"))
                        .addAnnotation(Override.class)
                        .addAnnotation(NotNull.class)
                        .addStatement("return this.id")
                        .addModifiers(Modifier.PUBLIC)
                        .build()
        );
        // getId method
        entityClass.addMethod(
                MethodSpec.methodBuilder("getId")
                        .returns(TypeName.SHORT)
                        .addStatement("return (short) ordinal()")
                        .addModifiers(Modifier.PUBLIC)
                        .build()
        );
        // getNamespaceID method
        entityClass.addMethod(
                MethodSpec.methodBuilder("getNamespaceID")
                        .returns(namespaceIDClassName)
                        .addAnnotation(NotNull.class)
                        .addStatement("return this.id")
                        .addModifiers(Modifier.PUBLIC)
                        .build()
        );
        // getWidth method
        entityClass.addMethod(
                MethodSpec.methodBuilder("getWidth")
                        .returns(TypeName.DOUBLE)
                        .addStatement("return this.width")
                        .addModifiers(Modifier.PUBLIC)
                        .build()
        );
        // getHeight method
        entityClass.addMethod(
                MethodSpec.methodBuilder("getHeight")
                        .returns(TypeName.DOUBLE)
                        .addStatement("return this.height")
                        .addModifiers(Modifier.PUBLIC)
                        .build()
        );
        // getMetaConstructor method
        entityClass.addMethod(
                MethodSpec.methodBuilder("getMetaConstructor")
                        .returns(
                                ParameterizedTypeName.get(
                                        ClassName.get(BiFunction.class),
                                        ClassName.get("net.minestom.server.entity", "Entity"),
                                        ClassName.get("net.minestom.server.entity", "Metadata"),
                                        ClassName.get("net.minestom.server.entity.metadata", "EntityMeta")
                                )
                        )
                        .addStatement("return this.metaConstructor")
                        .addModifiers(Modifier.PUBLIC)
                        .build()
        );
        // getSpawnType method
        entityClass.addMethod(
                MethodSpec.methodBuilder("getSpawnType")
                        .returns(ClassName.get("net.minestom.server.entity", "EntitySpawnType"))
                        .addStatement("return this.spawnType")
                        .addModifiers(Modifier.PUBLIC)
                        .build()
        );
        // fromId Method
        entityClass.addMethod(
                MethodSpec.methodBuilder("fromId")
                        .returns(entityClassName)
                        .addAnnotation(Nullable.class)
                        .addParameter(TypeName.SHORT, "id")
                        .beginControlFlow("if(id >= 0 && id < VALUES.length)")
                        .addStatement("return VALUES[id]")
                        .endControlFlow()
                        .addStatement("return null")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .build()
        );
        // toString method
        entityClass.addMethod(
                MethodSpec.methodBuilder("toString")
                        .addAnnotation(NotNull.class)
                        .addAnnotation(Override.class)
                        .returns(String.class)
                        // this resolves to [Namespace]
                        .addStatement("return \"[\" + this.id + \"]\"")
                        .addModifiers(Modifier.PUBLIC)
                        .build()
        );
        // Use data
        entities.entrySet().forEach(entry -> {
            final String entityNamespace = entry.getKey();
            final String entityConstant = toConstant(entityNamespace);

            JsonObject entity = entry.getValue().getAsJsonObject();

            // Get metaClass (this is a little complicated)
            String metaClassName = NameUtil.convertSnakeCaseToCamelCase(entityConstant.toLowerCase());
            switch (metaClassName) {
                // These are cases where the entity name doesn't fully match up to the meta name.
                // UPDATE: Handle new entity names that don't match up to their meta name.
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
            String packageName = metadata.get(metaClassName);
            String className = metaClassName + "Meta";
            if (packageName == null) {
                LOGGER.error("The Entity metadata for " + entityNamespace + " is not implemented!");
                LOGGER.error("The metadata has been defaulted to EntityMeta.");
                packageName = "net.minestom.server.entity.metadata";
                className = "EntityMeta";
            }

            entityClass.addEnumConstant(
                    entityConstant,
                    TypeSpec.anonymousClassBuilder(
                            "$T.from($S), $L, $L, $T::new, $T.$N",
                            namespaceIDClassName,
                            entityNamespace,
                            entity.get("width").getAsDouble(),
                            entity.get("height").getAsDouble(),
                            ClassName.get(packageName, className),
                            ClassName.get("net.minestom.server.entity", "EntitySpawnType"),
                            entity.get("packetType").getAsString().toUpperCase()
                    ).build()
            );
        });

        // Write files to outputFolder
        writeFiles(
                Collections.singletonList(
                        JavaFile.builder("net.minestom.server.entity", entityClass.build())
                                .indent("    ")
                                .skipJavaLangImports(true)
                                .build()
                ),
                outputFolder
        );
    }
}
