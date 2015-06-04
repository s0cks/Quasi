package io.github.s0cks.quasi;

import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class QuasiTest {
    @Test
    public void testMock()
    throws Exception {
        AMockTest mockTest = Quasi.mockSpec(AMockTest.class)
                .when("name").thenReturn("Test")
                .when("parent").thenReturn(new Object())
                .build().create();
        Quasi.dumpClassFileInformation(mockTest.getClass());
        dumpInstanceInformation(mockTest);

        IMockTest test = Quasi.mock(IMockTest.class);
        Quasi.dumpClassFileInformation(test.getClass());
        dumpInstanceInformation(test);
    }

    private void dumpInstanceInformation(Object instance){
        System.out.println(
                                  instance.getClass()
                                          .getName()
        );

        for(Field f : instance.getClass().getDeclaredFields()){
            System.out.println(f.getName() + " -> " + f.getGenericType());
        }

        for(Method m : instance.getClass().getDeclaredMethods()){
            System.out.println(m.getName() + "()");
        }
    }

    public static abstract class AMockTest{
        public abstract String name();
        public abstract Object parent();
    }

    public static interface IMockTest{
        public String name();
        public Object parent();
    }
}