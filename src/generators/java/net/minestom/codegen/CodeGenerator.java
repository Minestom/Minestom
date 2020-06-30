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

    static String decapitalize(String text) {
        char first = text.charAt(0);
        return ""+Character.toLowerCase(first)+text.substring(1);
    }
}
