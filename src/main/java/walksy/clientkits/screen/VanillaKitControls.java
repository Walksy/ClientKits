package walksy.clientkits.screen;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import main.walksy.lib.core.platform.WLInput;
import main.walksy.lib.core.render.WLGraphics2D;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ChestMenu;
import walksy.clientkits.config.ClientKitsConfig;
import walksy.clientkits.kit.EnderChests;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

final class VanillaKitControls implements KitControls {
    private static final int BUTTON_HEIGHT = 20;
    private static final int LABEL_PADDING = 10;

    private static final int TOGGLE_SIZE = 12;
    private static final int TOGGLE_INSET_X = 6;
    private static final int TOGGLE_INSET_Y = 4;

    private static final String TOGGLE_CLOSED = "»";
    private static final String TOGGLE_OPEN = "«";
    private static final int CHEST_GAP = 4;
    private static final int STACK_GAP = 6;
    private static final int MARGIN = 4;

    private static final int PANEL_PADDING = 4;
    private static final int ROW_GAP = 2;
    private static final int SCROLLBAR_WIDTH = 4;

    private static final int PANEL_FILL = 0xF0100010;
    private static final int PANEL_BORDER_TOP = 0x505000FF;
    private static final int PANEL_BORDER_BOTTOM = 0x5028007F;
    private static final int TITLE_COLOR = 0xFFFFFFFF;
    private static final int EMPTY_COLOR = 0xFFA0A0A0;
    private static final int SCROLL_TRACK = 0xFF000000;
    private static final int SCROLL_THUMB = 0xFF808080;

    private final Screen screen;
    private final ChestMenu menu;
    private final Button toggleButton;
    private final Button loadButton;
    private final Button saveButton;

    private boolean expanded;

    private Dropdown dropdown;

    VanillaKitControls(final Screen screen, final ChestMenu menu) {
        this.screen = screen;
        this.menu = menu;
        this.toggleButton = Button.builder(Component.literal(TOGGLE_CLOSED),
                ignored -> this.setExpanded(!this.expanded))
            .tooltip(Tooltip.create(Component.literal("Ender chest kits")))
            .build();
        this.loadButton = Button.builder(Component.literal(EnderChestKitOverlay.LOAD_LABEL),
            ignored -> this.openLoad()).build();
        this.saveButton = Button.builder(Component.literal(EnderChestKitOverlay.SAVE_LABEL),
            ignored -> this.openSave()).build();
        this.layoutButtons();
    }

    private void setExpanded(final boolean expanded) {
        this.expanded = expanded;
        this.toggleButton.setMessage(Component.literal(expanded ? TOGGLE_OPEN : TOGGLE_CLOSED));
        if (!expanded) {
            this.dropdown = null;
        }
    }

    private static Font font() {
        return Minecraft.getInstance().font;
    }

    private List<Button> buttons() {
        return List.of(this.loadButton, this.saveButton);
    }

    private void layoutButtons() {
        final EnderChestKitOverlay.ChestRect chest =
            EnderChestKitOverlay.chestRect(this.screen, this.menu);
        this.toggleButton.setRectangle(TOGGLE_SIZE, TOGGLE_SIZE,
            chest.right() - TOGGLE_INSET_X - TOGGLE_SIZE, chest.top() + TOGGLE_INSET_Y);

        final int width = Math.max(widthFor(EnderChestKitOverlay.LOAD_LABEL),
            widthFor(EnderChestKitOverlay.SAVE_LABEL));
        final EnderChestKitOverlay.Placement placement = EnderChestKitOverlay.place(
            this.screen, this.menu, width, BUTTON_HEIGHT, CHEST_GAP, STACK_GAP, MARGIN, 0);

        this.loadButton.setRectangle(width, BUTTON_HEIGHT, placement.loadX(), placement.loadY());
        this.saveButton.setRectangle(width, BUTTON_HEIGHT, placement.saveX(), placement.saveY());
    }

    private static int widthFor(final String label) {
        return font().width(label) + LABEL_PADDING * 2;
    }

    @Override
    public void extract(final WLGraphics2D graphics, final int mouseX, final int mouseY, final float delta) {
        this.layoutButtons();
        graphics.nextStratum();

        final boolean open = this.dropdown != null;
        final int hoverX = open ? -1 : mouseX;
        final int hoverY = open ? -1 : mouseY;
        graphics.widget(this.toggleButton, hoverX, hoverY, delta);
        if (this.expanded) {
            for (final Button button : this.buttons()) {
                graphics.widget(button, hoverX, hoverY, delta);
            }
        }
        if (open) {
            this.dropdown.extract(graphics, mouseX, mouseY, delta);
        }
    }

    @Override
    public boolean capturesPointer(final double mouseX, final double mouseY) {
        return this.dropdown != null && this.dropdown.isOverPanel(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(final MouseButtonEvent event) {
        if (this.dropdown != null) {
            return this.dropdown.mouseClicked(event);
        }
        if (event.button() != InputConstants.MOUSE_BUTTON_LEFT) {
            return false;
        }
        if (this.toggleButton.isMouseOver(event.x(), event.y())) {
            WLInput.mouseClicked(this.toggleButton, event, false);
            return true;
        }
        if (!this.expanded) {
            return false;
        }
        for (final Button button : this.buttons()) {
            if (button.isMouseOver(event.x(), event.y())) {
                WLInput.mouseClicked(button, event, false);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseReleased(final MouseButtonEvent event) {
        return this.dropdown != null;
    }

    @Override
    public boolean mouseDragged(final MouseButtonEvent event, final double dragX, final double dragY) {
        return this.dropdown != null;
    }

    @Override
    public boolean mouseScrolled(final double mouseX, final double mouseY,
                                 final double scrollX, final double scrollY) {
        if (this.dropdown == null) {
            return false;
        }
        this.dropdown.scrollBy(scrollY);
        return true;
    }

    @Override
    public boolean keyPressed(final KeyEvent event) {
        if (event.key() != InputConstants.KEY_ESCAPE) {
            return false;
        }
        if (this.dropdown != null) {
            this.dropdown = null;
            return true;
        }
        if (this.expanded) {
            this.setExpanded(false);
            return true;
        }
        return false;
    }

    @Override
    public void retire() {
        this.dropdown = null;
    }

    private void openLoad() {
        final List<Row> rows = new ArrayList<>();
        for (final String name : ClientKitsConfig.enderKitNames()) {
            rows.add(new Row(name, name + " ("
                + EnderChestKitOverlay.itemCount(EnderChests.filledCount(ClientKitsConfig.enderKit(name))) + ")"));
        }
        this.dropdown = new Dropdown(this.loadButton, EnderChestKitOverlay.LOAD_TITLE, rows,
            EnderChestKitOverlay::load);
    }

    private void openSave() {
        final List<Row> rows = new ArrayList<>();
        for (final String value : EnderChestKitOverlay.saveValues()) {
            rows.add(EnderChestKitOverlay.isNewKit(value)
                ? new Row(value, EnderChestKitOverlay.NEW_KIT_LABEL + " (" + EnderChestKitOverlay.nextKitName() + ")")
                : new Row(value, value + " ("
                    + EnderChestKitOverlay.itemCount(EnderChests.filledCount(ClientKitsConfig.enderKit(value))) + ")"));
        }
        this.dropdown = new Dropdown(this.saveButton, EnderChestKitOverlay.SAVE_LABEL, rows,
            EnderChestKitOverlay::save);
    }

    private record Row(String value, String label) {
    }

    private final class Dropdown {
        private final Button trigger;
        private final String title;
        private final List<Row> rows;
        private final List<Button> rowButtons = new ArrayList<>();

        private int x;
        private int y;
        private int width;
        private int height;
        private int visibleRows;
        private int scroll;

        private Dropdown(final Button trigger, final String title, final List<Row> rows,
                         final Consumer<String> onPick) {
            this.trigger = trigger;
            this.title = title;
            this.rows = rows;
            for (final Row row : rows) {
                this.rowButtons.add(Button.builder(Component.literal(row.label()), ignored -> {
                    VanillaKitControls.this.dropdown = null;
                    onPick.accept(row.value());
                }).build());
            }
            this.layout();
        }

        private int titleHeight() {
            return font().lineHeight + PANEL_PADDING;
        }

        private boolean scrollable() {
            return this.visibleRows < this.rows.size();
        }

        private void layout() {
            final Font font = font();
            final int contentTop = this.titleHeight();

            final int roomForRows = VanillaKitControls.this.screen.height - MARGIN * 2 - PANEL_PADDING * 2 - contentTop;
            final int perRow = BUTTON_HEIGHT + ROW_GAP;
            this.visibleRows = Math.max(1, Math.min(this.rows.size(), (roomForRows + ROW_GAP) / perRow));
            this.scroll = Math.max(0, Math.min(this.scroll, this.rows.size() - this.visibleRows));

            int rowWidth = Math.max(this.trigger.getWidth(), widthFor(this.title));
            for (final Row row : this.rows) {
                rowWidth = Math.max(rowWidth, widthFor(row.label()));
            }
            if (this.rows.isEmpty()) {
                rowWidth = Math.max(rowWidth, widthFor(EnderChestKitOverlay.EMPTY_MESSAGE));
            }
            final int gutter = this.scrollable() ? SCROLLBAR_WIDTH + ROW_GAP : 0;
            this.width = Math.min(rowWidth + gutter + PANEL_PADDING * 2, VanillaKitControls.this.screen.width - MARGIN * 2);

            final int contentHeight = this.rows.isEmpty()
                ? font.lineHeight
                : this.visibleRows * perRow - ROW_GAP;
            this.height = contentTop + contentHeight + PANEL_PADDING * 2;

            int panelX = this.trigger.getRight() + CHEST_GAP;
            if (panelX + this.width > VanillaKitControls.this.screen.width - MARGIN) {
                panelX = this.trigger.getX() - CHEST_GAP - this.width;
            }
            this.x = clamp(panelX, MARGIN, VanillaKitControls.this.screen.width - MARGIN - this.width);
            this.y = clamp(this.trigger.getY(), MARGIN, VanillaKitControls.this.screen.height - MARGIN - this.height);

            final int buttonWidth = this.width - gutter - PANEL_PADDING * 2;
            for (int index = 0; index < this.rowButtons.size(); index++) {
                final Button button = this.rowButtons.get(index);
                final int slot = index - this.scroll;
                button.visible = slot >= 0 && slot < this.visibleRows;
                if (button.visible) {
                    button.setRectangle(buttonWidth, BUTTON_HEIGHT,
                        this.x + PANEL_PADDING, this.rowsTop() + slot * perRow);
                }
            }
        }

        private int rowsTop() {
            return this.y + PANEL_PADDING + this.titleHeight();
        }

        private void scrollBy(final double scrollY) {
            if (this.scrollable()) {
                this.scroll = clamp(this.scroll - (int) Math.signum(scrollY), 0,
                    this.rows.size() - this.visibleRows);
            }
        }

        private boolean isOverPanel(final double mouseX, final double mouseY) {
            return mouseX >= this.x && mouseX < this.x + this.width
                && mouseY >= this.y && mouseY < this.y + this.height;
        }

        private boolean mouseClicked(final MouseButtonEvent event) {
            if (event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
                for (final Button button : this.rowButtons) {
                    if (button.visible && button.isMouseOver(event.x(), event.y())
                        && WLInput.mouseClicked(button, event, false)) {
                        return true;
                    }
                }
            }
            VanillaKitControls.this.dropdown = null;
            return true;
        }

        private void extract(final WLGraphics2D graphics, final int mouseX, final int mouseY,
                          final float delta) {
            this.layout();

            final int right = this.x + this.width;
            final int bottom = this.y + this.height;
            graphics.fill(this.x, this.y, right, bottom, PANEL_FILL, null);
            graphics.fill(this.x, this.y, right, this.y + 1, PANEL_BORDER_TOP, null);
            graphics.fill(this.x, bottom - 1, right, bottom, PANEL_BORDER_BOTTOM, null);
            graphics.fill(this.x, this.y + 1, this.x + 1, bottom - 1, PANEL_BORDER_TOP, PANEL_BORDER_BOTTOM);
            graphics.fill(right - 1, this.y + 1, right, bottom - 1, PANEL_BORDER_TOP, PANEL_BORDER_BOTTOM);

            final Font font = font();
            centeredText(graphics, font, this.title, this.x + this.width / 2, this.y + PANEL_PADDING, TITLE_COLOR);

            if (this.rows.isEmpty()) {
                centeredText(graphics, font, EnderChestKitOverlay.EMPTY_MESSAGE, this.x + this.width / 2, this.rowsTop(), EMPTY_COLOR);
                return;
            }
            for (final Button button : this.rowButtons) {
                if (button.visible) {
                    graphics.widget(button, mouseX, mouseY, delta);
                }
            }
            this.extractScrollbar(graphics);
        }

        private void extractScrollbar(final WLGraphics2D graphics) {
            if (!this.scrollable()) {
                return;
            }
            final int trackLeft = this.x + this.width - PANEL_PADDING - SCROLLBAR_WIDTH;
            final int trackRight = trackLeft + SCROLLBAR_WIDTH;
            final int trackTop = this.rowsTop();
            final int trackBottom = trackTop + this.visibleRows * (BUTTON_HEIGHT + ROW_GAP) - ROW_GAP;
            final int track = trackBottom - trackTop;

            final int thumb = Math.max(BUTTON_HEIGHT / 2, track * this.visibleRows / this.rows.size());
            final int thumbTop = trackTop
                + (track - thumb) * this.scroll / (this.rows.size() - this.visibleRows);

            graphics.fill(trackLeft, trackTop, trackRight, trackBottom, SCROLL_TRACK, null);
            graphics.fill(trackLeft, thumbTop, trackRight, thumbTop + thumb, SCROLL_THUMB, null);
        }
    }

    private static void centeredText(final WLGraphics2D graphics, final Font font, final String text, final int centerX, final int y, final int color) {
        graphics.text(font, text, centerX - font.width(text) / 2, y, color);
    }

    private static int clamp(final int value, final int min, final int max) {
        return Math.max(min, Math.min(value, Math.max(min, max)));
    }
}
