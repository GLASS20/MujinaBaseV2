package io.github.lefraudeur;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main
{
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    // warning called from c++ thread
    public static void onLoad()
    {
        if (MinecraftClient.getInstance().player != null) {
            MinecraftClient.getInstance().player.sendMessage(Text.of("Hello fron Mujina"));
        }
    }

    // warning called from c++ thread
    public static void onUnload()
    {
        if (MinecraftClient.getInstance().player != null) {
            MinecraftClient.getInstance().player.sendMessage(Text.of("Bye fron Mujina"));
        }
    }
}