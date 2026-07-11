package net.minestom.codegen;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.FieldSpec;
import com.palantir.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.util.Map;
import java.util.regex.Pattern;

record ParticleGenerator(Codegen codegen) {
    private static final Pattern PASCAL_PATTERN = Pattern.compile("_([a-z])");

    void generate() {
        ClassName particleCN = ClassName.get("net.minestom.server.particle", "Particle");
        ClassName particleImplCN = ClassName.get("net.minestom.server.particle", "ParticleImpl");
        ClassName particlesCN = ClassName.get("net.minestom.server.particle", "Particles");

        TypeSpec.Builder particlesInterface = TypeSpec.interfaceBuilder(particlesCN)
                .addModifiers(Modifier.SEALED)
                .addPermittedSubclass(particleCN)
                .addJavadoc(codegen.constantsJavadoc(particleCN));

        for (Map.Entry<String, JsonElement> particleIdObjectEntry : codegen.orderedEntries("particle.json")) {
            final String key = particleIdObjectEntry.getKey();
            final JsonObject value = particleIdObjectEntry.getValue().getAsJsonObject();
            final String namespacedName = codegen.namespaceShort(key);

            final ClassName fieldCN = value.get("hasData").getAsBoolean()
                    ? ClassName.get("net.minestom.server.particle", "Particle", toPascalCase(namespacedName))
                    : particleCN;

            particlesInterface.addField(FieldSpec.builder(fieldCN, codegen.constantName(key))
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("$T.get($S)", particleImplCN, key)
                    .build());
        }

        codegen.write(codegen.javaFile("net.minestom.server.particle", particlesInterface.build()));
    }

    private static String toPascalCase(String input) {
        String camelCase = PASCAL_PATTERN
                .matcher(input)
                .replaceAll(match -> match.group(1).toUpperCase());
        return camelCase.substring(0, 1).toUpperCase() + camelCase.substring(1);
    }
}
