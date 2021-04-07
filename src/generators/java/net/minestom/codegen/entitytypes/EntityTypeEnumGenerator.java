package net.minestom.codegen.entitytypes;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.squareup.javapoet.*;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.codegen.ConstructorLambda;
import net.minestom.codegen.EnumGenerator;
import net.minestom.codegen.MinestomEnumGenerator;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntitySpawnType;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.ResourceGatherer;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.function.BiFunction;

/**
 * Generates an EntityType enum containing all data about entity types
 */
public class EntityTypeEnumGenerator extends MinestomEnumGenerator<EntityTypeContainer> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityTypeEnumGenerator.class);

    private final String targetVersion;
    private final File targetFolder;

    public static void main(String[] args) throws IOException {
        String targetVersion;
        if (args.length < 1) {
            System.err.println("Usage: <MC version> [target folder]");
            return;
        }

        targetVersion = args[0];

        try {
            ResourceGatherer.ensureResourcesArePresent(targetVersion); // TODO
        } catch (IOException e) {
            e.printStackTrace();
        }

        String targetPart = DEFAULT_TARGET_PATH;
        if (args.length >= 2) {
            targetPart = args[1];
        }

        File targetFolder = new File(targetPart);
        if (!targetFolder.exists()) {
            targetFolder.mkdirs();
        }

        new EntityTypeEnumGenerator(targetVersion, targetFolder);
    }

    private EntityTypeEnumGenerator(String targetVersion, File targetFolder) throws IOException {
        this.targetVersion = targetVersion;
        this.targetFolder = targetFolder;
        generateTo(targetFolder);
    }

    /**
     * Extract entity information from Burger (submodule of Minestom)
     *
     * @param gson
     * @param url
     * @return
     * @throws IOException
     */
    private List<BurgerEntity> parseEntitiesFromBurger(Gson gson, String url) throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new URL(url).openStream()))) {
            LOGGER.debug("\tConnection established, reading file");
            JsonObject dictionary = gson.fromJson(bufferedReader, JsonArray.class).get(0).getAsJsonObject();
            JsonObject entitiesMap = dictionary.getAsJsonObject("entities").getAsJsonObject("entity");
            List<BurgerEntity> entities = new LinkedList<>();
            for (var entry : entitiesMap.entrySet()) {
                BurgerEntity entity = gson.fromJson(entry.getValue(), BurgerEntity.class);
                entities.add(entity);
            }
            return entities;
        }
    }

    @Override
    public String getPackageName() {
        return "net.minestom.server.entity";
    }

    @Override
    public String getClassName() {
        return "EntityType";
    }

    @Override
    protected Collection<EntityTypeContainer> compile() throws IOException {
        Gson gson = new Gson();
        LOGGER.debug("Loading PrismarineJS entity types data");
        List<BurgerEntity> burgerEntities = parseEntitiesFromBurger(gson, BURGER_URL_BASE_URL + targetVersion + ".json");

        TreeSet<EntityTypeContainer> types = new TreeSet<>(EntityTypeContainer::compareTo);
        for (var burgerEntity : burgerEntities) {
            if (burgerEntity.name.contains("~")) {
                continue;
            }
            types.add(new EntityTypeContainer(
                    burgerEntity.id,
                    NamespaceID.from("minecraft:" + burgerEntity.name),
                    burgerEntity.width,
                    burgerEntity.height
            ));
        }
        return types;
    }

    @Override
    protected void prepare(EnumGenerator generator) {
        ClassName className = ClassName.get(getPackageName(), getClassName());
        generator.addClassAnnotation(AnnotationSpec.builder(SuppressWarnings.class).addMember("value", "{$S}", "deprecation").build());
        generator.setParams(
                ParameterSpec.builder(String.class, "namespaceID").addAnnotation(NotNull.class).build(),
                ParameterSpec.builder(TypeName.DOUBLE, "width").build(),
                ParameterSpec.builder(TypeName.DOUBLE, "height").build(),
                ParameterSpec.builder(ParameterizedTypeName.get(
                        BiFunction.class,
                        Entity.class,
                        Metadata.class,
                        EntityMeta.class
                ), "metaConstructor").addAnnotation(NotNull.class).build(),
                ParameterSpec.builder(EntitySpawnType.class, "spawnType").addAnnotation(NotNull.class).build()
        );
        generator.appendToConstructor(code -> {
            code.addStatement("$T.$N.put($T.from(namespaceID), this)", Registries.class, "entityTypes", NamespaceID.class);
        });

        generator.addMethod("getId", new ParameterSpec[0], TypeName.SHORT, code -> {
            code.addStatement("return (short) ordinal()");
        });
        generator.addMethod("getNamespaceID", new ParameterSpec[0], ClassName.get(String.class), code -> {
            code.addStatement("return this.namespaceID");
        });
        generator.addMethod("getWidth", new ParameterSpec[0], TypeName.DOUBLE, code -> {
            code.addStatement("return this.width");
        });
        generator.addMethod("getHeight", new ParameterSpec[0], TypeName.DOUBLE, code -> {
            code.addStatement("return this.height");
        });
        generator.addMethod("getMetaConstructor", new ParameterSpec[0],
                ParameterizedTypeName.get(
                        BiFunction.class,
                        Entity.class,
                        Metadata.class,
                        EntityMeta.class
                ),
                code -> code.addStatement("return this.metaConstructor")
        );
        generator.addMethod("getSpawnType", new ParameterSpec[0], ClassName.get(EntitySpawnType.class), code -> {
            code.addStatement("return this.spawnType");
        });

        generator.addStaticField(ArrayTypeName.of(ClassName.get(EntityType.class)), "VALUES", "values()");

        generator.addStaticMethod("fromId", new ParameterSpec[]{ParameterSpec.builder(TypeName.SHORT, "id").build()}, className, code -> {
            code.beginControlFlow("if(id >= 0 && id < VALUES.length)")
                    .addStatement("return VALUES[id]")
                    .endControlFlow()
                    .addStatement("return null");
        });

        // implement Keyed
        generator.addSuperinterface(ClassName.get(Keyed.class));
        generator.addField(ClassName.get(Key.class), "key", true);
        generator.appendToConstructor(code -> code.addStatement("this.key = Key.key(this.namespaceID)"));
        generator.addMethod("key", new ParameterSpec[0], ClassName.get(Key.class), code -> code.addStatement("return this.key"));
    }

    @Override
    protected void writeSingle(EnumGenerator generator, EntityTypeContainer type) {
        String instanceName = type.getName().getPath().toUpperCase();
        generator.addInstance(instanceName,
                "\"" + type.getName().toString() + "\"",
                type.getWidth(),
                type.getHeight(),
                new ConstructorLambda(ClassName.get(type.getMetaClass())),
                "EntitySpawnType." + type.getSpawnType().name()
        );
    }

    @Override
    protected List<JavaFile> postGeneration(Collection<EntityTypeContainer> types) {
        return Collections.emptyList();
    }

    @Override
    protected void postWrite(EnumGenerator generator) {
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }
}
