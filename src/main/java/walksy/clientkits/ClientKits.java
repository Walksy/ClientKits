package walksy.clientkits;

import main.walksy.lib.api.event.WalksyLibEvents;
import net.fabricmc.api.ClientModInitializer;
import walksy.clientkits.kit.LegacyKits;
import walksy.clientkits.kit.command.ClientKitCommands;
import walksy.clientkits.manager.KitManager;

public final class ClientKits implements ClientModInitializer {


    @Override
    public void onInitializeClient() {
        ClientKitCommands.create();
        WalksyLibEvents.CLIENT_READY.register(minecraft -> LegacyKits.migrate());
        WalksyLibEvents.END_LOCAL_PLAYER_TICK.register(localPlayer -> {
            KitManager.tick();
        });
    }
}
