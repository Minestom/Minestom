package net.minestom.codegen;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.palantir.javapoet.*;

import javax.lang.model.element.Modifier;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public record ParticleGenerator(Entry.Static entry,
                                Path outputFolder) implements MinestomCodeGenerator {
    public static final Pattern PASCAL_PATTERN = Pattern.compile("_([a-z])");

    public ParticleGenerator {
        Objects.requireNonNull(entry, "Particle entry cannot be null");
        Objects.requireNonNull(outputFolder, "Output folder cannot be null");
    }

    @Override
    public void generate() {
        ensureDirectory(outputFolder);

        // Important classes we use alot
        ClassName particleCN = ClassName.get(entry.packageName(), entry.typeName());
        ClassName particleImplCN = ClassName.get(entry.packageName(), entry.loaderName());

        JsonObject particleObject = GSON.fromJson(new InputStreamReader(entry.resource()), JsonObject.class);
        List<Map.Entry<String, JsonElement>> orderedParticleIdObjectEntries = particleObject.entrySet().stream()
                .sorted(Comparator.comparingInt(o -> o.getValue().getAsJsonObject().get("id").getAsInt())).toList();

        // Start code gen
        ClassName particlesCN = ClassName.get(entry.packageName(), entry.generatedName());
        ClassName particlesKeysCN = ClassName.get(entry.packageName(), entry.keysName());
        TypeSpec.Builder particlesInterface = TypeSpec.interfaceBuilder(particlesCN)
                .addModifiers(Modifier.SEALED)
                .addPermittedSubclass(particleCN)
                .addAnnotation(AnnotationSpec.builder(SuppressWarnings.class).addMember("value", "$S", "unused").build())
                .addJavadoc(generateJavadoc(particleCN));

        for (Map.Entry<String, JsonElement> particleIdObjectEntry : orderedParticleIdObjectEntries) {
            final String key = particleIdObjectEntry.getKey();
            final JsonObject value = particleIdObjectEntry.getValue().getAsJsonObject();

            final ClassName fieldCN;
            final CodeBlock cast;
            if (value.get("hasData").getAsBoolean()) {
                // This particle has data, use the particle implementation class
                fieldCN = particleCN.nestedClass(toPascalCase(namespaceShort(key)));
                cast = CodeBlock.of("($T) ", fieldCN);
            } else {
                fieldCN = particleCN;
                cast = CodeBlock.builder().build(); // Empty cast for particles without data
            }

            String fieldName = toConstant(key);

            particlesInterface.addField(FieldSpec.builder(fieldCN, fieldName)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("$L$T.get($T.$L)", cast, particleImplCN, particlesKeysCN, fieldName).build());
        }

        writeFiles(JavaFile.builder("net.minestom.server.particle", particlesInterface.build())
                .indent("    ")
                .skipJavaLangImports(true)
                .build());
    }

    private static String toPascalCase(String input) {
        String camelCase = PASCAL_PATTERN
                .matcher(input)
                .replaceAll(m -> m.group(1).toUpperCase());
        return camelCase.substring(0, 1).toUpperCase() + camelCase.substring(1);
    }
}
