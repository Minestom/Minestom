package net.minestom.server.utils;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
public class LambdaMetafactoryUtils {

    private final static MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    public static <T> Supplier<T> getEmptyConstructor(Class<T> targetClass)
            throws Throwable {
        MethodHandle handle = LOOKUP.findConstructor(targetClass, MethodType.methodType(void.class));
        return (Supplier<T>) LambdaMetafactory.metafactory(
                LOOKUP,
                "get",
                MethodType.methodType(Supplier.class),
                handle.type().generic(),
                handle,
                handle.type()
        ).getTarget().invokeExact();
    }

    public static <A, T> Function<A, T> getSingleArgumentConstructor(Class<T> targetClass, Class<A> argumentClass) throws Throwable {
        MethodHandle handle = LOOKUP.findConstructor(targetClass, MethodType.methodType(void.class, argumentClass));
        return (Function<A, T>) LambdaMetafactory.metafactory(
                LOOKUP,
                "apply",
                MethodType.methodType(Function.class),
                handle.type().generic(),
                handle,
                handle.type()
        ).getTarget().invokeExact();
    }

}
