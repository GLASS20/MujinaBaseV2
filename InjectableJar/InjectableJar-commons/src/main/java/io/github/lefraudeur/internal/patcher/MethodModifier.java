package io.github.lefraudeur.internal.patcher;

public class MethodModifier extends Modifier
{
    public enum Type
    {
        ON_ENTRY,
        ON_RETURN
    }
    public Type type;
    public String methodName;
    public String methodSignature;
    public String eventMethodClass;
    public String eventMethodName;
    public String eventMethodSignature;
    public boolean isStatic;

    public MethodModifier(Type type, String methodName, String methodSignature,
                          String eventMethodClass, String eventMethodName, String eventMethodSignature, boolean isStatic)
    {
        this.type = type;
        this.methodName = methodName;
        this.methodSignature = methodSignature;
        this.eventMethodClass = eventMethodClass;
        this.eventMethodName = eventMethodName;
        this.eventMethodSignature = eventMethodSignature;
        this.isStatic = isStatic;
    }

    @Override
    public String getNewInstanceCode()
    {
        return String.format("new MethodModifier(MethodModifier.Type.%s, \"%s\", \"%s\", \"%s\", \"%s\", \"%s\", %b)",
                type.name(),
                methodName,
                methodSignature,
                eventMethodClass,
                eventMethodName,
                eventMethodSignature,
                isStatic);
    }
}
