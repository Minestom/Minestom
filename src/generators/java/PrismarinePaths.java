import java.io.File;

public class PrismarinePaths {

    private String blocks;
    private String biomes;
    private String effects;
    private String items;
    private String recipes;
    private String instruments;
    private String materials;
    private String entities;
    private String protocol;
    private String windows;
    private String version;
    private String language;

    public File getBlockFile() {
        return new File("prismarine-minecraft-data/data/"+blocks+"/blocks.json");
    }
}
