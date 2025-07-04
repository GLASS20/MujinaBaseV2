package io.github.lefraudeur.internal;

// warning: changing this could break ReturnThrowMethodModifier
public class Thrower
{
    // not null if the target method was interrupted because of a ATHROW instruction
    // set to the object which was supposed to be thrown
    // if not null, and it is a throw, then the return value of the eventHandler is ignored
    public Throwable thrown = null;
}
