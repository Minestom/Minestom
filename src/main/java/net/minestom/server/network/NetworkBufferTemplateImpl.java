package net.minestom.server.network;

import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnknownNullability;

import java.lang.classfile.ClassFile;
import java.lang.classfile.CodeBuilder;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;

@ApiStatus.Internal
final class NetworkBufferTemplateImpl {
    private static final String PACKAGE = "net.minestom.server.network";
    private static final ClassDesc CD_OBJECT = ConstantDescs.CD_Object;
    private static final ClassDesc CD_STRING = ConstantDescs.CD_String;
    private static final ClassDesc CD_CLASS = ConstantDescs.CD_Class;
    private static final ClassDesc CD_INT = ConstantDescs.CD_int;
    private static final ClassDesc CD_VOID = ConstantDescs.CD_void;
    private static final ClassDesc CD_METHOD_HANDLES = ConstantDescs.CD_MethodHandles;
    private static final ClassDesc CD_METHOD_HANDLES_LOOKUP = ConstantDescs.CD_MethodHandles_Lookup;
    private static final ClassDesc CD_NETWORK_BUFFER = NetworkBuffer.class.describeConstable().orElseThrow();
    private static final ClassDesc CD_TYPE = NetworkBuffer.Type.class.describeConstable().orElseThrow();
    private static final ClassDesc CD_TEMPLATE_TYPE = NetworkTemplate.class.describeConstable().orElseThrow();
    private static final ClassDesc CD_FUNCTION = Function.class.describeConstable().orElseThrow();

    private static final MethodTypeDesc MT_VOID = MethodTypeDesc.of(CD_VOID);
    private static final MethodTypeDesc MT_LOOKUP = MethodTypeDesc.of(CD_METHOD_HANDLES_LOOKUP);
    private static final MethodTypeDesc MT_CLASS_DATA_AT = MethodTypeDesc.of(CD_OBJECT, CD_METHOD_HANDLES_LOOKUP, CD_STRING, CD_CLASS, CD_INT);
    private static final MethodTypeDesc MT_READ_OBJECT = MethodTypeDesc.of(CD_OBJECT, CD_NETWORK_BUFFER);
    private static final MethodTypeDesc MT_WRITE_OBJECT = MethodTypeDesc.of(CD_VOID, CD_NETWORK_BUFFER, CD_OBJECT);
    private static final MethodTypeDesc MT_FUNCTION_APPLY = MethodTypeDesc.of(CD_OBJECT, CD_OBJECT);

    private static final int FIELD_FLAGS = ClassFile.ACC_PRIVATE | ClassFile.ACC_STATIC | ClassFile.ACC_FINAL | ClassFile.ACC_SYNTHETIC;
    private static final int METHOD_FLAGS = ClassFile.ACC_PUBLIC | ClassFile.ACC_FINAL | ClassFile.ACC_SYNTHETIC;
    private static final int CLASS_FLAGS = ClassFile.ACC_FINAL | ClassFile.ACC_SUPER | ClassFile.ACC_SYNTHETIC;

    private static final String CTOR_NAME = "ctor";
    private static final String TYPE_PREFIX = "t";
    private static final String GETTER_PREFIX = "g";
    private static final String READ = "read";
    private static final String WRITE = "write";

    private NetworkBufferTemplateImpl() {
    }

    // pairs of [Type<T>, Function (getter)] for N fields, up to 20
    // always odd because ends in ctor applicable to N.
    @SuppressWarnings("unchecked")
    static <T extends @UnknownNullability Object> NetworkBuffer.Type<T> template(Object... values) {
        Objects.requireNonNull(values, "values");
        Check.argCondition(values.length % 2 == 0, "Expected an odd number of values, got: {0}", values.length);
        Check.argCondition(values.length < 3, "Expected at least three values ([type, getter], ctor), got: {0}", values.length);
        final int fieldCount = values.length / 2;
        Check.argCondition(fieldCount > 20, "Templates only support up to 20 fields, got: {0}", fieldCount);
        for (int i = 0; i < fieldCount; i++) {
            Objects.requireNonNull(values[i * 2], typeName(i));
            Objects.requireNonNull(values[i * 2 + 1], getterName(i));
        }
        Objects.requireNonNull(values[values.length - 1], CTOR_NAME);
        try {
            final ClassDesc classDesc = ClassDesc.of(PACKAGE, "NetworkTemplateImpl");
            final byte[] bytes = ClassFile.of().build(classDesc, classBuilder -> {
                classBuilder.withFlags(CLASS_FLAGS)
                        .withSuperclass(CD_OBJECT)
                        .withInterfaceSymbols(CD_TEMPLATE_TYPE);

                for (int i = 0; i < fieldCount; i++) {
                    classBuilder.withField(typeName(i), CD_TYPE, FIELD_FLAGS);
                    classBuilder.withField(getterName(i), CD_FUNCTION, FIELD_FLAGS);
                }
                final ClassDesc ctor = constructorInterface(fieldCount);
                classBuilder.withField(CTOR_NAME, ctor, FIELD_FLAGS);

                classBuilder.withMethodBody(ConstantDescs.CLASS_INIT_NAME, MT_VOID, ClassFile.ACC_STATIC | ClassFile.ACC_SYNTHETIC,
                        codeBuilder -> buildClassInitializer(codeBuilder, classDesc, fieldCount, ctor));
                classBuilder.withMethodBody(ConstantDescs.INIT_NAME, MT_VOID, ClassFile.ACC_PRIVATE | ClassFile.ACC_SYNTHETIC,
                        codeBuilder -> codeBuilder.aload(0).invokespecial(CD_OBJECT, ConstantDescs.INIT_NAME, MT_VOID).return_());
                classBuilder.withMethodBody(WRITE, MT_WRITE_OBJECT, METHOD_FLAGS,
                        codeBuilder -> buildWrite(codeBuilder, classDesc, fieldCount));
                classBuilder.withMethodBody(READ, MT_READ_OBJECT, METHOD_FLAGS,
                        codeBuilder -> buildRead(codeBuilder, classDesc, fieldCount, ctor));
            });

            final MethodHandles.Lookup lookup = MethodHandles.lookup().defineHiddenClassWithClassData(bytes, Arrays.asList(values), true, MethodHandles.Lookup.ClassOption.NESTMATE);
            final MethodHandle constructor = lookup.findConstructor(lookup.lookupClass(), MethodType.methodType(void.class));
            return (NetworkBuffer.Type<T>) constructor.invoke();
        } catch (Throwable throwable) {
            throw new IllegalStateException("Failed to generate network type template, if this continues to be an issue consider disabling compiled templates by setting the property `minestom.template-compiler` to `false`", throwable);
        }
    }

    private static void buildClassInitializer(CodeBuilder codeBuilder, ClassDesc classDesc, int fieldCount, ClassDesc ctor) {
        codeBuilder.invokestatic(CD_METHOD_HANDLES, "lookup", MT_LOOKUP)
                .astore(0);
        for (int i = 0; i < fieldCount; i++) {
            loadClassDataAt(codeBuilder, CD_TYPE, i * 2)
                    .putstatic(classDesc, typeName(i), CD_TYPE);
            loadClassDataAt(codeBuilder, CD_FUNCTION, i * 2 + 1)
                    .putstatic(classDesc, getterName(i), CD_FUNCTION);
        }
        loadClassDataAt(codeBuilder, ctor, fieldCount * 2)
                .putstatic(classDesc, CTOR_NAME, ctor)
                .return_();
    }

    private static void buildWrite(CodeBuilder codeBuilder, ClassDesc classDesc, int fieldCount) {
        for (int i = 0; i < fieldCount; i++) {
            codeBuilder.getstatic(classDesc, typeName(i), CD_TYPE)
                    .aload(1)
                    .getstatic(classDesc, getterName(i), CD_FUNCTION)
                    .aload(2)
                    .invokeinterface(CD_FUNCTION, "apply", MT_FUNCTION_APPLY)
                    .invokeinterface(CD_TYPE, WRITE, MT_WRITE_OBJECT);
        }
        codeBuilder.return_();
    }

    private static void buildRead(CodeBuilder codeBuilder, ClassDesc classDesc, int fieldCount, ClassDesc ctor) {
        codeBuilder.getstatic(classDesc, CTOR_NAME, ctor);

        for (int i = 0; i < fieldCount; i++) {
            codeBuilder.getstatic(classDesc, typeName(i), CD_TYPE)
                    .aload(1)
                    .invokeinterface(CD_TYPE, READ, MT_READ_OBJECT);
        }
        codeBuilder.invokeinterface(ctor, "apply", constructorApplyType(fieldCount))
                .areturn();
    }

    private static ClassDesc constructorInterface(int fieldCount) {
        return ClassDesc.of(PACKAGE, "NetworkBufferTemplate$F" + fieldCount);
    }

    private static MethodTypeDesc constructorApplyType(int fieldCount) {
        ClassDesc[] parameters = new ClassDesc[fieldCount];
        Arrays.fill(parameters, CD_OBJECT);
        return MethodTypeDesc.of(CD_OBJECT, parameters);
    }

    private static CodeBuilder loadClassDataAt(CodeBuilder codeBuilder, ClassDesc type, int index) {
        return codeBuilder.aload(0) // assumes lookup is at slot 0
                .ldc("_")
                .ldc(type)
                .loadConstant(index)
                .invokestatic(CD_METHOD_HANDLES, "classDataAt", MT_CLASS_DATA_AT)
                .checkcast(type);
    }

    private static String typeName(int index) {
        return TYPE_PREFIX + (index + 1);
    }

    private static String getterName(int index) {
        return GETTER_PREFIX + (index + 1);
    }

    public interface NetworkTemplate extends NetworkBuffer.Type<Object> {
    }
}
