package testextension;

import net.minestom.server.extras.selfmodification.CodeModifier;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.List;

public class TestModifier extends CodeModifier implements Opcodes {
    @Override
    public boolean transform(ClassNode source) {
        if(source.name.equals("net/minestom/server/instance/InstanceContainer")) {
            System.out.println("Modifying code of "+source.name);
            MethodNode constructor = findConstructor(source.methods);
            constructor.instructions.insert(constructor.instructions.getFirst(), buildInjectionCode());
            return true;
        }
        return false;
    }

    private InsnList buildInjectionCode() {
        InsnList list = new InsnList();
        list.add(new FieldInsnNode(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"));
        list.add(new LdcInsnNode("Hello from modified code!!"));
        list.add(new MethodInsnNode(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false));
        return list;
    }

    private MethodNode findConstructor(List<MethodNode> methods) {
        return methods.stream().filter(m -> m.name.equals("<init>")).findFirst().orElseThrow();
    }

    @Override
    public String getNamespace() {
        return "net.minestom.server";
    }
}
