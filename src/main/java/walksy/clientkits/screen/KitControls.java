package walksy.clientkits.screen;

import main.walksy.lib.core.render.WLGraphics2D;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

interface KitControls {

    void extract(WLGraphics2D graphics, int mouseX, int mouseY, float delta);

    default boolean capturesPointer(final double mouseX, final double mouseY) {
        return false;
    }

    default boolean mouseClicked(final MouseButtonEvent event) {
        return false;
    }

    default boolean mouseReleased(final MouseButtonEvent event) {
        return false;
    }

    default boolean mouseDragged(final MouseButtonEvent event, final double dragX, final double dragY) {
        return false;
    }

    default boolean mouseScrolled(final double mouseX, final double mouseY, final double scrollX, final double scrollY) {
        return false;
    }

    default boolean keyPressed(final KeyEvent event) {
        return false;
    }

    default boolean charTyped(final CharacterEvent event) {
        return false;
    }

    default void retire() {
    }
}
