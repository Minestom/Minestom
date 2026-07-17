package net.minestom.codegen;

import com.google.gson.JsonObject;
import com.palantir.javapoet.*;
import org.jetbrains.annotations.ApiStatus;

import javax.lang.model.element.Modifier;
import java.util.function.Function;

record RegistryGenerator(Codegen codegen) {

    void generate(Generators.StaticRegistrySpec spec) {
        ClassName typeClass = ClassName.get(spec.packageName(), spec.typeName());
        ClassName loaderClass = ClassName.get(spec.packageName(), spec.loaderName());
        ClassName keysClass = ClassName.get(spec.packageName(), spec.typeName() + "Keys");
        generateKeys(spec.key(), typeClass, keysClass, true);
        generateConstants(
                spec.key(),
                typeClass,
                ClassName.get(spec.packageName(), spec.generatedName()),
                typeClass,
                namespace -> CodeBlock.of("$T.get($T.$L)", loaderClass, keysClass, codegen.constantName(namespace))
        );
        generateTags(spec.key(), typeClass);
    }

    void generate(Generators.DynamicRegistrySpec spec) {
        ClassName typeClass = ClassName.bestGuess(spec.packageName() + "." + spec.typeName());
        ClassName registryKeyClass = ClassName.get("net.minestom.server.registry", "RegistryKey");
        generateConstants(
                spec.key(),
                typeClass,
                ClassName.get(spec.packageName(), spec.generatedName()),
                ParameterizedTypeName.get(registryKeyClass, typeClass),
                namespace -> CodeBlock.of("$T.unsafeOf($S)", registryKeyClass, codegen.namespaceShort(namespace))
        );
        generateTags(spec.key(), typeClass);
    }

    void generateKeys(String key, ClassName typeClass, ClassName generatedClass, boolean publicKeys) {
        final ClassName registryKeyClass = ClassName.get("net.minestom.server.registry", "RegistryKey");
        final TypeSpec.Builder constants = constantsBuilder(typeClass, generatedClass, publicKeys);
        addFields(codegen.objectResource(key), constants,
                ParameterizedTypeName.get(registryKeyClass, typeClass),
                namespace -> CodeBlock.of("$T.unsafeOf($S)", registryKeyClass, codegen.namespaceShort(namespace)));
        codegen.write(codegen.javaFile(generatedClass.packageName(), constants.build()));
    }

    void generateTags(String key, ClassName typeClass) {
        final JsonObject tags = codegen.optionalObjectResource("tags/" + key);
        if (tags == null) return;

        final ClassName tagKeyClass = ClassName.get("net.minestom.server.registry", "TagKey");
        final ClassName generatedClass = ClassName.get(typeClass.packageName(), typeClass.simpleName() + "Tags");
        final TypeSpec.Builder constants = constantsBuilder(typeClass, generatedClass, true);
        addFields(tags, constants, ParameterizedTypeName.get(tagKeyClass, typeClass),
                namespace -> CodeBlock.of("$T.unsafeOf($S)", tagKeyClass, codegen.namespaceShort(namespace)));
        codegen.write(codegen.javaFile(generatedClass.packageName(), constants.build()));
    }

    private void generateConstants(String key, ClassName permittedType, ClassName generatedClass,
                                   TypeName fieldType, Function<String, CodeBlock> initializer) {
        JsonObject json = codegen.objectResource(key);
        TypeSpec.Builder constants = constantsBuilder(permittedType, generatedClass, false);
        addFields(json, constants, fieldType, initializer);
        codegen.write(codegen.javaFile(generatedClass.packageName(), constants.build()));
    }

    private TypeSpec.Builder constantsBuilder(ClassName permittedType, ClassName generatedClass, boolean publicConstants) {
        final TypeSpec.Builder constants = TypeSpec.interfaceBuilder(generatedClass)
                .addAnnotation(codegen.suppressUnused())
                .addJavadoc(codegen.constantsJavadoc(permittedType));
        if (publicConstants) {
            constants.addModifiers(Modifier.PUBLIC).addAnnotation(ApiStatus.NonExtendable.class);
        } else {
            constants.addModifiers(Modifier.SEALED).addPermittedSubclass(permittedType);
        }
        return constants;
    }

    private void addFields(JsonObject json, TypeSpec.Builder constants, TypeName fieldType,
                           Function<String, CodeBlock> initializer) {
        json.keySet().forEach(namespace -> constants.addField(
                FieldSpec.builder(fieldType, codegen.constantName(namespace))
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                        .initializer(initializer.apply(namespace))
                        .build()
        ));
    }
}
