package io.github.s0cks.quasi;

import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class QuasiTest {
    @Test
    public void testMock()
    throws Exception {
        IMockTest mockTest = Quasi.mockSpec(IMockTest.class)
                .when("name").thenThrow(new Throwable("Test"))
                .when("parent").thenReturn(new Object())
                .build().create();

        System.out.println(mockTest.getClass().getName());

        for(Field f : mockTest.getClass().getDeclaredFields()){
            System.out.println(f.getName() + " -> " + f.getGenericType());
        }

        for(Method m : mockTest.getClass().getDeclaredMethods()){
            System.out.println(m.getName() + "()");
        }

        System.out.println(mockTest.name());
        System.out.println(mockTest.parent());
    }

    public static interface IMockTest{
        public String name();
        public Object parent();
    }
}