package net.minestom.server.network.template;

import net.minestom.server.network.NetworkBuffer;

import java.lang.classfile.ClassBuilder;
import java.lang.classfile.ClassFile;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodHandle;

@SuppressWarnings("ClassCanBeRecord") // Not necessary currently.
final class TemplateGenerator {
    private static final ClassDesc CD_OBJECT = ConstantDescs.CD_Object;
    private static final ClassDesc CD_VOID = ConstantDescs.CD_void;
    private static final ClassDesc CD_NETWORK_BUFFER = NetworkBuffer.class.describeConstable().orElseThrow();
    private static final ClassDesc CD_TYPE = NetworkBuffer.Type.class.describeConstable().orElseThrow();
    private static final ClassDesc CD_PRIMITIVE_TYPE = PrimitiveType.class.describeConstable().orElseThrow();
    private static final ClassDesc CD_METHOD_HANDLE = ConstantDescs.CD_MethodHandle;
    private static final ClassDesc CD_NETWORK_TEMPLATE = NetworkTemplate.class.describeConstable().orElseThrow();

    private static final MethodTypeDesc MT_READ_OBJECT = MethodTypeDesc.of(CD_OBJECT, CD_NETWORK_BUFFER);
    private static final MethodTypeDesc MT_WRITE_OBJECT = MethodTypeDesc.of(CD_VOID, CD_NETWORK_BUFFER, CD_OBJECT);

    private static final int FIELD_FLAGS = ClassFile.ACC_PRIVATE | ClassFile.ACC_FINAL | ClassFile.ACC_SYNTHETIC;
    private static final int METHOD_FLAGS = ClassFile.ACC_PUBLIC | ClassFile.ACC_FINAL | ClassFile.ACC_SYNTHETIC;

    private static final String CTOR_NAME = "ctor";
    private static final String TYPE_PREFIX = "t";
    private static final String GETTER_PREFIX = "g";
    private static final String READ = "read";
    private static final String WRITE = "write";

    final ClassDesc thisClass;
    final MethodHandle ctor;
    final TemplateReflection.FieldAnalysis[] fields;
    final TemplateReflection.FieldAnalysis[] uniqueFields;
    final int[] fieldToUniqueIndex;

    public TemplateGenerator(ClassDesc classDesc, MethodHandle ctor, TemplateReflection.FieldAnalysis[] fields, TemplateReflection.FieldAnalysis[] uniqueFields, int[] fieldToUniqueIndex) {
        this.thisClass = classDesc;
        this.ctor = ctor;
        this.fields = fields;
        this.uniqueFields = uniqueFields;
        this.fieldToUniqueIndex = fieldToUniqueIndex;
        super();
    }

    public byte[] build() {
        return ClassFile.of().build(thisClass, classBuilder -> {
            classBuilder.withFlags(ClassFile.ACC_FINAL | ClassFile.ACC_PUBLIC | ClassFile.ACC_SUPER | ClassFile.ACC_SYNTHETIC);
            classBuilder.withInterfaceSymbols(CD_NETWORK_TEMPLATE);

            buildFields(classBuilder);
            buildConstructor(classBuilder);
            buildWriteMethod(classBuilder);
            buildReadMethod(classBuilder);
        });
    }

    public void buildFields(ClassBuilder cb) {
        cb.withField(CTOR_NAME, CD_METHOD_HANDLE, FIELD_FLAGS);
        // Unique Types
        for (int i = 0; i < uniqueFields.length; i++) {
            boolean isPrimitive = uniqueFields[i].physicalType() instanceof PrimitiveType;
            ClassDesc fieldDesc = isPrimitive ? CD_PRIMITIVE_TYPE : CD_TYPE;
            cb.withField(TYPE_PREFIX + (i + 1), fieldDesc, FIELD_FLAGS);
        }
        // Getters (Logical)
        for (int i = 0; i < fields.length; i++) {
            cb.withField(GETTER_PREFIX + (i + 1), CD_METHOD_HANDLE, FIELD_FLAGS);
        }
    }

    public void buildConstructor(ClassBuilder cb) {
        // Constructor args: [Type0, Type1... (unique)], [Getter0, Getter1... (logical)], [Ctor]
        ClassDesc[] paramTypes = new ClassDesc[uniqueFields.length + fields.length + 1];
        int paramIndex = 0;

        for (TemplateReflection.FieldAnalysis uniqueField : uniqueFields) {
            boolean isPrimitive = uniqueField.physicalType() instanceof PrimitiveType;
            ClassDesc fieldDesc = isPrimitive ? CD_PRIMITIVE_TYPE : CD_TYPE;
            paramTypes[paramIndex++] = fieldDesc;
        }
        for (int i = 0; i < fields.length; i++) {
            paramTypes[paramIndex++] = CD_METHOD_HANDLE;
        }
        paramTypes[paramIndex] = CD_METHOD_HANDLE;

        cb.withMethodBody(ConstantDescs.INIT_NAME, MethodTypeDesc.of(CD_VOID, paramTypes), ClassFile.ACC_PUBLIC | ClassFile.ACC_SYNTHETIC, code -> {
            code.aload(0);
            code.invokespecial(CD_OBJECT, ConstantDescs.INIT_NAME, MethodTypeDesc.of(CD_VOID));

            int argSlot = 1;

            // Store Unique Types
            for (int i = 0; i < uniqueFields.length; i++) {
                boolean isPrimitive = uniqueFields[i].physicalType() instanceof PrimitiveType;
                ClassDesc fieldDesc = isPrimitive ? CD_PRIMITIVE_TYPE : CD_TYPE;

                code.aload(0);
                code.aload(argSlot++);
                code.putfield(thisClass, TYPE_PREFIX + (i + 1), fieldDesc);
            }

            // Store Logical Getters
            for (int i = 0; i < fields.length; i++) {
                code.aload(0);
                code.aload(argSlot++);
                code.putfield(thisClass, GETTER_PREFIX + (i + 1), CD_METHOD_HANDLE);
            }

            // Store Ctor
            code.aload(0);
            code.aload(argSlot);
            code.putfield(thisClass, CTOR_NAME, CD_METHOD_HANDLE);

            code.return_();
        });
    }

    public void buildWriteMethod(ClassBuilder cb) {
        cb.withMethodBody(WRITE, MT_WRITE_OBJECT, METHOD_FLAGS, code -> {
            for (int i = 0; i < fields.length; i++) {
                TemplateReflection.FieldAnalysis fa = fields[i];
                int uniqueIndex = fieldToUniqueIndex[i];
                boolean isPrimitive = fa.physicalType() instanceof PrimitiveType;
                ClassDesc fieldDesc = isPrimitive ? CD_PRIMITIVE_TYPE : CD_TYPE;

                code.aload(0);
                code.getfield(thisClass, TYPE_PREFIX + (uniqueIndex + 1), fieldDesc);
                code.aload(1); // Buffer

                code.aload(0);
                code.getfield(thisClass, GETTER_PREFIX + (i + 1), CD_METHOD_HANDLE);
                code.aload(2); // Record Instance (Object)

                // Checkcast the instance to the type the getter expects
                Class<?> receiverType = fa.getter().type().parameterType(0);
                if (receiverType != Object.class) {
                    code.checkcast(receiverType.describeConstable().orElseThrow());
                }

                // invokeExact: (Receiver) -> Value
                code.invokevirtual(CD_METHOD_HANDLE, "invokeExact", MethodTypeDesc.ofDescriptor(fa.getter().type().toMethodDescriptorString()));

                if (isPrimitive) {
                    PrimitiveType<?> pt = (PrimitiveType<?>) fa.physicalType();
                    code.invokeinterface(CD_PRIMITIVE_TYPE, pt.writeMethodName(), MethodTypeDesc.of(CD_VOID, CD_NETWORK_BUFFER, fa.physicalDesc()));
                } else {
                    code.invokeinterface(CD_TYPE, WRITE, MethodTypeDesc.of(CD_VOID, CD_NETWORK_BUFFER, CD_OBJECT));
                }
            }
            code.return_();
        });
    }

    public void buildReadMethod(ClassBuilder cb) {
        cb.withMethodBody(READ, MT_READ_OBJECT, METHOD_FLAGS, code -> {
            code.aload(0);
            code.getfield(thisClass, CTOR_NAME, CD_METHOD_HANDLE);

            for (int i = 0; i < fields.length; i++) {
                TemplateReflection.FieldAnalysis fa = fields[i];
                int uniqueIndex = fieldToUniqueIndex[i];
                boolean isPrimitive = fa.physicalType() instanceof PrimitiveType;
                ClassDesc fieldDesc = isPrimitive ? CD_PRIMITIVE_TYPE : CD_TYPE;

                code.aload(0);
                code.getfield(thisClass, TYPE_PREFIX + (uniqueIndex + 1), fieldDesc);
                code.aload(1); // Buffer

                if (isPrimitive) {
                    PrimitiveType<?> pt = (PrimitiveType<?>) fa.physicalType();
                    // Read raw primitive (Z, B, I, F, etc.)
                    code.invokeinterface(CD_PRIMITIVE_TYPE, pt.readMethodName(), MethodTypeDesc.of(fa.physicalDesc(), CD_NETWORK_BUFFER));
                } else {
                    // Read Object
                    code.invokeinterface(CD_TYPE, READ, MethodTypeDesc.of(CD_OBJECT, CD_NETWORK_BUFFER));

                    // Checkcast the object to what the ctor expects
                    // The ctor has been conformed in TemoplateReflection to expect exactly what we read.
                    Class<?> expected = ctor.type().parameterType(i);
                    if (expected != Object.class && !expected.isPrimitive()) {
                        code.checkcast(expected.describeConstable().orElseThrow());
                    }
                }
            }

            // invokeExact using the ctor's own MethodType
            code.invokevirtual(CD_METHOD_HANDLE, "invokeExact", MethodTypeDesc.ofDescriptor(ctor.type().toMethodDescriptorString()));

            // Emit a checkcast the return type, just to ensure it throws on mismatch
            Class<?> returnType = ctor.type().returnType();
            if (returnType != Object.class) {
                code.checkcast(returnType.describeConstable().orElseThrow());
            }

            code.areturn();
        });
    }
}