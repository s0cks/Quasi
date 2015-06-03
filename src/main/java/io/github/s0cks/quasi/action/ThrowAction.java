package io.github.s0cks.quasi.action;

import io.github.s0cks.quasi.MockSpec;
import io.github.s0cks.quasi.QuasiException;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;

public final class ThrowAction
implements Action{
    private final Throwable delegate;

    public ThrowAction(Throwable delegate){
        this.delegate = delegate;
    }

    @Override
    public void generateBytecode(ClassWriter cv, Method m, MethodVisitor init, MockSpec spec, String name) {
        Class<?>[] excs = m.getExceptionTypes();
        String[] exc = new String[excs.length];
        for(int i = 0; i < excs.length; i++){
            exc[i] = Type.getInternalName(excs[i]);
        }
        MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC, m.getName(), Type.getMethodDescriptor(m), null, exc);
        mv.visitCode();

        mv.visitTypeInsn(Opcodes.NEW, Type.getInternalName(this.delegate.getClass()));
        mv.visitInsn(Opcodes.DUP);
        mv.visitLdcInsn(this.delegate.getMessage());
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(this.delegate.getClass()), "<init>", this.getDelegateConstructorDescriptor(), false);
        mv.visitInsn(Opcodes.ATHROW);

        mv.visitMaxs(-1, -1);
        mv.visitEnd();
    }

    private String getDelegateConstructorDescriptor(){
        try{
            return Type.getConstructorDescriptor(this.delegate.getClass().getDeclaredConstructor(String.class));
        } catch(Exception e){
            throw new QuasiException("Delegate Throwable's need a constructor with one arg of type String");
        }
    }
}