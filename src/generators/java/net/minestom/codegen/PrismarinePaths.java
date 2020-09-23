package net.minestom.codegen;

import lombok.Getter;

import java.io.File;

public class PrismarinePaths {

    @Getter private String blocks;
    @Getter private String biomes;
    @Getter private String effects;
    @Getter private String items;
    @Getter private String recipes;
    @Getter private String instruments;
    @Getter private String materials;
    @Getter private String entities;
    @Getter private String protocol;
    @Getter private String windows;
    @Getter private String version;
    @Getter private String language;

    public File getBlockFile() {
        return getFile(blocks, "blocks");
    }

    public File getItemsFile() {
        return getFile(items, "items");
    }

    public File getBiomesFile() {
        return getFile(biomes, "biomes");
    }

    public File getFile(String path, String type) {
        return new File("prismarine-minecraft-data/data/"+path+"/"+type+".json");
    }
}
