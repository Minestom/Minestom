package net.minestom.codegen;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public final class ParticleGenerator extends MinestomCodeGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ParticleGenerator.class);

    private final InputStream particlesFile;
    private final File outputFolder;

    public ParticleGenerator(@Nullable InputStream particlesFile, @NotNull File outputFolder) {
        this.particlesFile = particlesFile;
        this.outputFolder = outputFolder;
    }

    @Override
    public void generate() {
        if (particlesFile == null) {
            LOGGER.error("Failed to find particles.json.");
            LOGGER.error("Stopped code generation for particles.");
            return;
        }
        if (!outputFolder.exists() && !outputFolder.mkdirs()) {
            LOGGER.error("Output folder for code generation does not exist and could not be created.");
            return;
        }

        // Important classes we use alot
        ClassName particleCN = ClassName.get("net.minestom.server.particle", "Particle");
        ClassName particleImplCN = ClassName.get("net.minestom.server.particle", "ParticleImpl");

        JsonObject particleObject = GSON.fromJson(new InputStreamReader(particlesFile), JsonObject.class);
        List<Map.Entry<String, JsonElement>> orderedParticleIdObjectEntries = particleObject.entrySet().stream()
                .sorted(Comparator.comparingInt(o -> o.getValue().getAsJsonObject().get("id").getAsInt())).toList();


        // Start code gen
        ClassName particlesCN = ClassName.get("net.minestom.server.particle", "Particles");
        TypeSpec.Builder particlesInterface = TypeSpec.interfaceBuilder(particlesCN)
                .addJavadoc("AUTOGENERATED by " + getClass().getSimpleName());

        for (Map.Entry<String, JsonElement> particleIdObjectEntry : orderedParticleIdObjectEntries) {
            final String key = particleIdObjectEntry.getKey();
            final JsonObject value = particleIdObjectEntry.getValue().getAsJsonObject();

            ClassName fieldCN = particleCN;
            if (value.get("hasData").getAsBoolean()) {
                // This particle has data, use the particle implementation class
                fieldCN = ClassName.get("net.minestom.server.particle", "Particle",
                        toPascalCase(key.replace("minecraft:", "")));
            }

            String cast = "";
            if (!fieldCN.equals(particleCN)) {
                // This is one of the unique particle classes with particle data, cast this
                cast = "(Particle." + fieldCN.simpleName() + ") ";
            }

            String fieldName = key.replace("minecraft:", "").toUpperCase();

            particlesInterface.addField(FieldSpec.builder(fieldCN, fieldName)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("$L$T.get($S)", cast, particleImplCN, key).build());
        }

        writeFiles(
                List.of(JavaFile.builder("net.minestom.server.particle", particlesInterface.build())
                        .indent("    ")
                        .skipJavaLangImports(true)
                        .build()),
                outputFolder);
    }

    private static String toPascalCase(@NotNull String input) {
        String camelCase = Pattern.compile("_([a-z])")
                .matcher(input)
                .replaceAll(m -> m.group(1).toUpperCase());
        return camelCase.substring(0, 1).toUpperCase() + camelCase.substring(1);
    }
}
