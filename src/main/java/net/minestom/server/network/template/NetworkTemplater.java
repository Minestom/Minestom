package net.minestom.server.network.template;

import net.minestom.server.network.NetworkBuffer.Type;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnknownNullability;

import java.io.IOException;
import java.lang.constant.ClassDesc;
import java.lang.invoke.MethodHandle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;


@ApiStatus.Internal
public final class NetworkTemplater {
    private static final AtomicLong COUNTER = new AtomicLong();
    private static final boolean DEBUG = true; // Really shouldn't be an option

    private NetworkTemplater() {
    }

    public static <T extends @UnknownNullability Object> Type<T> templateN(Object... args) {
        Class<?> caller = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                .walk(frames -> frames
                        .map(StackWalker.StackFrame::getDeclaringClass)
                        .skip(2)
                        .findFirst()).orElseThrow();
        return templateN(caller, args);
    }

    public static <T> Type<T> templateN(Class<?> caller, Object... args) {
        TemplateReflection.FieldAnalysis[] fields = new TemplateReflection.FieldAnalysis[args.length / 2];
        for (int i = 0; i < (args.length - 1) / 2; i++) {
            // HandleAligner unrolls the TransformingType and folds logic into the MethodHandle
            fields[i] = TemplateReflection.conformField((Type<?>) args[i * 2], args[i * 2 + 1]);
        }

        // Deduplicate Types
        int[] fieldToUniqueIndex = new int[fields.length];
        TemplateReflection.FieldAnalysis[] uniqueFields = flattenFields(fields, fieldToUniqueIndex);

        MethodHandle foldedCtor = TemplateReflection.conformConstructor(args[args.length - 1], args);

        final ClassDesc thisClass = ClassDesc.of(caller.getPackageName(), "%s$TType$%d".formatted(caller.getSimpleName(), COUNTER.incrementAndGet()));
        byte[] bytes = new TemplateGenerator(thisClass, foldedCtor, fields, uniqueFields, fieldToUniqueIndex).build();

        // Dump the class if enabled
        debug(thisClass, bytes);

        // Prepare Constructor Args for the New Class
        // Args: [UniqueType0, UniqueType1...], [Getter0, Getter1...], [Ctor]
        Object[] ctorArgs = new Object[uniqueFields.length + fields.length + 1];
        int argIndex = 0;
        for (TemplateReflection.FieldAnalysis unique : uniqueFields) {
            ctorArgs[argIndex++] = unique.physicalType();
        }
        for (TemplateReflection.FieldAnalysis field : fields) {
            ctorArgs[argIndex++] = field.getter();
        }
        ctorArgs[argIndex] = foldedCtor;

        //noinspection unchecked
        return (NetworkTemplate<T>) TemplateReflection.init(caller, bytes, ctorArgs);
    }

    // Not reflection, really just checking identity to deduplicate
    private static TemplateReflection.FieldAnalysis[] flattenFields(TemplateReflection.FieldAnalysis[] fields, int[] fieldToUniqueIndex) {
        List<TemplateReflection.FieldAnalysis> uniqueFields = new ArrayList<>(fields.length);
        // Quite equivalent to a similar identity hash map good for small N
        for (int i = 0; i < fields.length; i++) {
            TemplateReflection.FieldAnalysis current = fields[i];
            int index = -1;
            for (int j = 0; j < uniqueFields.size(); j++) {
                if (uniqueFields.get(j).physicalType() == current.physicalType()) {
                    index = j;
                    break;
                }
            }
            if (index == -1) {
                index = uniqueFields.size();
                uniqueFields.add(current);
            }
            fieldToUniqueIndex[i] = index;
        }
        return uniqueFields.toArray(new TemplateReflection.FieldAnalysis[0]);
    }

    // Dumps the current class into the dump folder to see the generated class
    private static void debug(ClassDesc thisClass, byte[] bytes) {
        if (!DEBUG) return;
        try {
            String pkg = thisClass.packageName();
            Path path = java.nio.file.Paths.get("dump", pkg.replace('.', '/'), thisClass.displayName() + ".class");
            Files.createDirectories(path.getParent());
            Files.write(path, bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}