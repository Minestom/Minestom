package net.minestom.codegen.enchantment;

import net.minestom.codegen.BasicEnumGenerator;
import net.minestom.server.registry.ResourceGatherer;

import java.io.File;
import java.io.IOException;

public class EnchantmentEnumGenerator extends BasicEnumGenerator {
    public static void main(String[] args) throws IOException {
        String targetVersion;
        if(args.length < 1) {
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
        if(args.length >= 2) {
            targetPart = args[1];
        }

        File targetFolder = new File(targetPart);
        if(!targetFolder.exists()) {
            targetFolder.mkdirs();
        }

        new EnchantmentEnumGenerator(targetFolder);
    }

    private EnchantmentEnumGenerator(File targetFolder) throws IOException {
        super(targetFolder);
    }

    @Override
    protected String getCategoryID() {
        return "minecraft:enchantment";
    }

    @Override
    public String getPackageName() {
        return "net.minestom.server.item";
    }

    @Override
    public String getClassName() {
        return "Enchantment";
    }
}
