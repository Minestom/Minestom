package net.minestom.codegen;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.squareup.javapoet.*;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.registry.Registries;
import net.minestom.server.utils.NamespaceID;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public abstract class BasicEnumGenerator extends MinestomEnumGenerator<BasicEnumGenerator.Container> {

    private static final String MC_DATA_REGISTRIES_PATH = "minecraft_data/reports/registries.json";
    /**
     * Are IDs linear? Biomes are not for instance.
     */
    private final boolean linear;
    private NamespaceID defaultEntry;

    /**
     * True if the enum is linear and start by 1 instead of 0
     */
    private boolean incrementOrdinal;

    protected BasicEnumGenerator(File targetFolder, boolean linear, boolean incrementOrdinal) throws IOException {
        this.linear = linear;
        this.incrementOrdinal = incrementOrdinal;
        generateTo(targetFolder);
    }

    protected BasicEnumGenerator(File targetFolder, boolean linear) throws IOException {
        this(targetFolder, linear, false);
    }

    protected BasicEnumGenerator(File targetFolder) throws IOException {
        this(targetFolder, true);
    }

    @Override
    protected Collection<Container> compile() throws IOException {
        Gson gson = new Gson();

        TreeSet<Container> items = new TreeSet<>();

        JsonObject root = gson.fromJson(new FileReader(MC_DATA_REGISTRIES_PATH), JsonObject.class);
        JsonObject category = root.getAsJsonObject(getCategoryID());
        Objects.requireNonNull(category, "Category " + getCategoryID() + " not found in registries.json!");
        final JsonObject entries = category.getAsJsonObject("entries");
        if (category.has("default")) {
            defaultEntry = NamespaceID.from(category.get("default").getAsString());
        }
        for (var entry : entries.entrySet()) {
            final NamespaceID name = NamespaceID.from(entry.getKey());
            final int id = entry.getValue().getAsJsonObject().get("protocol_id").getAsInt();
            items.add(new Container(id, name));
        }

        return items;
    }

    protected abstract String getCategoryID();

    @Override
    protected void postWrite(EnumGenerator generator) {
        ClassName className = ClassName.get(getPackageName(), getClassName());
        ParameterSpec idParam = ParameterSpec.builder(TypeName.INT, "id").build();
        ParameterSpec[] signature = new ParameterSpec[]{idParam};
        if (linear) {
            final String ordinalIncrementCondition = incrementOrdinal ? " + 1" : "";
            final String ordinalIncrementIndex = incrementOrdinal ? " - 1" : "";
            generator.addStaticMethod("fromId", signature, className, code -> {
                        code.beginControlFlow("if ($N >= 0 && $N < values().length" + ordinalIncrementCondition + ")", idParam, idParam)
                                .addStatement("return values()[$N" + ordinalIncrementIndex + "]", idParam)
                                .endControlFlow()
                                .addStatement("return " + (defaultEntry == null ? "null" : identifier(defaultEntry)));
                    }
            );
        } else {
            generator.addStaticMethod("fromId", signature, className, code -> {
                        code.beginControlFlow("for ($T o : values())")
                                .beginControlFlow("if (o.getId() == id)")
                                .addStatement("return o")
                                .endControlFlow()
                                .endControlFlow()
                                .addStatement("return " + (defaultEntry == null ? "null" : identifier(defaultEntry)));
                    }
            );
        }
    }

    private String identifier(NamespaceID id) {
        return id.getPath().toUpperCase().replace(".", "_"); // block.ambient.cave will be replaced by "BLOCK_AMBIENT_CAVE"
    }

    @Override
    protected List<JavaFile> postGeneration(Collection<Container> items) throws IOException {
        return Collections.emptyList();
    }

    @Override
    protected void prepare(EnumGenerator generator) {
        generator.addClassAnnotation(AnnotationSpec.builder(SuppressWarnings.class).addMember("value", "{$S}", "deprecation").build());
        ClassName registriesClass = ClassName.get(Registries.class);
        if (linear) {
            generator.setParams(ParameterSpec.builder(ClassName.get(String.class), "namespaceID").build());
            generator.addMethod("getId", new ParameterSpec[0], TypeName.INT, code -> code.addStatement("return ordinal()" + (incrementOrdinal ? " + 1" : "")));
        } else {
            generator.setParams(ParameterSpec.builder(ClassName.get(String.class), "namespaceID").build(), ParameterSpec.builder(TypeName.INT, "id").build());
            generator.addMethod("getId", new ParameterSpec[0], TypeName.INT, code -> code.addStatement("return $N", "id"));
        }
        generator.addMethod("getNamespaceID", new ParameterSpec[0], ClassName.get(String.class), code -> code.addStatement("return $N", "namespaceID"));

        generator.appendToConstructor(code -> {
            code.addStatement("$T." + CodeGenerator.decapitalize(getClassName()) + "s.put($T.from($N), this)", registriesClass, NamespaceID.class, "namespaceID");
        });

        // implement Keyed
        generator.addSuperinterface(ClassName.get(Keyed.class));
        generator.addField(ClassName.get(Key.class), "key", true);
        generator.appendToConstructor(code -> code.addStatement("this.key = Key.key(this.namespaceID)"));
        generator.addMethod("key", new ParameterSpec[0], ClassName.get(Key.class), code -> code.addStatement("return this.key"));
    }

    @Override
    protected void writeSingle(EnumGenerator generator, Container item) {
        if (linear) {
            generator.addInstance(identifier(item.name), "\"" + item.name.toString() + "\"");
        } else {
            generator.addInstance(identifier(item.name), "\"" + item.name.toString() + "\"", item.id);
        }
    }

    static class Container implements Comparable<Container> {
        private int id;
        private NamespaceID name;

        public Container(int id, NamespaceID name) {
            this.id = id;
            this.name = name;
        }

        public NamespaceID getName() {
            return name;
        }

        public int getId() {
            return id;
        }

        @Override
        public int compareTo(Container o) {
            return Integer.compare(id, o.id);
        }
    }
}
