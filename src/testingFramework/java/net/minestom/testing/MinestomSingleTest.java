package net.minestom.testing;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.DynamicTest;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;

public class MinestomSingleTest {

    @FunctionalInterface
    public interface TestRunnable {
        TestRunnable IDENTITY = (_1,_2) -> {};

        void run(Object testInstance, TestEnvironment args) throws Throwable;

        default TestRunnable compose(TestRunnable next) {
            return (obj, args) -> {
                this.run(obj, args);
                next.run(obj, args);
            };
        }
    }

    @NotNull
    private final String name;
    @NotNull
    private final Supplier<TestEnvironment> environment;
    private final Supplier<Object> constructor;
    @Nullable
    private final TestRunnable before;
    @NotNull
    private final TestRunnable test;
    @Nullable
    private final TestRunnable after;

    // test instance
    private Object instance;

    public MinestomSingleTest(@NotNull String name,
                              @NotNull Supplier<TestEnvironment> environment,
                              @NotNull Supplier<Object> constructor,
                              @Nullable TestRunnable before,
                              @NotNull TestRunnable test,
                              @Nullable TestRunnable after
                              ) {
        this.name = name;
        this.environment = environment;
        this.constructor = constructor;
        this.before = before;
        this.test = test;
        this.after = after;
    }

    public void before(TestEnvironment env) throws Throwable {
        instance = constructor.get();
        if(before != null) {
            before.run(instance, env);
        }
    }

    public void runTest(TestEnvironment env) throws Throwable {
        test.run(instance, env);
    }

    public void after(TestEnvironment env) throws Throwable {
        if(after != null) {
            after.run(instance, env);
        }
        instance = null;
    }

    public DynamicTest toDynamicTest() {
        return DynamicTest.dynamicTest(name, () -> {
            try(TestEnvironment env = environment.get()) {
                try {
                    try {
                        before(env);
                    } catch (InvocationTargetException e) {
                        throw e.getCause();
                    }
                    try {
                        runTest(env);
                    } catch (InvocationTargetException e) {
                        throw e.getCause();
                    }
                } finally {
                    try {
                        after(env);
                    } catch (InvocationTargetException e) {
                        throw e.getCause();
                    }
                }
            } // server will be closed at this point
        });
    }
}
