package io.github.lefraudeur;

import io.github.lefraudeur.internal.Canceler;
import io.github.lefraudeur.internal.EventHandler;
import net.minecraft.client.entity.EntityClientPlayerMP;

import static io.github.lefraudeur.internal.patcher.MethodModifier.Type.ON_ENTRY;

public class TestClass
{
    @EventHandler(type=ON_ENTRY,
            targetClass = "net/minecraft/client/entity/EntityClientPlayerMP",
            targetMethodName = "sendChatMessage",
            targetMethodDescriptor = "(Ljava/lang/String;)V",
            targetMethodIsStatic = false)
    public static void sendChatMessage(Canceler canceler, EntityClientPlayerMP player, String message)
    {
        System.out.println("test success");
    }
}
