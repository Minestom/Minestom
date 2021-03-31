package net.minestom.testing.framework;

import com.google.common.reflect.ClassPath;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class RunMinestomTests {

    private Logger log = LoggerFactory.getLogger("MinestomTests environment");

    @TestFactory
    public Collection<DynamicTest> launchMinestomTests() throws IOException {
        List<DynamicTest> allTests = new LinkedList<>();
        var cp = ClassPath.from(ClassLoader.getSystemClassLoader());
        for(var clazz : cp.getTopLevelClasses()) {
            var loadedClass = safeLoad(clazz);
            if(loadedClass == null)
                continue;
            if(loadedClass.isAnnotation() || loadedClass.isInterface())
                continue;
            Method[] methods = null;
            Supplier<Object> constructor = null;
            try {
                methods = loadedClass.getMethods();
                var constructorRef = loadedClass.getConstructor();
                constructor = () -> {
                    try {
                        return constructorRef.newInstance();
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                        log.error("Failed to instantiate class "+clazz.getName()+".", e);
                        return null;
                    }
                };
            } catch (NoClassDefFoundError | Exception e) {
                log.trace("Class {} has no default constructor (or could not access it), will not be able to instantiate it for tests.", clazz.getName());
            }
            if(methods == null)
                continue;
            for(var m : methods) {
                MinestomTest annotation = m.getAnnotation(MinestomTest.class);
                if(annotation == null)
                    continue;

                boolean valid = true;
                if(constructor == null) {
                    valid = false;
                    log.error("Method {}.{} is annotated with @{} but class {} has no default public constructor.", clazz.getName(), m.toGenericString(), MinestomTest.class.getSimpleName(), clazz.getName());
                }
                if((m.getModifiers() & Modifier.STATIC) == Modifier.STATIC) {
                    valid = false;
                    log.error("Method {}.{} is static. @{} is allowed only on non-static methods.", clazz.getName(), m.toGenericString(), MinestomTest.class.getSimpleName());
                }
                if((m.getModifiers() & Modifier.PUBLIC) != Modifier.PUBLIC) {
                    valid = false;
                    log.error("Method {}.{} is not public. @{} is allowed only on public methods.", clazz.getName(), m.toGenericString(), MinestomTest.class.getSimpleName());
                }

                if(!valid)
                    continue;

                allTests.add(createTest(constructor, m));
            }
        }
        return allTests;
    }

    private Class<?> safeLoad(ClassPath.ClassInfo info) {
        try {
            return info.load();
        } catch (Throwable t) {
            log.error("Could not load class {}", info.getName());
            return null;
        }
    }

    private DynamicTest createTest(Supplier<Object> constructor, Method toCall) {
        return DynamicTest.dynamicTest(toCall.getName(), () -> {
            Object instance = constructor.get();
            // TODO: beforeeach
            if(instance != null) {
                // TODO: argument for testing features
                toCall.invoke(instance, new Object[]{null});
            }
            // TODO: aftereach
        });
    }
}
