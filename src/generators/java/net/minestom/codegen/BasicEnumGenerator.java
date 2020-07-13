package net.minestom.codegen;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minestom.server.registry.Registries;
import net.minestom.server.utils.NamespaceID;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Objects;
import java.util.TreeSet;

public abstract class BasicEnumGenerator extends MinestomEnumGenerator<BasicEnumGenerator.Container> {

    private static final String MC_DATA_REGISTRIES_PATH = "minecraft_data/reports/registries.json";
    /**
     * Are IDs linear? Biomes are not for instance.
     */
    private final boolean linear;
    private NamespaceID defaultEntry;

    protected BasicEnumGenerator(File targetFolder) throws IOException {
        this(targetFolder, true);
    }

    protected BasicEnumGenerator(File targetFolder, boolean linear) throws IOException {
        this.linear = linear;
        generateTo(targetFolder);
    }

    @Override
    protected Collection<Container> compile() throws IOException {
        Gson gson = new Gson();

        TreeSet<Container> items = new TreeSet<>();

        JsonObject root = gson.fromJson(new FileReader(MC_DATA_REGISTRIES_PATH), JsonObject.class);
        JsonObject category = root.getAsJsonObject(getCategoryID());
        Objects.requireNonNull(category, "Category "+getCategoryID()+" not found in registries.json!");
        JsonObject entries = category.getAsJsonObject("entries");
        if(category.has("default")) {
            defaultEntry = NamespaceID.from(category.get("default").getAsString());
        }
        for(var entry : entries.entrySet()) {
            NamespaceID name = NamespaceID.from(entry.getKey());
            int id = entry.getValue().getAsJsonObject().get("protocol_id").getAsInt();
            items.add(new Container(id, name));
        }

        return items;
    }

    protected abstract String getCategoryID();

    @Override
    protected void postWrite(EnumGenerator generator) {
        if(linear) {
            generator.addMethod("fromId", "(int id)", "static "+getClassName(),
                    "if(id >= 0 && id < values().length) {",
                    "\treturn values()[id];",
                    "}",
                    "return "+(defaultEntry == null ? "null" : identifier(defaultEntry))+";"
            );
        } else {
            generator.addMethod("fromId", "(int id)", "static "+getClassName(),
                    "for("+getClassName()+" o : values()) {",
                    "\tif(o.getId() == id) {",
                    "\t\treturn o;",
                    "\t}",
                    "}",
                    "return "+(defaultEntry == null ? "null" : identifier(defaultEntry))+";"
            );
        }
    }

    private String identifier(NamespaceID id) {
        return id.getPath().toUpperCase().replace(".", "_"); // block.ambient.cave will be replaced by "BLOCK_AMBIENT_CAVE"
    }

    @Override
    protected void postGeneration() throws IOException {}

    @Override
    protected void prepare(EnumGenerator generator) {
        generator.addClassAnnotation("@SuppressWarnings({\"deprecation\"})");
        generator.addImport(Registries.class.getCanonicalName());
        generator.addImport(NamespaceID.class.getCanonicalName());
        if(linear) {
            generator.setParams("String namespaceID");
            generator.addMethod("getId", "()", "int", "return ordinal();");
        } else {
            generator.setParams("String namespaceID", "int id");
            generator.addMethod("getId", "()", "int", "return id;");
        }
        generator.addMethod("getNamespaceID", "()", "String", "return namespaceID;");

        generator.appendToConstructor("Registries."+CodeGenerator.decapitalize(getClassName())+"s.put(NamespaceID.from(namespaceID), this);");
    }

    @Override
    protected void writeSingle(EnumGenerator generator, Container item) {
        if(linear) {
            generator.addInstance(identifier(item.name), "\""+item.name.toString()+"\"");
        } else {
            generator.addInstance(identifier(item.name), "\""+item.name.toString()+"\"", item.id);
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
