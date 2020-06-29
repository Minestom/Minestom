package net.minestom.codegen;

import java.io.IOException;

/**
 * Interface representing a code generator
 */
public interface CodeGenerator {

    /**
     * Generates the Java code
     * @return
     */
    String generate() throws IOException;

}
