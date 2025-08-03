package io.github.lefraudeur;

import io.github.lefraudeur.internal.Canceler;
import io.github.lefraudeur.internal.EventHandler;
import net.minecraft.client.network.ClientPlayNetworkHandler;

import static io.github.lefraudeur.internal.patcher.MethodModifier.Type.*;

public class TestClass
{
    @EventHandler(type=ON_ENTRY,
            targetClass = "net/minecraft/client/network/ClientPlayNetworkHandler",
            targetMethodName = "sendChatMessage",
            targetMethodDescriptor = "(Ljava/lang/String;)V",
            targetMethodIsStatic = false)
    public static void sendChatMessage(Canceler canceler, ClientPlayNetworkHandler clientPlayNetworkHandler, String message)
    {
        System.out.println("sendChatMessage on entry succeeded");
    }
}
