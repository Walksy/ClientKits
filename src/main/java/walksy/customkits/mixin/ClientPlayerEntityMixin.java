package walksy.customkits.mixin;

import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import walksy.customkits.main.ClientKitsMod;
import walksy.customkits.manager.KitCommandManager;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {

    @Inject(method = "tick()V", at = @At("TAIL"))
    private void playerTick(CallbackInfo ci)
    {
        KitCommandManager.tick();
    }
}
