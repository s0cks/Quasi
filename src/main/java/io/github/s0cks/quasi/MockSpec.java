package io.github.s0cks.quasi;

import io.github.s0cks.quasi.action.Action;
import io.github.s0cks.quasi.reflect.Key;

import java.util.Collections;
import java.util.Map;

public final class MockSpec{
    public final Key mockKey;
    private final Map<String, Action> actionMap;
    private final MockBytecodeGenerator bgen;

    protected MockSpec(Key mockKey, Map<String, Action> retMap, MockBytecodeGenerator bgen){
        this.mockKey = mockKey;
        this.actionMap = retMap;
        this.bgen = bgen;
    }

    public Map<String, Action> getActionMap(){
        return Collections.unmodifiableMap(this.actionMap);
    }

    public <T> T create(){
        return this.bgen.getInstance(this);
    }

    @Override
    public String toString() {
        return "MockSpec{" +
                       "mockKey=" + mockKey +
                       '}';
    }
}