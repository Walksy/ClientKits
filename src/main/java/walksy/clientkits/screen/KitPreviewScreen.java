package walksy.clientkits.screen;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.CraftingRecipeBookComponent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
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
    protected void extractLabels(final GuiGraphicsExtractor graphics, final int xm, final int ym) {
        graphics.text(this.font, this.title, 97, this.titleLabelY, -12566464, false);
    }

    @Override
    public void extractBackground(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        int xo = this.leftPos;
        int yo = this.topPos;
        graphics.blit(RenderPipelines.GUI_TEXTURED, INVENTORY_LOCATION, xo, yo, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
        InventoryScreen.extractEntityInInventoryFollowsMouse(graphics, xo + 26, yo + 8, xo + 75, yo + 78, 30, 0.0625F, mouseX, mouseY, PreviewInventory.model(this.minecraft.player, this.kit));
    }

    @Override
    public boolean showsActiveEffects() {
        return false;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        return false;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        return false;
    }

    @Override
    protected ScreenPosition getRecipeBookButtonPosition() {
        return null;
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (input.key() == InputConstants.KEY_ESCAPE || this.minecraft.options.keyInventory.matches(input)) {
            return super.keyPressed(input);
        }

        return true;
    }
}
