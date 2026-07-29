package net.minestom.codegen;

import com.google.gson.JsonObject;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.FieldSpec;
import com.palantir.javapoet.TypeName;
import com.palantir.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;

record ConstantsGenerator(Codegen codegen) {

    void generate() {
        ClassName implCN = ClassName.get("net.minestom.server", "MinecraftServer");
        ClassName minecraftConstantsCN = ClassName.get("net.minestom.server", "MinecraftConstants");
        JsonObject constants = codegen.objectResource("constants");

        TypeSpec.Builder constantsInterface = TypeSpec.interfaceBuilder(minecraftConstantsCN)
                .addModifiers(Modifier.SEALED)
                .addPermittedSubclass(implCN)
                .addJavadoc(codegen.constantsJavadoc(implCN));

        constantsInterface.addField(FieldSpec.builder(String.class, "VERSION_NAME")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S", constants.get("name").getAsString())
                .build()
        );
        constantsInterface.addField(FieldSpec.builder(TypeName.INT, "PROTOCOL_VERSION")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$L", constants.get("protocol").getAsInt())
                .build()
        );
        constantsInterface.addField(FieldSpec.builder(TypeName.INT, "DATA_VERSION")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$L", constants.get("world").getAsInt())
                .build()
        );
        addMajorMinorField(constantsInterface, "RESOURCE_PACK_VERSION", constants.get("resourcepack").getAsString());
        addMajorMinorField(constantsInterface, "DATA_PACK_VERSION", constants.get("datapack").getAsString());

        codegen.write(codegen.javaFile("net.minestom.server", constantsInterface.build()));
    }

    private static void addMajorMinorField(TypeSpec.Builder typeSpec, String name, String value) {
        String[] parts = value.split("\\.", -1);
        if (parts.length != 2) throw new IllegalArgumentException("Invalid version format for " + name + ": " + value);

        ClassName majorMinorClass = ClassName.get("net.minestom.server.utils", "MajorMinorVersion");
        typeSpec.addField(FieldSpec.builder(majorMinorClass, name)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("new $T($L, $L)", majorMinorClass, parts[0], parts[1])
                .build()
        );
    }
}
