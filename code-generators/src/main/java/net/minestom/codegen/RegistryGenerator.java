package net.minestom.codegen;

import com.google.gson.JsonObject;
import com.palantir.javapoet.*;

import javax.lang.model.element.Modifier;
import java.util.function.Function;

record RegistryGenerator(Codegen codegen) {

    void generate(Generators.StaticRegistrySpec spec) {
        ClassName typeClass = ClassName.get(spec.packageName(), spec.typeName());
        ClassName loaderClass = ClassName.get(spec.packageName(), spec.loaderName());
        generateConstants(
                spec.resource(),
                typeClass,
                ClassName.get(spec.packageName(), spec.generatedName()),
                typeClass,
                namespace -> CodeBlock.of("$T.get($S)", loaderClass, codegen.namespaceShort(namespace))
        );
    }

    void generate(Generators.DynamicRegistrySpec spec) {
        ClassName typeClass = ClassName.bestGuess(spec.packageName() + "." + spec.typeName());
        ClassName registryKeyClass = ClassName.get("net.minestom.server.registry", "RegistryKey");
        generateConstants(
                spec.resource(),
                typeClass,
                ClassName.get(spec.packageName(), spec.generatedName()),
                ParameterizedTypeName.get(registryKeyClass, typeClass),
                namespace -> CodeBlock.of("$T.unsafeOf($S)", registryKeyClass, codegen.namespaceShort(namespace))
        );
    }

    private void generateConstants(String resource, ClassName permittedType, ClassName generatedClass,
                                   TypeName fieldType, Function<String, CodeBlock> initializer) {
        JsonObject json = codegen.objectResource(resource);
        TypeSpec.Builder constants = TypeSpec.interfaceBuilder(generatedClass)
                .addModifiers(Modifier.SEALED)
                .addPermittedSubclass(permittedType)
                .addAnnotation(codegen.suppressUnused())
                .addJavadoc(codegen.constantsJavadoc(permittedType));

        json.keySet().forEach(namespace -> constants.addField(
                FieldSpec.builder(fieldType, codegen.constantName(namespace))
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                        .initializer(initializer.apply(namespace))
                        .build()
        ));

        codegen.write(codegen.javaFile(generatedClass.packageName(), constants.build()));
    }
}
