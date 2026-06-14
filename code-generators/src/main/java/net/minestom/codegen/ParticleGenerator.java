package net.minestom.codegen;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.FieldSpec;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public record ParticleGenerator(InputStream particleFile,
                                Path outputFolder) implements MinestomCodeGenerator {
    public static final Pattern PASCAL_PATTERN = Pattern.compile("_([a-z])");

    public ParticleGenerator {
        Objects.requireNonNull(particleFile, "Particle file cannot be null");
        Objects.requireNonNull(outputFolder, "Output folder cannot be null");
    }

    @Override
    public void generate() {
        ensureDirectory(outputFolder);

        // Important classes we use alot
        ClassName particleCN = ClassName.get("net.minestom.server.particle", "Particle");
        ClassName particleImplCN = ClassName.get("net.minestom.server.particle", "ParticleImpl");

        JsonObject particleObject = GSON.fromJson(new InputStreamReader(particleFile), JsonObject.class);
        List<Map.Entry<String, JsonElement>> orderedParticleIdObjectEntries = particleObject.entrySet().stream()
                .sorted(Comparator.comparingInt(o -> o.getValue().getAsJsonObject().get("id").getAsInt())).toList();

        // Start code gen
        ClassName particlesCN = ClassName.get("net.minestom.server.particle", "Particles");
        TypeSpec.Builder particlesInterface = TypeSpec.interfaceBuilder(particlesCN)
                .addModifiers(Modifier.SEALED)
                .addPermittedSubclass(particleCN)
                .addJavadoc(generateJavadoc(particleCN));

        for (Map.Entry<String, JsonElement> particleIdObjectEntry : orderedParticleIdObjectEntries) {
            final String key = particleIdObjectEntry.getKey();
            final JsonObject value = particleIdObjectEntry.getValue().getAsJsonObject();
            final String namespacedName = namespaceShort(key);

            final ClassName fieldCN = value.get("hasData").getAsBoolean()
                    ? getDataImplementation(namespacedName)
                    : particleCN;

            particlesInterface.addField(FieldSpec.builder(fieldCN, toConstant(key))
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("$T.get($S)", particleImplCN, key)
                    .build());
        }

        writeFiles(JavaFile.builder("net.minestom.server.particle", particlesInterface.build())
                .indent("    ")
                .skipJavaLangImports(true)
                .build());
    }

    private static ClassName getDataImplementation(String namespacedName) {
        return ClassName.get("net.minestom.server.particle", "Particle", toPascalCase(namespacedName));
    }

    private static String toPascalCase(String input) {
        String camelCase = PASCAL_PATTERN
                .matcher(input)
                .replaceAll(m -> m.group(1).toUpperCase());
        return camelCase.substring(0, 1).toUpperCase() + camelCase.substring(1);
    }
}
