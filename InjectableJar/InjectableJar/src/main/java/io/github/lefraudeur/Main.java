package io.github.lefraudeur;

import net.minecraft.client.Minecraft;

public class Main
{
    // warning called from c++
    public static void onLoad()
    {
        Minecraft.getMinecraft().thePlayer.sendChatMessage("hello from Mujina");
    }

    // warning called from c++
    public static void onUnload()
    {

    }
}