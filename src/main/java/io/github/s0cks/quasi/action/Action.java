package io.github.s0cks.quasi.action;

import io.github.s0cks.quasi.MockSpec;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import java.lang.reflect.Method;

public interface Action{
    public void generateBytecode(ClassWriter cv, Method method, MethodVisitor init, MockSpec spec, String name);
}