package walksy.customkits.mixin;

import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import walksy.customkits.manager.KitCommandManager;

@Mixin(ChatHud.class)
public class ChatHudMixin {

    @Inject(method = "addMessage(Lnet/minecraft/text/Text;)V", at = @At("HEAD"), cancellable = true)
    public void stopIncomingDebug(Text message, CallbackInfo ci)
    {
        if (KitCommandManager.loadKit || KitCommandManager.i != 0)
        {
            if (message.getString().equals("Set own game mode to Creative Mode")
                || message.getString().equals("Set own game mode to Survival Mode")
                || message.getString().equals("Set own game mode to Adventure Mode")
                || message.getString().equals("Set own game mode to Spectator Mode"))
            {
                ci.cancel();
            }
        }
    }
}
