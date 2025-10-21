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
import java.util.regex.Pattern;

public final class ParticleGenerator extends RegistryGenerator {
    public static final Pattern PASCAL_PATTERN = Pattern.compile("_([a-z])");

    public ParticleGenerator(Path outputFolder) {
        super(outputFolder);
    }

    @Override
    public void generate(CodegenRegistry registry, CodegenValue value) {
        generateStatic(registry, value); // Generate this as static
        generateTags(registry, value); // generate tags if present
    }

    @Override
    public void generate(InputStreamReader resourceFile, String packageName, String typeName, String loaderName, String keyName, String generatedName) {
        // Important classes we use alot
        ClassName particleCN = ClassName.get(packageName, typeName);
        ClassName particleImplCN = ClassName.get(packageName, loaderName);
        ClassName particleKeysCN = ClassName.get(packageName, keyName);

        JsonObject particleObject = GSON.fromJson(resourceFile, JsonObject.class);
        List<Map.Entry<String, JsonElement>> orderedParticleIdObjectEntries = particleObject.entrySet().stream()
                .sorted(Comparator.comparingInt(o -> o.getValue().getAsJsonObject().get("id").getAsInt())).toList();

        // Start code gen
        ClassName particlesCN = ClassName.get(packageName, generatedName);
        TypeSpec.Builder particlesInterface = TypeSpec.interfaceBuilder(particlesCN)
                .addModifiers(Modifier.SEALED)
                .addPermittedSubclass(particleCN)
                .addJavadoc(generateJavadoc(particleCN));

        for (Map.Entry<String, JsonElement> particleIdObjectEntry : orderedParticleIdObjectEntries) {
            final String key = particleIdObjectEntry.getKey();
            final JsonObject object = particleIdObjectEntry.getValue().getAsJsonObject();
            final String namespacedName = namespaceShort(key);

            final ClassName fieldCN;
            final CodeBlock cast;
            if (object.get("hasData").getAsBoolean()) {
                // This particle has data, use the particle implementation class
                fieldCN = particleCN.nestedClass(toPascalCase(namespacedName));
                cast = CodeBlock.of("($T) ", fieldCN);
            } else {
                fieldCN = particleCN;
                cast = CodeBlock.builder().build(); // Empty cast for particles without data
            }

            String fieldName = toConstant(key);

            particlesInterface.addField(FieldSpec.builder(fieldCN, fieldName)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("$L$T.get($T.$L)", cast, particleImplCN, particleKeysCN, fieldName).build());
        }

        writeFiles(JavaFile.builder(packageName, particlesInterface.build())
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
