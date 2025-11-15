package net.minestom.codegen;

import java.nio.file.Path;

public final class Generators {

    static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: <target folder>");
            return;
        }
        Path outputFolder = Path.of(args[0]);

        CodegenRegistry registry = CodegenRegistries.registry();
        for (CodegenValue value : registry) {
            value.generator(outputFolder).generate(registry, value);
        }

        System.out.println("Finished generating code");
    }
}
