package net.minestom.codegen.sounds;

import com.squareup.javapoet.ClassName;
import net.kyori.adventure.sound.Sound;
import net.minestom.codegen.BasicEnumGenerator;
import net.minestom.codegen.EnumGenerator;
import net.minestom.server.registry.ResourceGatherer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class SoundEnumGenerator extends BasicEnumGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SoundEnumGenerator.class);

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

        new SoundEnumGenerator(targetFolder);
    }

    private SoundEnumGenerator(File targetFolder) throws IOException {
        super(targetFolder);
    }

    @Override
    protected void prepare(EnumGenerator generator) {
        super.prepare(generator);

        // implement type as well
        generator.addSuperinterface(ClassName.get(Sound.Type.class));
    }

    @Override
    protected String getCategoryID() {
        return "minecraft:sound_event";
    }

    @Override
    public String getPackageName() {
        return "net.minestom.server.sound";
    }

    @Override
    public String getClassName() {
        return "SoundEvent";
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }
}
