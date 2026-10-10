package walksy.clientkits.mixin;

import main.walksy.lib.core.render.GuiContext;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import walksy.clientkits.screen.EnderChestKitOverlay;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin extends Screen {

    private AbstractContainerScreenMixin(final net.minecraft.network.chat.Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void clientkits$init(final CallbackInfo ci) {
        EnderChestKitOverlay.onInit(this);
    }

    @Inject(method = "removed", at = @At("TAIL"))
    private void clientkits$removed(final CallbackInfo ci) {
        EnderChestKitOverlay.onRemoved(this);
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void clientkits$extract(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY,
                                    final float a, final CallbackInfo ci) {
        EnderChestKitOverlay.extract(this, GuiContext.graphics(graphics), mouseX, mouseY, a);
    }

    @Inject(method = "extractTooltip", at = @At("HEAD"), cancellable = true)
    private void clientkits$suppressTooltip(final GuiGraphicsExtractor graphics, final int mouseX,
                                            final int mouseY, final CallbackInfo ci) {
        if (EnderChestKitOverlay.capturesPointer(this, mouseX, mouseY)) {
            ci.cancel();
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void clientkits$mouseClicked(final MouseButtonEvent event, final boolean doubleClick,
                                          final CallbackInfoReturnable<Boolean> cir) {
        if (EnderChestKitOverlay.mouseClicked(this, event)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
    private void clientkits$mouseReleased(final MouseButtonEvent event,
                                           final CallbackInfoReturnable<Boolean> cir) {
        if (EnderChestKitOverlay.mouseReleased(this, event)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
    private void clientkits$mouseDragged(final MouseButtonEvent event, final double dragX, final double dragY,
                                          final CallbackInfoReturnable<Boolean> cir) {
        if (EnderChestKitOverlay.mouseDragged(this, event, dragX, dragY)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void clientkits$mouseScrolled(final double mouseX, final double mouseY, final double scrollX,
                                           final double scrollY, final CallbackInfoReturnable<Boolean> cir) {
        if (EnderChestKitOverlay.mouseScrolled(this, mouseX, mouseY, scrollX, scrollY)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void clientkits$keyPressed(final KeyEvent event, final CallbackInfoReturnable<Boolean> cir) {
        if (EnderChestKitOverlay.keyPressed(this, event)) {
            cir.setReturnValue(true);
        }
    }

    @Override
    public boolean charTyped(final CharacterEvent event) {
        return EnderChestKitOverlay.charTyped(this, event) || super.charTyped(event);
    }
}
