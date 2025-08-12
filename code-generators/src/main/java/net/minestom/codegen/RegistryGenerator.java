package net.minestom.codegen;

import com.google.gson.JsonObject;
import com.palantir.javapoet.*;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.Modifier;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Objects;

public record RegistryGenerator(Path outputFolder) implements MinestomCodeGenerator {
    public RegistryGenerator {
        Objects.requireNonNull(outputFolder, "Output folder cannot be null");
    }

    public void generate(@Nullable InputStream resourceFile, String packageName, String typeName, String loaderName, String generatedName) {
        Objects.requireNonNull(resourceFile, "Nothing to generate, resourceFile is null");
        ensureDirectory(outputFolder);

        ClassName typeClass = ClassName.get(packageName, typeName);
        ClassName loaderClass = ClassName.get(packageName, loaderName);
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
                                    "$T.get($S)",
                                    loaderClass,
                                    namespaceString
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

    public void generateKeys(@Nullable InputStream resourceFile, String packageName, String typeName) {
        Objects.requireNonNull(resourceFile, "Nothing to generate, resourceFile is null");
        ensureDirectory(outputFolder);

        ClassName typeClass = ClassName.bestGuess(packageName + "." + typeName); // Use bestGuess to handle nested class
        ClassName registryKeyClass = ClassName.get("net.minestom.server.registry", "RegistryKey");
        ParameterizedTypeName typedRegistryKeyClass = ParameterizedTypeName.get(registryKeyClass, typeClass);

        JsonObject json = GSON.fromJson(new InputStreamReader(resourceFile), JsonObject.class);
        ClassName generatedCN = ClassName.get(packageName, typeName + "s");
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

    @Override
    public void generate() {
        throw new UnsupportedOperationException("Use generate(InputStream, String, String, String, String) instead");
    }
}
