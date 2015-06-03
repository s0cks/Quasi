package io.github.s0cks.quasi;

import io.github.s0cks.quasi.action.Action;
import io.github.s0cks.quasi.action.ReturnAction;
import io.github.s0cks.quasi.action.ThrowAction;
import io.github.s0cks.quasi.reflect.Key;

import java.util.HashMap;
import java.util.Map;

public final class MockSpecBuilder {
    private final MockBytecodeGenerator generator;
    private final Key mockKey;
    private final Map<String, Action> actionMap;

    protected MockSpecBuilder(Class<?> clazz, MockBytecodeGenerator generator){
        this(generator, Key.get(clazz));
    }

    protected MockSpecBuilder(MockBytecodeGenerator generator, Key mockKey) {
        this.generator = generator;
        this.mockKey = mockKey;
        this.actionMap = new HashMap<>();
    }

    public When when(String name){
        return new When(name, this);
    }

    public MockSpec build() {
        return new MockSpec(this.mockKey, this.actionMap, this.generator);
    }

    public static final class When{
        private final String name;
        private final MockSpecBuilder builder;

        private When(String name, MockSpecBuilder builder){
            this.name = name;
            this.builder = builder;
        }

        public MockSpecBuilder thenReturn(Object o){
            this.builder.actionMap.put(this.name, new ReturnAction(o));
            return this.builder;
        }

        public MockSpecBuilder thenThrow(Throwable t){
            this.builder.actionMap.put(this.name, new ThrowAction(t));
            return this.builder;
        }
    }
}