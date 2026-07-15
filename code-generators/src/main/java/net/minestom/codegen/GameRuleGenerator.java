package net.minestom.codegen;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.FieldSpec;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeSpec;
import org.jetbrains.annotations.ApiStatus;

import javax.lang.model.element.Modifier;
import java.util.Map;
import java.util.Objects;

record GameRuleGenerator(Codegen codegen) {
    static final String PACKAGE = "net.minestom.server.instance.gamerule";
    static final ClassName GAME_RULE_CN = ClassName.get(PACKAGE, "GameRule");
    static final ClassName GAME_RULE_IMPL_CN = ClassName.get(PACKAGE, "GameRuleImpl");
    static final ClassName GAME_RULES_CN = ClassName.get(PACKAGE, "GameRules");
    static final ClassName GAME_RULE_KEY_CN = ClassName.get(PACKAGE, "GameRuleKey");
    static final ClassName GAME_RULE_KEYS_CN = ClassName.get(PACKAGE, "GameRuleKeys");

    public GameRuleGenerator {
        Objects.requireNonNull(codegen, "codegen cannot be null");
    }

    void generate() {
        JsonObject gameRules = codegen.objectResource("game_rule");

        // Start code gen
        TypeSpec.Builder gameRulesInterface = TypeSpec.interfaceBuilder(GAME_RULES_CN)
                .addModifiers(Modifier.SEALED)
                .addPermittedSubclass(GAME_RULE_CN)
                .addJavadoc(codegen.constantsJavadoc(GAME_RULE_CN));
        TypeSpec.Builder gameRuleKeyInterface = TypeSpec.interfaceBuilder(GAME_RULE_KEY_CN)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(ApiStatus.NonExtendable.class)
                .addAnnotation(codegen.suppressUnused())
                .addJavadoc(codegen.constantsJavadoc(GAME_RULE_CN));
        ClassName registryKeyClass = ClassName.get("net.minestom.server.registry", "RegistryKey");

        for (Map.Entry<String, JsonElement> particleIdObjectEntry : gameRules.entrySet()) {
            final String key = particleIdObjectEntry.getKey();
            final JsonObject value = particleIdObjectEntry.getValue().getAsJsonObject();

            final String type = value.get("type").getAsString();

            final ParameterizedTypeName fieldCN = switch (type) {
                case "boolean" -> ParameterizedTypeName.get(GAME_RULE_CN, ClassName.get(Boolean.class));
                case "integer" -> ParameterizedTypeName.get(GAME_RULE_CN, ClassName.get(Integer.class));
                default -> throw new IllegalArgumentException("Unknown type: " + type);
            };
            final ParameterizedTypeName registryKeyType = ParameterizedTypeName.get(registryKeyClass, fieldCN);

            String fieldName = codegen.constantName(key);
            String namespacedName = codegen.namespaceShort(key);

            gameRulesInterface.addField(FieldSpec.builder(fieldCN, fieldName)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("$T.get($T.$L)", GAME_RULE_IMPL_CN, GAME_RULE_KEY_CN, fieldName).build());
            gameRuleKeyInterface.addField(FieldSpec.builder(registryKeyType, fieldName)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("$T.unsafeOf($S)", registryKeyClass, namespacedName).build());
        }

        codegen.write(codegen.javaFile(PACKAGE, gameRulesInterface.build()),
                codegen.javaFile(PACKAGE, gameRuleKeyInterface.build()));
        final RegistryGenerator registryGenerator = new RegistryGenerator(codegen);
        registryGenerator.generateCompatibilityAlias(GAME_RULE_KEY_CN, GAME_RULE_KEYS_CN);
        registryGenerator.generateTags("game_rule", GAME_RULE_CN);
    }
}
