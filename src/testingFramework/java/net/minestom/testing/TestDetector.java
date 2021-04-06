package net.minestom.testing;

import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Made to be loaded in a different classloader for each test class
 */
public final class TestDetector {

    private static Logger log = LoggerFactory.getLogger("MinestomTests environment");

    private TestDetector() {}

    public static DynamicContainer detect(ClassLoader loader, String fullClassName) {
        var loadedClass = safeLoad(loader, fullClassName);
        if(loadedClass == null) {
            return null;
        }
        if(loadedClass.isAnnotation() || loadedClass.isInterface()) {
            return null;
        }

        MinestomTestCollection collection = loadedClass.getAnnotation(MinestomTestCollection.class);
        if(collection != null) {
            // TODO: handle collection annotation
        }

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
                    log.error("Failed to instantiate class "+fullClassName+".", e);
                    return null;
                }
            };
        } catch (NoClassDefFoundError | Exception e) {
            log.trace("Class {} has no default constructor (or could not access it), will not be able to instantiate it for tests.", fullClassName);
        }
        if(methods == null) {
            return null;
        }


        List<Method> testMethods = new LinkedList<>();
        List<Method> beforeMethods = new LinkedList<>();
        List<Method> afterMethods = new LinkedList<>();
        for(var m : methods) {
            MinestomTest testAnnotation = m.getAnnotation(MinestomTest.class);
            BeforeEachMinestomTest beforeAnnotation = m.getAnnotation(BeforeEachMinestomTest.class);
            AfterEachMinestomTest afterAnnotation = m.getAnnotation(AfterEachMinestomTest.class);
            if(testAnnotation == null && beforeAnnotation == null && afterAnnotation == null)
                continue;

            if(testAnnotation != null) {
                if(checkValidity(loadedClass, constructor, m, testAnnotation, beforeAnnotation, testAnnotation, afterAnnotation)) {
                    testMethods.add(m);
                }
            }
            if(beforeAnnotation != null) {
                if(checkValidity(loadedClass, constructor, m, beforeAnnotation, beforeAnnotation, testAnnotation, afterAnnotation)) {
                    beforeMethods.add(m);
                }
            }
            if(afterAnnotation != null) {
                if(checkValidity(loadedClass, constructor, m, afterAnnotation, beforeAnnotation, testAnnotation, afterAnnotation)) {
                    afterMethods.add(m);
                }
            }
        }

        if(constructor == null || testMethods.isEmpty()) {
            return null;
        }

        boolean collectionIndependance = false;
        String collectionName = loadedClass.getSimpleName();
        if(collection != null) {
            String newCollectionName = collection.value();
            if(!newCollectionName.isBlank()) {
                collectionName = newCollectionName;
            }

            collectionIndependance = collection.independent();
        }

        if(collectionIndependance) {
            collectionName += " (Independent tests)";
        } else {
            collectionName += " (Dependent tests)";
        }

        MinestomSingleTest.TestRunnable beforeAction = beforeMethods.stream()
                .map(TestDetector::wrap)
                .reduce(MinestomSingleTest.TestRunnable.IDENTITY, MinestomSingleTest.TestRunnable::compose);
        MinestomSingleTest.TestRunnable afterAction = afterMethods.stream()
                .map(TestDetector::wrap)
                .reduce(MinestomSingleTest.TestRunnable.IDENTITY, MinestomSingleTest.TestRunnable::compose);

        // TODO: handle test independance
        Supplier<TestEnvironment> envSupplier;
        if(collectionIndependance) {
            envSupplier = () -> new TestEnvironment(1);
        } else {
            envSupplier = new TestEnvironment.Lazy(testMethods.size());
        }
        Supplier<Object> finalConstructor = constructor;
        Stream<DynamicTest> dynamicTests = testMethods.stream()
                .map(m -> new MinestomSingleTest(m.getName(), envSupplier, finalConstructor, beforeAction, wrap(m), afterAction).toDynamicTest());

        return DynamicContainer.dynamicContainer(collectionName, dynamicTests);
    }

    private static boolean checkValidity(Class<?> clazz, Supplier<Object> constructor, Method methodToCheck, Annotation currentAnnotationBeingTested, Annotation beforeAnnotation, Annotation testAnnotation, Annotation afterAnnotation) {
        boolean valid = true;
        if(constructor == null) {
            valid = false;
            log.error("Method {}.{} is annotated with @{} but class {} has no default public constructor.", clazz.getName(), methodToCheck.toGenericString(), MinestomTest.class.getSimpleName(), clazz.getName());
        }
        if((methodToCheck.getModifiers() & Modifier.STATIC) == Modifier.STATIC) {
            valid = false;
            log.error("Method {}.{} is static. @{} is allowed only on non-static methods.",
                    clazz.getName(),
                    methodToCheck.toGenericString(),
                    currentAnnotationBeingTested.annotationType().getSimpleName());
        }
        if((methodToCheck.getModifiers() & Modifier.PUBLIC) != Modifier.PUBLIC) {
            valid = false;
            log.error("Method {}.{} is not public. @{} is allowed only on public methods.",
                    clazz.getName(),
                    methodToCheck.toGenericString(),
                    currentAnnotationBeingTested.annotationType().getSimpleName());
        }
        Parameter[] parameters = methodToCheck.getParameters();
        if(parameters.length != 1) {
            valid = false;
            log.error("Method {}.{} does not have exactly one argument while tagged with @{}.",
                    clazz.getName(),
                    methodToCheck.toGenericString(),
                    currentAnnotationBeingTested.annotationType().getSimpleName());
        } else {
            boolean hasTestEnvArgument = parameters[0].getType().isAssignableFrom(TestEnvironment.class);
            if(!hasTestEnvArgument) {
                valid = false;
                log.error("Method {}.{} is tagged with @{} but cannot accept a @{} argument.",
                        clazz.getName(),
                        methodToCheck.toGenericString(),
                        currentAnnotationBeingTested.annotationType().getSimpleName(),
                        TestEnvironment.class.getCanonicalName());
            }
        }
        if(beforeAnnotation != currentAnnotationBeingTested && beforeAnnotation != null) {
            valid = false;
            log.error("Method {}.{} is tagged with @{} which is not allowed on methods tagged with @{}.",
                    clazz.getName(),
                    methodToCheck.toGenericString(),
                    beforeAnnotation.annotationType().getSimpleName(),
                    currentAnnotationBeingTested.annotationType().getSimpleName());
        }
        if(afterAnnotation != currentAnnotationBeingTested && afterAnnotation != null) {
            valid = false;
            log.error("Method {}.{} is tagged with @{} which is not allowed on methods tagged with @{}.",
                    clazz.getName(),
                    methodToCheck.toGenericString(),
                    afterAnnotation.annotationType().getSimpleName(),
                    currentAnnotationBeingTested.annotationType().getSimpleName());
        }
        return valid;
    }

    private static MinestomSingleTest.TestRunnable wrap(Method m) {
        return m::invoke;
    }

    private static Class<?> safeLoad(ClassLoader classLoader, String name) {
        try {
            return classLoader.loadClass(name);
        } catch (Throwable t) {
            log.error("Could not load class {}", name);
            return null;
        }
    }
}
