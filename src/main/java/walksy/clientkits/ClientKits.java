package walksy.clientkits;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import walksy.clientkits.kit.KitIO;
import walksy.clientkits.kit.command.ClientKitCommands;
import walksy.clientkits.manager.KitManager;

import java.io.IOException;

public class ClientKits implements ModInitializer {


    @Override
    public void onInitialize() {
        ClientKitCommands.create();

        try {
            KitIO.init();
        } catch (IOException e) {
            System.err.println("[ClientKits] Failed to initialize directories!");
            e.printStackTrace();
        }

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null && client.gameMode != null) {
                KitManager.tick();
            }
        });
    }
}
