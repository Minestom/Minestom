package net.minestom.codegen;

import com.palantir.javapoet.*;

import javax.lang.model.element.Modifier;
import java.nio.file.Path;
import java.util.Comparator;

public record BuiltInRegistryGenerator(Path outputFolder) implements MinestomCodeGenerator {
    public BuiltInRegistryGenerator {
        ensureDirectory(outputFolder);
    }

    @Override
    public void generate(CodegenRegistry registry, CodegenValue value) {
        final ClassName implCN = ClassName.get(value.packageName(), value.typeName());

        ClassName registryConstants = ClassName.get(value.packageName(), value.generatedName());
        ClassName registryKeyClass = ClassName.get("net.minestom.server.registry", "RegistryKey");
        ClassName staticRegistryClass = ClassName.get("net.minestom.server.registry", "Registry");
        ClassName dynamicRegistryClass = ClassName.get("net.minestom.server.registry", "DynamicRegistry");

        TypeSpec.Builder constantsInterface = TypeSpec.interfaceBuilder(registryConstants)
                .addAnnotation(NONEXTENDABLE_ANNOTATION).addAnnotation(SUPPRESS_ANNOTATION)
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc(generateJavadoc(implCN));

        registry.registry().values().stream()
                .filter(it -> it.registryType() == CodegenValue.Type.DYNAMIC || it.registryType() == CodegenValue.Type.STATIC)
                .sorted(Comparator.comparing(CodegenValue::namespace))// Make the ordering somewhat stable.
                .sorted(Comparator.comparing(CodegenValue::registryType))
                .forEachOrdered(entry -> {
                    CodegenValue.Type type = entry.registryType();

                    ClassName entryClass = ClassName.get(entry.packageName(), entry.typeName());
                    ClassName registryClass = switch (type) {
                        case STATIC -> staticRegistryClass;
                        case DYNAMIC -> dynamicRegistryClass;
                        default -> throw new IllegalStateException("Unexpected value: " + type);
                    };
                    ParameterizedTypeName typedRegistryClass = ParameterizedTypeName.get(registryClass, entryClass);
                    ParameterizedTypeName typedRegistryKeyClass = ParameterizedTypeName.get(registryKeyClass, typedRegistryClass);

                    String namespace = entry.namespace();
                    String constantName = toConstant(namespace);
                    String shortNamespace = namespaceShort(namespace);

                    constantsInterface.addField(FieldSpec.builder(typedRegistryKeyClass, constantName)
                            .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                            .initializer("$T.unsafeOf($S)", registryKeyClass, shortNamespace)
                            .addJavadoc("The %s registry key for {@link $T}".formatted(type.name().toLowerCase()), entryClass)
                            .build());
                });

        // Write files to outputFolder
        writeFiles(JavaFile.builder(value.packageName(), constantsInterface.build())
                .indent("    ")
                .skipJavaLangImports(true)
                .build()
        );
    }
}
