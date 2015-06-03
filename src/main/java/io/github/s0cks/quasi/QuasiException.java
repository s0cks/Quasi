package io.github.s0cks.quasi;

public final class QuasiException
extends RuntimeException{
    public QuasiException(String msg){
        super(msg);
    }

    public QuasiException(String msg, Throwable t){
        super(msg, t);
    }
}