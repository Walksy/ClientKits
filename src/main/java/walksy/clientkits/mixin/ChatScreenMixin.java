package walksy.clientkits.mixin;

import net.minecraft.client.gui.screen.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import walksy.clientkits.main.ClientKitsMod;
import walksy.clientkits.manager.ConfigManager;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {

    @Inject(method = "init()V", at = @At("HEAD"))
    public void init(CallbackInfo ci)
    {
        ClientKitsMod.debugMessage("Opened Chat Screen");
        ConfigManager.loadKitsFromFile();
    }
}
