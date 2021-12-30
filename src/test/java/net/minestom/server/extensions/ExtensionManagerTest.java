package net.minestom.server.extensions;

import com.google.gson.JsonObject;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.extensions.descriptor.Dependency;
import net.minestom.server.extensions.descriptor.ExtensionDescriptor;
import net.minestom.server.util.TestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.params.provider.Arguments.of;

@ExtendWith(MockitoExtension.class)
public class ExtensionManagerTest {
    private @TempDir Path dataRoot;
    private EventNode<Event> globalNode = EventNode.all("global");
    private ExtensionManager extensionManager;

    @BeforeEach
    public void setup() {
        extensionManager = new ExtensionManager(
                TestUtil.IGNORING_EXTENSION_MANAGER,
                globalNode, ExtensionDiscoverer.AUTOSCAN);
        extensionManager.setExtensionDataRoot(dataRoot);
    }

    //
    // Load Ordering
    //

    @ParameterizedTest(name = "{0}")
    @MethodSource("validLoadOrderingProvider")
    public void testValidLoadOrdering(String ignoredName, List<ExtensionStub> stubs, List<String> expected) {
        // Init sample
        Map<String, ExtensionDescriptor> extensionsByName = parseStubs(stubs);

        // Verify
        List<String> ordered = extensionManager
                .computeLoadOrder(extensionsByName)
                .stream()
                .map(ExtensionDescriptor::name)
                .map(s -> s.substring(0, 1))
                .toList();

        assertEquals(expected, ordered);
    }

    private static Stream<Arguments> validLoadOrderingProvider() {
        return Stream.of(
                of("A, B", List.of(ext("A"), ext("B")), List.of("B", "A")),
                of("A > B, B", List.of(ext("A", "B"), ext("B")), List.of("B", "A")),
                of("A > C, B > C, C", List.of(ext("A", "C"), ext("B", "C"), ext("C")), List.of("C", "B", "A")),
                of("A > B?, B", List.of(ext("A", "B?"), ext("B")), List.of("B", "A")),
                of("A > B?, no B", List.of(ext("A", "B?")), List.of("A"))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidLoadOrderingProvider")
    public void testInvalidLoadOrdering(String ignoredName, List<ExtensionStub> stubs, String expectedError) {
        // Init sample
        Map<String, ExtensionDescriptor> extensionsByName = parseStubs(stubs);

        // Verify
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> extensionManager.computeLoadOrder(extensionsByName));

        assertEquals(expectedError, exception.getMessage());
    }

    private static Stream<Arguments> invalidLoadOrderingProvider() {
        return Stream.of(
                of("A > B, no B", List.of(ext("A", "B")), "Unknown extension: BB (dependency of AA)"),
                of("A > B, B > A", List.of(ext("A", "B"), ext("B", "A")), "Illegal circular extension dependency: AA -> BB -> AA")

        );
    }

    record ExtensionStub(String name, boolean optional, String... dependencies) { }

    private static ExtensionStub ext(String name, String... dependencies) {
        return new ExtensionStub(name, false, dependencies);
    }

    private static Map<String, ExtensionDescriptor> parseStubs(List<ExtensionStub> stubs) {
        Map<String, ExtensionDescriptor> extensionsByName = new HashMap<>();
        for (ExtensionStub stub : stubs) {
            List<Dependency> dependencies = new ArrayList<>();
            for (String dep : stub.dependencies) {
                String name = dep.replace("?", "");
                dependencies.add(new Dependency.ExtensionDependency(
                        name + name, null, dep.endsWith("?")));
            }
            ExtensionDescriptor descriptor = new ExtensionDescriptor(
                    // Single letter extension names are not valid, so we do A > AA
                    stub.name + stub.name, "1.0.0", List.of(), "entrypoint",
                    List.of(), dependencies, new JsonObject(), Paths.get("."),
                    new HierarchyClassLoader("Ext_" + stub.name, new URL[0])
            );
            extensionsByName.put(descriptor.name().toLowerCase(), descriptor);
        }
        return extensionsByName;
    }

    //
    // Create Extension Impl
    //

    @Test
    public void testCreateExtensionImplFromClassloader() throws Exception {
        //todo This should test loading a class from a HierarchyClassLoader,
        // however I am unsure how to do so. If it is included here in the test
        // module then it will be loaded from the test module classpath, which
        // is not what we want. I think that we need to have a compiled extension
        // class somewhere to use.
        assumeTrue(false);
    }

    @Test
    public void testCreateExtensionImpl() {
        ExtensionDescriptor descriptor = createExtensionDescriptor("Test", "net.minestom.server.extensions.mock.ValidExtensionMain");
        Extension extension = extensionManager.createExtensionImpl(descriptor);

        assertNotNull(extension);
        assertEquals(descriptor, extension.descriptor());
        assertNotNull(extension.eventNode());
        assertEquals("Test", extension.eventNode().getName());
        assertNotNull(extension.logger());
    }

    //
    //
    //


    private ExtensionDescriptor createExtensionDescriptor(String name, String mainClass) {
        return new ExtensionDescriptor(
                name, "1.0.0", List.of(), mainClass,
                List.of(), List.of(), new JsonObject(), dataRoot.resolve(name),
                new HierarchyClassLoader("Ext_" + name, new URL[0])
        );
    }

}
