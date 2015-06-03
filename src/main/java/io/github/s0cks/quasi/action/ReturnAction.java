package io.github.s0cks.quasi.action;

import io.github.s0cks.quasi.MockSpec;
import io.github.s0cks.quasi.QuasiException;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public final class ReturnAction
implements Action{
    private static final Map<Class<?>, Class<?>> unbox = new HashMap<>();
    static{
        unbox.put(Boolean.class, boolean.class);
        unbox.put(Integer.class, int.class);
        unbox.put(Short.class, short.class);
        unbox.put(Long.class, long.class);
        unbox.put(Double.class, double.class);
        unbox.put(Float.class, float.class);
        unbox.put(Void.class, void.class);
    }

    private static final Random rand = new Random();

    private final Object delegate;

    public ReturnAction(Object delegate) {
        this.delegate = delegate;
    }

    @Override
    public void generateBytecode(ClassWriter cv, Method m, MethodVisitor init, MockSpec spec, String clazzName) {
        Class<?>[] excs = m.getExceptionTypes();
        String[] exc = new String[excs.length];
        for(int i = 0; i < excs.length; i++){
            exc[i] = Type.getInternalName(excs[i]);
        }
        MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC, m.getName(), Type.getMethodDescriptor(m), null, exc);
        mv.visitCode();

        Class<?> dClass = this.delegate != null ? unbox(this.delegate.getClass()) : Object.class;
        Type dType = Type.getType(dClass);

        if(this.delegate == null){
            mv.visitInsn(Opcodes.ACONST_NULL);
        } else if(this.delegate instanceof Number){
            mv.visitLdcInsn(this.delegate);
        } else if(this.delegate instanceof String){
            mv.visitLdcInsn(this.delegate);
        } else{
            String name =  "field_" + Math.abs(rand.nextInt());
            FieldVisitor fv = cv.visitField(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, name, Type.getDescriptor(this.delegate.getClass()), null, null);
            fv.visitEnd();

            init.visitVarInsn(Opcodes.ALOAD, 0);
            init.visitTypeInsn(Opcodes.NEW, dType.getInternalName());
            init.visitInsn(Opcodes.DUP);
            init.visitMethodInsn(Opcodes.INVOKESPECIAL, dType.getInternalName(), "<init>", this.getDelegateConstructorDescriptor(), false);
            init.visitFieldInsn(Opcodes.PUTFIELD, clazzName, name, Type.getDescriptor(this.delegate.getClass()));

            mv.visitVarInsn(dType.getOpcode(Opcodes.ILOAD), 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, clazzName, name, dType.getDescriptor());
            mv.visitInsn(dType.getOpcode(Opcodes.IRETURN));
        }

        mv.visitInsn(dType != null ? dType.getOpcode(Opcodes.IRETURN) : Opcodes.ARETURN);
        mv.visitMaxs(-1, -1);
        mv.visitEnd();
    }

    private String getDelegateConstructorDescriptor(){
        try{
            return Type.getConstructorDescriptor(this.delegate.getClass().getDeclaredConstructor());
        } catch(Exception e){
            throw new QuasiException("Delegate's need a constructor with no args");
        }
    }

    private Class<?> unbox(Class<?> c){
        if(unbox.containsKey(c)){
            return unbox.get(c);
        }

        return c;
    }
}