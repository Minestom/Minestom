package net.minestom.codegen;

import com.google.gson.JsonObject;
import com.palantir.javapoet.*;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.Modifier;
import java.io.InputStreamReader;
import java.nio.file.Path;

public class RegistryGenerator implements MinestomCodeGenerator {
    private static final AnnotationSpec SUPPRESS_ANNOTATION = AnnotationSpec.builder(SuppressWarnings.class)
            .addMember("value", "$S", "all") // unused, SpellCheckingInspection, NullableProblems
            .build();

    private static final AnnotationSpec NONEXTENDABLE_ANNOTATION = AnnotationSpec.builder(ApiStatus.NonExtendable.class).build();

    private final Path outputFolder;

    public RegistryGenerator(Path outputFolder) {
        this.outputFolder = ensureDirectory(outputFolder);
    }

    @Override
    public Path outputFolder() {
        return outputFolder;
    }

    @Override
    public void generate(CodegenRegistry registry, CodegenValue value) {
        switch (value.type()) {
            case STATIC -> generateStatic(registry, value);
            case DYNAMIC -> generateDynamic(registry, value);
        }
        generateTags(registry, value);
    }

    protected void generate(InputStreamReader resourceFile, String packageName, String typeName, String loaderName, String keyName, String generatedName) {
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

    protected void generateKeys(InputStreamReader resourceFile, String packageName, String typeName, String generatedName, boolean publicKeys) {
        ClassName typeClass = ClassName.get(packageName, typeName);
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


    protected void generateTags(@Nullable InputStreamReader resourceFile, String packageName, String typeName, String generatedName) {
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

    protected void generateStatic(CodegenRegistry registry, CodegenValue value) {
        generateKeys(registry.resource(value.resource()), value.packageName(), value.typeName(), value.keysName(), true);
        generate(registry.resource(value.resource()), value.packageName(), value.typeName(), value.loaderName(), value.keysName(), value.generatedName());
    }

    protected void generateDynamic(CodegenRegistry registry, CodegenValue value) {
        generateKeys(registry.resource(value.resource()), value.packageName(), value.typeName(), value.generatedName(), false);
    }

    protected void generateTags(CodegenRegistry registry, CodegenValue value) {
        generateTags(registry.optionalResource(value.tagResource()), value.packageName(), value.typeName(), value.tagsName());
    }
}
