package io.github.lefraudeur.internal;

import io.github.lefraudeur.internal.patcher.MethodModifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface EventHandler
{
    MethodModifier.Type type();
    String targetClass();
    String targetMethodName();
    String targetMethodDescriptor();
    boolean targetMethodIsStatic() default false;
}
