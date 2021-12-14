package net.minestom.server.extensions.descriptor;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.of;

public class DependencyTest {

    @ParameterizedTest
    @MethodSource("dependencyTypesProvider")
    public void testDependencyTypes(String dependencyString, Class<?> expectedType) {
        JsonElement json = JsonParser.parseString(dependencyString);
        Dependency dependency = Dependency.fromJson(json);

        if (expectedType == null) {
            assertNull(dependency);
        } else {
            assertEquals(expectedType, dependency.getClass());
        }
    }

    @ParameterizedTest
    @MethodSource("optionalValuesProvider")
    public void testOptionalValues(String dependencyString, boolean expectedOptional) {
        JsonElement json = JsonParser.parseString(dependencyString);
        Dependency dependency = Dependency.fromJson(json);

        assertNotNull(dependency);
        assertEquals(expectedOptional, dependency.isOptional());
    }

    private static Stream<Arguments> dependencyTypesProvider() {
        return Stream.of(
                of("\"LuckPerms\"", ExtensionDependency.class),
                of("\"LuckPerms:5.0.0\"", ExtensionDependency.class),
                of("{\"id\"=\"LuckPerms\"}", ExtensionDependency.class),
                of("\"org.junit.jupiter:junit-jupiter-params:5.8.1\"", MavenDependency.class)
        );
    }

    private static Stream<Arguments> optionalValuesProvider() {
        return Stream.of(
                of("\"LuckPerms\"", false),
                of("{\"id\"=\"LuckPerms\"}", false),
                of("{\"id\"=\"LuckPerms\",\"optional\":false}", false),
                of("{\"id\"=\"LuckPerms\",\"optional\":true}", true)
        );
    }
}
