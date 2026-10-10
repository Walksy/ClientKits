package walksy.clientkits.screen;

import main.walksy.lib.core.gui.liquid.LiquidRect;
import main.walksy.lib.core.gui.style.PopoverStyle;
import main.walksy.lib.core.gui.modal.PopoverHost;
import main.walksy.lib.core.gui.widget.options.popover.CataloguePickerPopover;
import main.walksy.lib.core.render.WLGraphics2D;
import main.walksy.lib.core.render.WLRenderSystem;
import main.walksy.lib.core.render.WLGraphics2D;
import main.walksy.lib.core.utils.ColorUtils;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.inventory.ChestMenu;
import walksy.clientkits.config.ClientKitsConfig;
import walksy.clientkits.kit.EnderChests;

import java.util.List;
import java.util.function.Consumer;

final class GlassKitControls implements KitControls {
    private static final float CHEST_GAP = 6f;
    private static final float STACK_GAP = 10f;

    private static final float SIBLING_FADE_OUT_SPEED = 90f;
    private static final float SIBLING_FADE_IN_SPEED = 20f;

    private static final String ICON_ITEM = "minecraft:ender_chest";

    private static final float POPOVER_WIDTH = CataloguePickerPopover.DEFAULT_WIDTH;
    private static final float POPOVER_HEIGHT = CataloguePickerPopover.DEFAULT_HEIGHT;

    private static final float UI_SCALE = 0.5f;

    private static final PopoverStyle GLASS_TEXT = new PopoverStyle() {
        @Override
        public int title() {
            return 0x8CFFFFFF;
        }

        @Override
        public int label() {
            return 0xFFFFFFFF;
        }

        @Override
        public int secondaryLabel() {
            return 0xB3FFFFFF;
        }

        @Override
        public int tertiaryLabel() {
            return 0x8CFFFFFF;
        }

        @Override
        public int rowHoverFill() {
            return ColorUtils.withAlpha(0xFFFFFFFF, 0.14f);
        }

        @Override
        public int scrollbar(final boolean dragging) {
            return ColorUtils.withAlpha(0xFFFFFFFF, dragging ? 0.55f : 0.3f);
        }
    };

    private final Screen screen;
    private final ChestMenu menu;
    private final PopoverHost host = new PopoverHost();
    private final PopoverButton loadButton;
    private final PopoverButton saveButton;
    private PopoverButton trigger;

    GlassKitControls(final Screen screen, final ChestMenu menu) {
        this.screen = screen;
        this.menu = menu;
        this.loadButton = new PopoverButton(EnderChestKitOverlay.LOAD_LABEL,
            this.host.panelLayer(), this::openLoadPopover);
        this.saveButton = new PopoverButton(EnderChestKitOverlay.SAVE_LABEL,
            this.host.panelLayer() + 1, this::openSavePopover);
    }

    @Override
    public void retire() {
        this.host.closeInstant();
    }

    @Override
    public boolean capturesPointer(final double mouseX, final double mouseY) {
        return this.host.isOpen() && this.host.isOverPanel(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(final MouseButtonEvent event) {
        if (this.host.isOpen()) {
            this.host.mouseClicked(event, false);
            return true;
        }
        if (event.button() != 0) {
            return false;
        }
        for (final PopoverButton button : this.buttons()) {
            if (button.isMouseOver(event.x(), event.y())) {
                button.onClick(event, false);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseReleased(final MouseButtonEvent event) {
        for (final PopoverButton button : this.buttons()) {
            if (button.isArmed()) {
                button.onRelease(event);
                return true;
            }
        }
        if (this.host.isOpen()) {
            this.host.mouseReleased(event);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(final MouseButtonEvent event, final double dragX, final double dragY) {
        if (!this.host.isOpen()) {
            return false;
        }
        this.host.mouseDragged(event, dragX, dragY);
        return true;
    }

    @Override
    public boolean mouseScrolled(final double mouseX, final double mouseY,
                                 final double scrollX, final double scrollY) {
        if (!this.host.isOpen()) {
            return false;
        }
        this.host.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        return true;
    }

    @Override
    public boolean keyPressed(final KeyEvent event) {
        if (!this.host.isOpen()) {
            return false;
        }
        this.host.keyPressed(event);
        return true;
    }

    @Override
    public boolean charTyped(final CharacterEvent event) {
        if (!this.host.isOpen()) {
            return false;
        }
        this.host.charTyped(event);
        return true;
    }

    private List<PopoverButton> buttons() {
        return List.of(this.loadButton, this.saveButton);
    }

    private float uiScale() {
        return UI_SCALE;
    }

    private void layoutButtons() {
        final float s = this.uiScale();
        final int width = Math.max(Math.round(PopoverButton.widthFor(EnderChestKitOverlay.LOAD_LABEL, s)), Math.round(PopoverButton.widthFor(EnderChestKitOverlay.SAVE_LABEL, s)));
        final int height = Math.max(1, Math.round(PopoverButton.heightFor(s)));
        final EnderChestKitOverlay.Placement placement = EnderChestKitOverlay.place(this.screen, this.menu, width, height, Math.round(CHEST_GAP * s), Math.round(STACK_GAP * s), Math.round(PopoverHost.screenMargin()), 0);
        this.loadButton.layout(placement.loadX(), placement.loadY(), s, width);
        this.saveButton.layout(placement.saveX(), placement.saveY(), s, width);
    }

    @Override
    public void extract(final WLGraphics2D graphics, final int mouseX, final int mouseY, final float delta) {
        this.layoutButtons();
        graphics.nextStratum();
        for (final PopoverButton button : this.buttons()) {
            this.extractButton(button, graphics, mouseX, mouseY, delta);
        }
        this.host.extract(graphics, mouseX, mouseY, delta);
    }

    private void extractButton(final PopoverButton button, final WLGraphics2D graphics, final int mouseX, final int mouseY, final float delta) {
        if (button == this.trigger) {
            button.settleInteraction();
            button.setAlpha(1f);
            return;
        }
        final float dt = WLRenderSystem.frameDeltaSeconds();
        final boolean open = this.host.isOpen();
        button.setAlpha(approach(button.getAlpha(), open ? 0f : 1f, open ? SIBLING_FADE_OUT_SPEED : SIBLING_FADE_IN_SPEED, dt));
        if (open) {
            graphics.widget(button, -1, -1, delta);
            return;
        }
        graphics.widget(button, mouseX, mouseY, delta);
        if (button.isMouseOver(mouseX, mouseY)) {
            graphics.pointingHandCursor();
        }
    }

    private static float approach(final float current, final float target, final float speed, final float dt) {
        if (dt <= 0f) {
            return current;
        }
        return current + (target - current) * (1f - (float) Math.exp(-speed * dt));
    }

    private void openLoadPopover() {
        this.open(this.loadButton, EnderChestKitOverlay.LOAD_TITLE, new LoadEntries(), EnderChestKitOverlay::load);
    }

    private void openSavePopover() {
        this.open(this.saveButton, EnderChestKitOverlay.SAVE_LABEL, new SaveEntries(), EnderChestKitOverlay::save);
    }

    private void open(final PopoverButton button, final String title, final CataloguePickerPopover.Entries entries, final Consumer<String> onPick) {
        final LiquidRect anchor = new LiquidRect(button.getX(), button.getY(), button.getWidth(), button.getHeight(), button.cornerRadius());
        final CataloguePickerPopover picker = new CataloguePickerPopover(title, entries, name -> false, onPick, true, this.host::close, () -> this.trigger = null, button::style, this.uiScale(), this.host.panelLayer(), POPOVER_WIDTH, POPOVER_HEIGHT)
            .style(GLASS_TEXT)
            .withTriggerHandoff((graphics, alpha) -> button.extractLabel(graphics, alpha));
        this.trigger = button;
        this.host.openAt(anchor, button.getX(), button.getY(),
            this.screen.width, this.screen.height, picker);
    }

    private static final class LoadEntries implements CataloguePickerPopover.Entries {
        @Override
        public List<String> values() {
            return ClientKitsConfig.enderKitNames();
        }

        @Override
        public String label(final String value) {
            return value;
        }

        @Override
        public String itemId(final String value) {
            return ICON_ITEM;
        }

        @Override
        public String trailing(final String value) {
            return EnderChestKitOverlay.itemCount(EnderChests.filledCount(ClientKitsConfig.enderKit(value)));
        }

        @Override
        public String emptyMessage() {
            return EnderChestKitOverlay.EMPTY_MESSAGE;
        }
    }

    private static final class SaveEntries implements CataloguePickerPopover.Entries {
        @Override
        public List<String> values() {
            return EnderChestKitOverlay.saveValues();
        }

        @Override
        public String label(final String value) {
            return EnderChestKitOverlay.isNewKit(value) ? EnderChestKitOverlay.NEW_KIT_LABEL : value;
        }

        @Override
        public String itemId(final String value) {
            return ICON_ITEM;
        }

        @Override
        public String trailing(final String value) {
            return EnderChestKitOverlay.isNewKit(value)
                ? EnderChestKitOverlay.nextKitName()
                : EnderChestKitOverlay.itemCount(EnderChests.filledCount(ClientKitsConfig.enderKit(value)));
        }
    }
}
