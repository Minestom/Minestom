package net.minestom.codegen;

import com.google.gson.JsonObject;
import com.palantir.javapoet.*;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.Modifier;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public record RegistryGenerator(Path outputFolder) implements MinestomCodeGenerator {
    public RegistryGenerator {
        Objects.requireNonNull(outputFolder, "Output folder cannot be null");
        ensureDirectory(outputFolder);
    }

    public void generate(InputStream resourceFile, String packageName, String typeName, String loaderName, String keyName, String generatedName) {
        ClassName typeClass = ClassName.get(packageName, typeName);
        ClassName loaderClass = ClassName.get(packageName, loaderName);
        ClassName keyClass = ClassName.get(packageName, keyName);

        JsonObject json = GSON.fromJson(new InputStreamReader(resourceFile), JsonObject.class);
        ClassName generatedCN = ClassName.get(packageName, generatedName);
        // BlockConstants class
        TypeSpec.Builder blockConstantsClass = TypeSpec.interfaceBuilder(generatedCN)
                // Add @SuppressWarnings("unused")
                .addModifiers(Modifier.SEALED)
                .addPermittedSubclass(typeClass)
                .addAnnotation(AnnotationSpec.builder(SuppressWarnings.class).addMember("value", "$S", "unused").build())
                .addJavadoc(generateJavadoc(typeClass));

        // Use data
        json.keySet().forEach(namespace -> {
            final String constantName = toConstant(namespace);
            final String namespaceString = namespaceShort(namespace);
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

    public void generateKeys(InputStream resourceFile, String packageName, String typeName, String generatedName, boolean publicKeys) {
        ClassName typeClass = ClassName.bestGuess(packageName + "." + typeName); // Use bestGuess to handle nested class
        ClassName registryKeyClass = ClassName.get("net.minestom.server.registry", "RegistryKey");
        ClassName generatedCN = ClassName.get(packageName, generatedName);
        ParameterizedTypeName typedRegistryKeyClass = ParameterizedTypeName.get(registryKeyClass, typeClass);

        JsonObject json = GSON.fromJson(new InputStreamReader(resourceFile), JsonObject.class);
        // BlockConstants class
        TypeSpec.Builder blockConstantsClass = TypeSpec.interfaceBuilder(generatedCN)
                // Add @SuppressWarnings("unused")
                .addAnnotation(AnnotationSpec.builder(SuppressWarnings.class).addMember("value", "$S", "unused").build())
                .addJavadoc(generateJavadoc(typeClass));
        if (!publicKeys) blockConstantsClass.addModifiers(Modifier.SEALED).addPermittedSubclass(typeClass);
        else blockConstantsClass.addModifiers(Modifier.PUBLIC);

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


    public void generateTags(@Nullable InputStream resourceFile, String packageName, String typeName, String generatedName) {
        if (resourceFile == null) {
            System.out.printf("Warning: Failed to locate tags for %s%n", packageName);
            return;
        }

        final JsonObject json = GSON.fromJson(new InputStreamReader(resourceFile), JsonObject.class);

        ClassName typeClass = ClassName.get(packageName, typeName);
        ClassName keyClass = ClassName.get(Key.class);
        ClassName tagKeyClass = ClassName.get("net.minestom.server.registry", "TagKey");
        ParameterizedTypeName typedTagClass = ParameterizedTypeName.get(tagKeyClass, typeClass);

        TypeSpec.Builder registryTagInterface = TypeSpec.interfaceBuilder(generatedName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(SuppressWarnings.class).addMember("value", "$S", "unused").build())
                .addAnnotation(AnnotationSpec.builder(ApiStatus.NonExtendable.class).build())
                .addJavadoc("Code autogenerated, do not edit!"); //TODO add usage javadoc.

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

    public void generateRegistryKeys(Collection<Entry.Static> staticRegistries, Collection<Entry.Dynamic> dynamicRegistries, String packageName, String typeName, String generatedName) {
        TypeSpec.Builder registryKeyInterface = TypeSpec.interfaceBuilder(generatedName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(SuppressWarnings.class).addMember("value", "$S", "unused").build())
                .addAnnotation(AnnotationSpec.builder(ApiStatus.NonExtendable.class).build())
                .addJavadoc("Code autogenerated, do not edit!");
        ClassName registryKeyClass = ClassName.get(packageName, typeName);
        ClassName registryClassType = ClassName.get(packageName, "Registry");
        ClassName dynamicRegistryClassType = ClassName.get(packageName, "DynamicRegistry");

        // Add static registries
        for (var registry : staticRegistries) {
            ClassName registryClass = ClassName.get(registry.packageName(), registry.typeName());
            TypeName parametrizedRegistryClassType = registry.wildcardKey() ? WildcardTypeName.subtypeOf(registryClass) : registryClass;
            ParameterizedTypeName registryType =  ParameterizedTypeName.get(registryClassType, parametrizedRegistryClassType);
            ParameterizedTypeName typedRegistryKeyClass = ParameterizedTypeName.get(registryKeyClass, registryType);
            String namespace = registry.namespace();
            String constantName = toConstant(namespace);
            String shortNamespace = namespaceShort(namespace);
            registryKeyInterface.addField(FieldSpec.builder(typedRegistryKeyClass, constantName)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("$T.unsafeOf($S)", registryKeyClass, shortNamespace)
                    .addJavadoc("The static registry key for {@link $T}", registryClass)
                    .build());
        }
        // Add dynamic registries
        for (var registry : dynamicRegistries) {
            ClassName registryClass = ClassName.get(registry.packageName(), registry.typeName());
            ParameterizedTypeName registryType = ParameterizedTypeName.get(dynamicRegistryClassType, registryClass);
            ParameterizedTypeName typedRegistryKeyClass = ParameterizedTypeName.get(registryKeyClass, registryType);
            String namespace = registry.namespace();
            String constantName = toConstant(namespace);
            String shortNamespace = namespaceShort(namespace);
            registryKeyInterface.addField(FieldSpec.builder(typedRegistryKeyClass, constantName)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("$T.unsafeOf($S)", registryKeyClass, shortNamespace)
                    .addJavadoc("The dynamic registry key for {@link $T}", registryClass)
                    .build());
        }

        writeFiles(JavaFile.builder(packageName, registryKeyInterface.build())
                .indent("    ")
                .skipJavaLangImports(true)
                .build()
        );
    }

    @Override
    public void generate() {
        throw new UnsupportedOperationException("Use generate(InputStream, String, String, String, String) instead");
    }
}
