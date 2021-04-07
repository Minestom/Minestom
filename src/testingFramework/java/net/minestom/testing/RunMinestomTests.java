package net.minestom.testing;

import com.google.common.reflect.ClassPath;
import net.minestom.server.extras.selfmodification.MinestomRootClassLoader;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.TestFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.function.Predicate;
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

        // classloader used to speed up look up by loading a class and testing whether it has methods annotated with @MinestomTest
        MinestomRootClassLoader checkingClassloader = new MinestomRootClassLoader(ClassLoader.getSystemClassLoader());
        checkingClassloader.protectedPackages.add("org.junit.jupiter.");
        checkingClassloader.protectedPackages.add("io.netty.");
        checkingClassloader.protectedPackages.add("com.intellij.");
        checkingClassloader.protectedPackages.add("io.unimi.dsi.fastutil.");

        Class<? extends Annotation> testAnnotation = (Class<? extends Annotation>) checkingClassloader.loadClass(MinestomTest.class.getCanonicalName());

        Predicate<ClassPath.ClassInfo> preliminaryTest = (clazz) -> {
            try {
                Class<?> loadedClass = checkingClassloader.loadClass(clazz.getName());
                Method[] methods = loadedClass.getDeclaredMethods();
                for (Method m : methods) {
                    if(m.getAnnotation(testAnnotation) != null)
                        return true;
                }
            } catch (Throwable e) {
                // nop
            }
            return false;
        };

        // TODO: might need to move loop body to method to be able to test it properly
        return cp.getAllClasses().stream().map(clazz -> {
            try {
                if(!preliminaryTest.test(clazz)) {
                    return null;
                }

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
