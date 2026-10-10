package walksy.clientkits.screen;

import com.mojang.blaze3d.platform.InputConstants;
import main.walksy.lib.core.render.GuiContext;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.CraftingRecipeBookComponent;
import net.minecraft.network.chat.Component;
import main.walksy.lib.api.config.value.ItemInventory;
import walksy.clientkits.manager.PreviewInventory;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;

public class KitPreviewScreen extends AbstractRecipeBookScreen<InventoryMenu> {

    private final ItemInventory kit;

    public KitPreviewScreen(Player player, Inventory inventory, String kitName, ItemInventory kit) {
        super(new InventoryMenu(inventory, false, player), new CraftingRecipeBookComponent(player.inventoryMenu), inventory, Component.literal(kitName));
        this.kit = kit;
    }

    @Override
    protected void renderLabels(final GuiGraphics graphics, final int xm, final int ym) {
        graphics.drawString(this.font, this.title, 97, this.titleLabelY, -12566464, false);
    }

    @Override
    protected void renderBg(final GuiGraphics graphics, final float a, final int mouseX, final int mouseY) {
        int xo = this.leftPos;
        int yo = this.topPos;
        GuiContext.graphics(graphics).blit(INVENTORY_LOCATION, xo, yo, 0.0F, 0.0F, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight, 256, 256, -1);
        InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, xo + 26, yo + 8, xo + 75, yo + 78, 30, 0.0625F, mouseX, mouseY, PreviewInventory.model(this.minecraft.player, this.kit));
    }

    @Override
    public boolean showsActiveEffects() {
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    protected ScreenPosition getRecipeBookButtonPosition() {
        return null;
    }

    @Override
    public boolean keyPressed(int key, int scancode, int modifiers) {
        if (key == InputConstants.KEY_ESCAPE || this.minecraft.options.keyInventory.matches(key, scancode)) {
            return super.keyPressed(key, scancode, modifiers);
        }

        return true;
    }
}
