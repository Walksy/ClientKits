package walksy.clientkits.mixin;

import main.walksy.lib.core.platform.WLInput;
import main.walksy.lib.core.platform.input.CharacterEvent;
import main.walksy.lib.core.platform.input.KeyEvent;
import main.walksy.lib.core.render.GuiContext;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
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

    @Inject(method = "render", at = @At("TAIL"))
    private void clientkits$extract(final GuiGraphics graphics, final int mouseX, final int mouseY,
                                    final float a, final CallbackInfo ci) {
        EnderChestKitOverlay.extract(this, GuiContext.graphics(graphics), mouseX, mouseY, a);
    }

    @Inject(method = "renderTooltip", at = @At("HEAD"), cancellable = true)
    private void clientkits$suppressTooltip(final GuiGraphics graphics, final int mouseX,
                                            final int mouseY, final CallbackInfo ci) {
        if (EnderChestKitOverlay.capturesPointer(this, mouseX, mouseY)) {
            ci.cancel();
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void clientkits$mouseClicked(final double mouseX, final double mouseY, final int button,
                                          final CallbackInfoReturnable<Boolean> cir) {
        if (EnderChestKitOverlay.mouseClicked(this, WLInput.mouse(mouseX, mouseY, button))) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
    private void clientkits$mouseReleased(final double mouseX, final double mouseY, final int button,
                                           final CallbackInfoReturnable<Boolean> cir) {
        if (EnderChestKitOverlay.mouseReleased(this, WLInput.mouse(mouseX, mouseY, button))) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
    private void clientkits$mouseDragged(final double mouseX, final double mouseY, final int button,
                                          final double dragX, final double dragY,
                                          final CallbackInfoReturnable<Boolean> cir) {
        if (EnderChestKitOverlay.mouseDragged(this, WLInput.mouse(mouseX, mouseY, button), dragX, dragY)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void clientkits$keyPressed(final int key, final int scancode, final int modifiers,
                                        final CallbackInfoReturnable<Boolean> cir) {
        if (EnderChestKitOverlay.keyPressed(this, new KeyEvent(key, scancode, modifiers))) {
            cir.setReturnValue(true);
        }
    }

    @Override
    public boolean charTyped(final char character, final int modifiers) {
        return EnderChestKitOverlay.charTyped(this, new CharacterEvent(character, modifiers)) || super.charTyped(character, modifiers);
    }
}
