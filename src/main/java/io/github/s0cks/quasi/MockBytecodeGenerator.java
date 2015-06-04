package io.github.s0cks.quasi;

import io.github.s0cks.quasi.reflect.Key;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class MockBytecodeGenerator {
    private static final Random rand = new Random();

    private final Lock lock = new ReentrantLock();
    private final Caching.Cache<ClassLoader, CachedBytecodeGenerator> generatorCache = Caching.newLRU();

    @SuppressWarnings("unchecked")
    public <T> T getInstance(MockSpec spec) {
        try {
            return (T) get(spec).newInstance();
        } catch (Exception e) {
            throw new AssertionError("This should never happen", e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> Class<T> get(MockSpec spec) {
        this.lock.lock();
        try {
            return (Class<T>) generate(spec.mockKey.rawType).getMockClass(spec);
        } finally {
            this.lock.unlock();
        }
    }

    private <T> CachedBytecodeGenerator generate(Class<T> tClass) {
        if (!this.generatorCache.containsKey(tClass.getClassLoader())) {
            this.generatorCache.put(tClass.getClassLoader(), new CachedBytecodeGenerator());
        }

        return this.generatorCache.get(tClass.getClassLoader());
    }

    protected static final class ByteArrayClassLoader
            extends ClassLoader {
        private static ByteArrayClassLoader instance;
        private final Caching.Cache<String, byte[]> classcache = Caching.newLRU();

        public static ByteArrayClassLoader instance() {
            return instance == null ? instance = new ByteArrayClassLoader() : instance;
        }

        public byte[] unload(String name){
            return this.classcache.get(name);
        }

        public Class<?> load(String name, byte[] bytes) {
            this.classcache.put(name, bytes);
            return this.defineClass(name, bytes, 0, bytes.length);
        }
    }

    private static final class CachedBytecodeGenerator {
        private final Caching.Cache<Key<?>, WeakReference<Class<?>>> classCache = Caching.newLRU();

        public Class<?> getMockClass(MockSpec spec) {
            WeakReference<Class<?>> classReference = this.classCache.get(spec.mockKey);
            Class<?> mockClass = null;
            if (classReference != null) {
                mockClass = classReference.get();
            }

            if (mockClass == null) {
                mockClass = this.generate(spec);
            }

            this.classCache.put(spec.mockKey, new WeakReference<Class<?>>(mockClass));
            return mockClass;
        }

        @SuppressWarnings("unchecked")
        private <T> Class<? extends T> generate(MockSpec spec) {
            String name = this.name();

            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES + ClassWriter.COMPUTE_MAXS);
            cw.visit(Opcodes.V1_7, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER + Opcodes.ACC_FINAL, name, null, getSuperClassInternalName(spec), interfaces(spec.mockKey.rawType));

            MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, getSuperClassInternalName(spec), "<init>", "()V", false);

            for (Method m : spec.mockKey.rawType.getDeclaredMethods()) {
                if (Modifier.isAbstract(m.getModifiers())) {
                    if (!spec.getActionMap().containsKey(m.getName())) {
                        this.generateDefaultMethod(cw, m);
                    } else {
                        spec.getActionMap()
                            .get(m.getName())
                            .generateBytecode(cw, m, mv, spec, name);
                    }
                }
            }

            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(-1, -1);
            mv.visitEnd();
            cw.visitEnd();
            return (Class<? extends T>) ByteArrayClassLoader.instance()
                                                            .load(name, cw.toByteArray());
        }

        private String getSuperClassInternalName(MockSpec spec){
            return spec.mockKey.rawType.isInterface() ? "java/lang/Object" : Type.getInternalName(spec.mockKey.rawType);
        }

        private String[] interfaces(Class<?> clazz) {
            String[] interfaces = new String[clazz.getGenericInterfaces().length + (clazz.isInterface() ? 1 : 0)];
            for (int i = (clazz.isInterface() ? 1 : 0); i < clazz.getGenericInterfaces().length; i++) {
                interfaces[i] = Type.getInternalName((Class<?>) clazz.getGenericInterfaces()[i]);
            }
            if(clazz.isInterface()){
                interfaces[0] = Type.getInternalName(clazz);
            }
            return interfaces;
        }

        private void generateDefaultMethod(ClassVisitor cv, Method m) {
            Class<?>[] excs = m.getExceptionTypes();
            String[] exc = new String[excs.length];
            for (int i = 0; i < excs.length; i++) {
                exc[i] = Type.getInternalName(excs[i]);
            }
            MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC, m.getName(), Type.getMethodDescriptor(m), null, exc);
            mv.visitCode();
            mv.visitTypeInsn(Opcodes.NEW, "java/lang/UnsupportedOperationException");
            mv.visitInsn(Opcodes.DUP);
            mv.visitLdcInsn("Method " + m.getName() + " is not implemented");
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/UnsupportedOperationException", "<init>", "(Ljava/lang/String;)V", false);
            mv.visitInsn(Opcodes.ATHROW);
            mv.visitMaxs(-1, -1);
            mv.visitEnd();
        }

        private String name() {
            return String.format("%s$%d", "QuasiMock", Math.abs(rand.nextInt(100)));
        }
    }
}