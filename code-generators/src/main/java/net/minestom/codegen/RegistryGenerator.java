package net.minestom.codegen;

import com.google.gson.JsonObject;
import com.palantir.javapoet.*;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.Modifier;
import java.io.InputStreamReader;
import java.nio.file.Path;

public record RegistryGenerator(Path outputFolder) implements MinestomCodeGenerator {
    private static final AnnotationSpec SUPPRESS_ANNOTATION = AnnotationSpec.builder(SuppressWarnings.class)
            .addMember("value", "$S", "all") // unused, SpellCheckingInspection, NullableProblems
            .build();

    private static final AnnotationSpec NONEXTENDABLE_ANNOTATION = AnnotationSpec.builder(ApiStatus.NonExtendable.class).build();

    public RegistryGenerator {
        ensureDirectory(outputFolder);
    }

    public void generate(InputStreamReader resourceFile, String packageName, String typeName, String loaderName, String keyName, String generatedName) {
        ClassName typeClass = ClassName.get(packageName, typeName);
        ClassName loaderClass = ClassName.get(packageName, loaderName);
        ClassName keyClass = ClassName.get(packageName, keyName);
        JsonObject json = GSON.fromJson(resourceFile, JsonObject.class);
        ClassName generatedCN = ClassName.get(packageName, generatedName);
        // BlockConstants class
        TypeSpec.Builder blockConstantsClass = TypeSpec.interfaceBuilder(generatedCN)
                // Add @SuppressWarnings("unused")
                .addModifiers(Modifier.SEALED)
                .addPermittedSubclass(typeClass)
                .addAnnotation(SUPPRESS_ANNOTATION)
                .addJavadoc(generateJavadoc(typeClass));

        // Use data
        json.keySet().forEach(namespace -> {
            final String constantName = toConstant(namespace);
            blockConstantsClass.addField(
                    FieldSpec.builder(typeClass, constantName)
                            .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                            .initializer(
                                    // TypeClass.CONSTANT_NAME = LoaderClass.get(namespaceString)
                                    "$T.get($T.$L)",
                                    loaderClass,
                                    keyClass,
                                    constantName
                            )
                            .build()
            );
        });
        writeFiles(JavaFile.builder(packageName, blockConstantsClass.build())
                .indent("    ")
                .skipJavaLangImports(true)
                .build()
        );
    }

    public void generateKeys(InputStreamReader resourceFile, String packageName, String typeName, String generatedName, boolean publicKeys) {
        ClassName typeClass = ClassName.bestGuess(packageName + "." + typeName); // Use bestGuess to handle nested class
        ClassName registryKeyClass = ClassName.get("net.minestom.server.registry", "RegistryKey");
        ClassName generatedCN = ClassName.get(packageName, generatedName);
        ParameterizedTypeName typedRegistryKeyClass = ParameterizedTypeName.get(registryKeyClass, typeClass);

        JsonObject json = GSON.fromJson(resourceFile, JsonObject.class);
        // BlockConstants class
        TypeSpec.Builder blockConstantsClass = TypeSpec.interfaceBuilder(generatedCN)
                // Add @SuppressWarnings("unused")
                .addAnnotation(SUPPRESS_ANNOTATION)
                .addJavadoc(generateJavadoc(typeClass));
        if (!publicKeys) blockConstantsClass.addModifiers(Modifier.SEALED).addPermittedSubclass(typeClass);
        else blockConstantsClass.addModifiers(Modifier.PUBLIC).addAnnotation(NONEXTENDABLE_ANNOTATION);

        // Use data
        json.keySet().forEach(namespace -> {
            final String constantName = toConstant(namespace);
            final String namespaceString = namespaceShort(namespace);
            blockConstantsClass.addField(
                    FieldSpec.builder(typedRegistryKeyClass, constantName)
                            .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                            .initializer(
                                    // RegistryKey<Biome> CONSTANT_NAME = RegistryKey.unsafeOf(nameSpaceString)
                                    "$T.unsafeOf($S)",
                                    registryKeyClass,
                                    namespaceString
                            )
                            .build()
            );
        });

        // Write files
        writeFiles(JavaFile.builder(packageName, blockConstantsClass.build())
                .indent("    ")
                .skipJavaLangImports(true)
                .build()
        );
    }


    public void generateTags(@Nullable InputStreamReader resourceFile, String packageName, String typeName, String generatedName) {
        if (resourceFile == null) {
            System.out.printf("Warning: Failed to locate tags for %s.%s%n", packageName, typeName);
            return;
        }

        final JsonObject json = GSON.fromJson(resourceFile, JsonObject.class);

        ClassName typeClass = ClassName.get(packageName, typeName);
        ClassName keyClass = ClassName.get(Key.class);
        ClassName tagKeyClass = ClassName.get("net.minestom.server.registry", "TagKey");
        ParameterizedTypeName typedTagClass = ParameterizedTypeName.get(tagKeyClass, typeClass);

        TypeSpec.Builder registryTagInterface = TypeSpec.interfaceBuilder(generatedName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(SUPPRESS_ANNOTATION)
                .addAnnotation(NONEXTENDABLE_ANNOTATION)
                .addJavadoc(generateJavadoc(typeClass));

        json.keySet().forEach(namespace -> {
            final String constantName = toConstant(namespace);
            final String namespaceString = namespaceShort(namespace);
            registryTagInterface.addField(
                    FieldSpec.builder(typedTagClass, constantName)
                            .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                            .initializer(
                                    // TypeClass.STONE = TagKey.of("stone")
                                    "$T.unsafeOf(key($S))",
                                    tagKeyClass,
                                    namespaceString
                            )
                            //.addJavadoc("The tag key for $L", namespaceString)
                            .build()
            );
        });

        writeFiles(JavaFile.builder(packageName, registryTagInterface.build())
                .indent("    ")
                .skipJavaLangImports(true)
                .addStaticImport(keyClass, "key")
                .build()
        );
    }

    @Override
    public void generate(CodegenRegistry registry, CodegenValue value) {
        if (value.type() == CodegenValue.Type.STATIC) {
            generateKeys(registry.resource(value.resource()), value.packageName(), value.typeName(), value.keysName(), true);
            generate(registry.resource(value.resource()), value.packageName(), value.typeName(), value.loaderName(), value.keysName(), value.generatedName());
        } else if (value.type() == CodegenValue.Type.DYNAMIC) {
            generateKeys(registry.resource(value.resource()), value.packageName(), value.typeName(), value.generatedName(), false);
        }
        generateTags(registry.optionalResource(value.tagResource()), value.packageName(), value.typeName(), value.tagsName());
    }
}
