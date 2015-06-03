package io.github.s0cks.quasi;

import java.lang.reflect.Modifier;

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

    public static boolean mockable(Class<?> clazz){
        return !clazz.isPrimitive() && !Modifier.isFinal(clazz.getModifiers());
    }
}