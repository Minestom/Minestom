package net.minestom.testing.framework;

import com.google.common.reflect.ClassPath;
import net.minestom.server.extras.selfmodification.MinestomExtensionClassLoader;
import net.minestom.server.extras.selfmodification.MinestomRootClassLoader;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class RunMinestomTests {

    private Logger log = LoggerFactory.getLogger("MinestomTests environment");

    private static URL[] extractURLsFromClasspath() {
        String classpath = System.getProperty("java.class.path");
        String[] parts = classpath.split(";");
        URL[] urls = new URL[parts.length];
        for (int i = 0; i < urls.length; i++) {
            try {
                String part = parts[i];
                String protocol;
                if (part.contains("!")) {
                    protocol = "jar://";
                } else {
                    protocol = "file://";
                }
                urls[i] = new URL(protocol + part);
            } catch (MalformedURLException e) {
                throw new Error(e);
            }
        }
        return urls;
    }

    /**
     * Launches all tests tagged with @MinestomTest: searches all classes available in the system classloader, extracts all
     * methods with @BeforeEachMinestomTest, @MinestomTest, @AfterEachMinestomTest and runs the tests.
     *
     * For more information, see how JUnit annotation work.
     *
     * <ul>
     *     <li>
     *         <code>@BeforeEachMinestomTest</code> ~= <code>@BeforeEach</code>
     *     </li>
     *     <li>
     *          <code>@AfterEachMinestomTest</code> ~= <code>@AfterEach</code>
     *     </li>
     *     <li>
     *          <code>@MinestomTest</code> ~= <code>@Test</code>
     *     </li>
     * </ul>
     *
     * @return
     * @throws IOException
     */
    @TestFactory
    public Stream<DynamicContainer> launchMinestomTests() throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        var cp = ClassPath.from(ClassLoader.getSystemClassLoader());

        // TODO: might need to move loop body to method to be able to test it properly
        return cp.getAllClasses().parallelStream().map(clazz -> {
            try {
                MinestomRootClassLoader rootClassLoader = new MinestomRootClassLoader(ClassLoader.getSystemClassLoader());
                rootClassLoader.protectedPackages.add("org.junit.jupiter.");
                rootClassLoader.protectedPackages.add("io.netty.");
                rootClassLoader.protectedPackages.add("com.intellij.");
                rootClassLoader.protectedPackages.add("io.unimi.dsi.fastutil.");

                Class<?> detector = rootClassLoader.loadClass(TestDetector.class.getCanonicalName());
                Method detectMethod = detector.getDeclaredMethod("detect", ClassLoader.class, String.class);

                DynamicContainer container = (DynamicContainer) detectMethod.invoke(null, rootClassLoader, clazz.getName());

                if(container == null) {
                    return null;
                }

                return container;
            } catch (IllegalAccessException | InvocationTargetException | ClassNotFoundException | NoSuchMethodException e) {
                log.error("Failed to load class " + clazz.getName(), e);
                return null;
            }
        }).filter(Objects::nonNull);
    }

}
