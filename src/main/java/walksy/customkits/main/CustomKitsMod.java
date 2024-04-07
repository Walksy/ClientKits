package walksy.customkits.main;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import walksy.customkits.manager.ConfigManager;
import walksy.customkits.manager.KitCommandManager;

import java.io.IOException;

public class CustomKitsMod implements ModInitializer {

    public static boolean debugger = true;

    @Override
    public void onInitialize()
    {
        new KitCommandManager();
        try {
            ConfigManager.loadKitsFromFiles();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void debugMessage(String message)
    {
        if (!debugger) return;
        MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.of(message));
    }
}
