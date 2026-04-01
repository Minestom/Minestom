package net.minestom.codegen;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.palantir.javapoet.*;

import javax.lang.model.element.Modifier;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

public record GameRuleGenerator(InputStream gameRulesFile,
                                Path outputFolder) implements MinestomCodeGenerator {
    static final String PACKAGE = "net.minestom.server.instance.gamerule";
    static final ClassName GAME_RULE_CN = ClassName.get(PACKAGE, "GameRule");
    static final ClassName GAME_RULE_IMPL_CN = ClassName.get(PACKAGE, "GameRuleImpl");
    static final ClassName GAME_RULES_CN = ClassName.get(PACKAGE, "GameRules");

    public GameRuleGenerator {
        Objects.requireNonNull(gameRulesFile, "Gamerules file cannot be null");
        Objects.requireNonNull(outputFolder, "Output folder cannot be null");
    }

    @Override
    public void generate() {
        ensureDirectory(outputFolder);

        JsonObject gameRules = GSON.fromJson(new InputStreamReader(gameRulesFile), JsonObject.class);

        // Start code gen
        TypeSpec.Builder gameRulesInterface = TypeSpec.interfaceBuilder(GAME_RULES_CN)
                .addModifiers(Modifier.SEALED)
                .addPermittedSubclass(GAME_RULE_CN)
                .addJavadoc(generateJavadoc(GAME_RULE_CN));

        for (Map.Entry<String, JsonElement> particleIdObjectEntry : gameRules.entrySet()) {
            final String key = particleIdObjectEntry.getKey();
            final JsonObject value = particleIdObjectEntry.getValue().getAsJsonObject();

            final String type = value.get("type").getAsString();

            final ParameterizedTypeName fieldCN = switch (type) {
                case "boolean" -> ParameterizedTypeName.get(GAME_RULE_CN, ClassName.get(Boolean.class));
                case "integer" -> ParameterizedTypeName.get(GAME_RULE_CN, ClassName.get(Integer.class));
                default -> throw new IllegalArgumentException("Unknown type: " + type);
            };

            String fieldName = toConstant(key);
            String namespacedName = namespaceShort(key);

            gameRulesInterface.addField(FieldSpec.builder(fieldCN, fieldName)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("$T.get($S)", GAME_RULE_IMPL_CN, namespacedName).build());
        }

        writeFiles(JavaFile.builder(PACKAGE, gameRulesInterface.build())
                .indent("    ")
                .skipJavaLangImports(true)
                .build());
    }
}
