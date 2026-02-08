package net.minestom.server.network.template;

import net.minestom.server.network.NetworkBuffer;

import java.io.Serializable;
import java.lang.constant.ClassDesc;
import java.lang.invoke.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

// TODO support Optional for BYTE.optional() folding on not null path.
final class TemplateReflection {

    public static FieldAnalysis conformField(NetworkBuffer.Type<?> topType, Object getterLambda) {
        MethodHandle getter = handle(getterLambda);
        NetworkBuffer.Type<?> currentType = topType;

        // Check if the type is a TransformingType, if so we can attempt to fold it.
        // If Type is TransformingType<Byte, Enum>, the getter returns Enum, but accepts byte possibly
        // If so, we can fold it.
        // We can only fold them if they are supported behind Serializable

        while (currentType instanceof TransformingType<?, ?> trans && trans.from() instanceof Serializable sf1) {
            // Get the raw 'from' handle (e.g., Enum -> Byte)
            MethodHandle fromHandle = handle(sf1);

            // Force the fromHandle to accept exactly what the getter currently returns
            fromHandle = fromHandle.asType(fromHandle.type().changeParameterType(0, getter.type().returnType()));

            // Chain getter returns T, fromHandle takes T -> NewGetter returns U
            getter = MethodHandles.filterReturnValue(getter, fromHandle);
            currentType = trans.parent();
        }

        // Now getter returns the type required by currentType (e.g. int, or String)

        // Ensure the getter returns the primitive type if currentType is PrimitiveType
        if (currentType instanceof PrimitiveType<?> pt) {
            // This forces the return type to be exactly the primitive class (e.g. int.class)
            // If the chain resulted in Integer, this unboxes it.
            getter = getter.asType(getter.type().changeReturnType(pt.primitiveClass()));
        }

        return new FieldAnalysis(currentType, getter, getter.type().returnType());
    }

    public static MethodHandle conformConstructor(Object ctorLambda, Object[] args) {
        int fieldCount = (args.length - 1) / 2;

        MethodHandle ctor = handle(ctorLambda);
        for (int i = 0; i < fieldCount; i++) {
            NetworkBuffer.Type<?> type = (NetworkBuffer.Type<?>) args[i * 2];

            // We need to reverse the transformations:
            // If Type is TransformingType<Byte, Enum>, the constructor expects Enum.
            // But we read Byte from the network.
            // We need to apply the 'to' function: Byte -> Enum.
            // We can only fold them if they are supported behind Serializable

            while (type instanceof TransformingType<?, ?> trans && trans.to() instanceof Serializable sf1) {
                MethodHandle toHandle = handle(sf1);

                // The 'toHandle' converts from ParentType (Byte) -> TransformedType (Enum).
                // The constructor currently expects TransformingType (Enum) at index i.
                // We want to filter the argument at index i so the new handle expects ParentType (Byte).

                // Align toHandle return type to strictly match what the constructor expects
                Class<?> expectedByCtor = ctor.type().parameterType(i);
                toHandle = toHandle.asType(toHandle.type().changeReturnType(expectedByCtor));

                // Apply filter: ctor(..., Enum, ...) -> newCtor(..., Byte, ...)
                ctor = MethodHandles.filterArguments(ctor, i, toHandle);

                type = trans.parent();
            }

            // If the final type is PrimitiveType (e.g. integer), the constructor corresponding arg must be int.
            if (type instanceof PrimitiveType<?> pt) {
                ctor = ctor.asType(ctor.type().changeParameterType(i, pt.primitiveClass()));
            }
        }

        return ctor;
    }

    public static MethodHandle handle(Object lambda) {
        if (lambda == null) return null;
        assert !(lambda instanceof MethodHandle) : "invalid call";
        if (!(lambda instanceof Serializable)) throw new IllegalArgumentException("Lambda must be serializable");
        try {
            Method writeReplace = lambda.getClass().getDeclaredMethod("writeReplace");
            writeReplace.setAccessible(true);
            SerializedLambda sl = (SerializedLambda) writeReplace.invoke(lambda);

            Class<?> implClass = Class.forName(sl.getImplClass().replace('/', '.'));
            MethodType mt = MethodType.fromMethodDescriptorString(sl.getImplMethodSignature(), implClass.getClassLoader());
            MethodHandles.Lookup lookup = findLookup(implClass);

            MethodHandle mh = switch (sl.getImplMethodKind()) {
                case MethodHandleInfo.REF_invokeVirtual, MethodHandleInfo.REF_invokeInterface ->
                        lookup.findVirtual(implClass, sl.getImplMethodName(), mt);
                case MethodHandleInfo.REF_invokeStatic -> lookup.findStatic(implClass, sl.getImplMethodName(), mt);
                case MethodHandleInfo.REF_invokeSpecial ->
                        lookup.findSpecial(implClass, sl.getImplMethodName(), mt, implClass);
                case MethodHandleInfo.REF_newInvokeSpecial -> lookup.findConstructor(implClass, mt);
                default ->
                        throw new UnsupportedOperationException("Unsupported lambda kind: " + sl.getImplMethodKind());
            };

            for (int i = 0; i < sl.getCapturedArgCount(); i++) {
                mh = mh.bindTo(sl.getCapturedArg(i));
            }

            return mh;
        } catch (Exception e) {
            throw new RuntimeException("Method references must be serializable " + lambda, e);
        }
    }

    static MethodHandles.Lookup findLookup(Class<?> caller) {
        MethodHandles.Lookup lookup;
        try {
            lookup = MethodHandles.privateLookupIn(caller, MethodHandles.lookup());
        } catch (IllegalAccessException e) {
            // If we can't get private access (e.g. standard library classes), fall back to public lookup
            lookup = MethodHandles.lookup();
        }
        return lookup;
    }

    static Object init(Class<?> caller, byte[] bytes, Object... args) {
        try {
            MethodHandles.Lookup lookup = findLookup(caller);
            Class<?> clazz = lookup.defineHiddenClass(bytes, true, MethodHandles.Lookup.ClassOption.NESTMATE, MethodHandles.Lookup.ClassOption.STRONG).lookupClass();
            return clazz.getDeclaredConstructors()[0].newInstance(args);
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    public record FieldAnalysis(NetworkBuffer.Type<?> physicalType, MethodHandle getter, Class<?> physicalClass) {
        public ClassDesc physicalDesc() {
            return physicalClass.describeConstable().orElseThrow();
        }
    }
}
