package io.github.s0cks.quasi;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Paths;

public final class Quasi{
    private static final MockBytecodeGenerator bytecodeGenerator = new MockBytecodeGenerator();

    public static MockSpecBuilder mockSpec(Class<?> c){
        if(!mockable(c)){
            throw new QuasiException("Unable to mock primitive/final types");
        }

        return new MockSpecBuilder(c, bytecodeGenerator);
    }

    public static <T> T mock(Class<T> tClass){
        return mockSpec(tClass).build().create();
    }

    protected static void dumpClassFileInformation(Class<?> clazz)
    throws IOException, InterruptedException{
        new ClassDumper(clazz).dumpToFile(Paths.get(System.getProperty("user.dir"), "Dumps", clazz.getSimpleName() + ".dump"));
    }

    public static boolean mockable(Class<?> clazz){
        return !clazz.isPrimitive() && !Modifier.isFinal(clazz.getModifiers());
    }
}