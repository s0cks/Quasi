package io.github.s0cks.quasi;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Arrays;

final class ClassDumper{
    private final Class<?> c;

    ClassDumper(Class<?> c) {
        this.c = c;
    }

    public void dumpToFile(Path file)
    throws IOException, InterruptedException {
        ClassReader reader = new ClassReader(MockBytecodeGenerator.ByteArrayClassLoader.instance().unload(c.getName()));
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES|ClassWriter.COMPUTE_MAXS);
        reader.accept(new DecompilationVisitor(writer, new PrintWriter(System.out)), 0);
    }

    private static final class DecompilationVisitor
    extends ClassVisitor{
        private final Writer out;

        public DecompilationVisitor(ClassVisitor visitor, Writer out){
            super(Opcodes.ASM5, visitor);
            this.out = out;
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces);

            try{
                this.out.write("class " + name + " extends " + superName + " implements " + Arrays.toString(interfaces) + "\n");
                this.out.flush();
            } catch(IOException e){
                throw new QuasiException("Error writing to stream", e);
            }
        }
    }
}