package net.minestom.codegen;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.palantir.javapoet.*;

import javax.lang.model.element.Modifier;
import java.util.Map;
import java.util.Objects;

record GameRuleGenerator(Codegen codegen) {
    static final String PACKAGE = "net.minestom.server.instance.gamerule";
    static final ClassName GAME_RULE_CN = ClassName.get(PACKAGE, "GameRule");
    static final ClassName GAME_RULE_IMPL_CN = ClassName.get(PACKAGE, "GameRuleImpl");
    static final ClassName GAME_RULES_CN = ClassName.get(PACKAGE, "GameRules");

    public GameRuleGenerator {
        Objects.requireNonNull(codegen, "codegen cannot be null");
    }

    void generate() {
        JsonObject gameRules = codegen.objectResource("game_rule.json");

        // Start code gen
        TypeSpec.Builder gameRulesInterface = TypeSpec.interfaceBuilder(GAME_RULES_CN)
                .addModifiers(Modifier.SEALED)
                .addPermittedSubclass(GAME_RULE_CN)
                .addJavadoc(codegen.constantsJavadoc(GAME_RULE_CN));

        for (Map.Entry<String, JsonElement> particleIdObjectEntry : gameRules.entrySet()) {
            final String key = particleIdObjectEntry.getKey();
            final JsonObject value = particleIdObjectEntry.getValue().getAsJsonObject();

            final String type = value.get("type").getAsString();

            final ParameterizedTypeName fieldCN = switch (type) {
                case "boolean" -> ParameterizedTypeName.get(GAME_RULE_CN, ClassName.get(Boolean.class));
                case "integer" -> ParameterizedTypeName.get(GAME_RULE_CN, ClassName.get(Integer.class));
                default -> throw new IllegalArgumentException("Unknown type: " + type);
            };

            String fieldName = codegen.constantName(key);
            String namespacedName = codegen.namespaceShort(key);

            gameRulesInterface.addField(FieldSpec.builder(fieldCN, fieldName)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("$T.get($S)", GAME_RULE_IMPL_CN, namespacedName).build());
        }

        codegen.write(codegen.javaFile(PACKAGE, gameRulesInterface.build()));
    }
}
