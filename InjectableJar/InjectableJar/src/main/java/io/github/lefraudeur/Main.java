package io.github.lefraudeur;

import net.minecraft.client.Minecraft;

public class Main
{
    // warning called from c++ thread
    public static void onLoad()
    {
        Minecraft.getMinecraft().thePlayer.sendChatMessage("hello from Mujina");
    }

    // warning called from c++ thread
    public static void onUnload()
    {
        Minecraft.getMinecraft().thePlayer.sendChatMessage("bye from Mujina");
    }
}