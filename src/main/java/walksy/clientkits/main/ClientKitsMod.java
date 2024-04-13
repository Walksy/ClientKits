package walksy.clientkits.main;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import walksy.clientkits.manager.ConfigManager;
import walksy.clientkits.manager.KitCommandManager;


public class ClientKitsMod implements ModInitializer {

    @Override
    public void onInitialize()
    {
        new KitCommandManager();
        ConfigManager.loadKitsFromFile();
    }

    public static void debugMessage(String message)
    {
        MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.of("§5[Client Kits] " + message));
    }
}
