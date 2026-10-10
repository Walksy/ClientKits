package walksy.clientkits.screen;

import main.walksy.lib.core.WalksyLib;
import main.walksy.lib.core.render.WLGraphics2D;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.inventory.ChestMenu;
import walksy.clientkits.config.ClientKitsConfig;
import walksy.clientkits.kit.EnderChests;
import walksy.clientkits.manager.KitManager;

import java.util.ArrayList;
import java.util.List;

public final class EnderChestKitOverlay {
    static final String LOAD_LABEL = "Load Ender Chest";
    static final String SAVE_LABEL = "Save Ender Chest";
    static final String LOAD_TITLE = "Ender Chest Kits";
    static final String EMPTY_MESSAGE = "No ender chest kits yet";
    static final String NEW_KIT_LABEL = "New Kit";

    private static final String NEW_KIT = " new";

    private static final String NEW_KIT_STEM = "EnderKit";

    private static final int IMAGE_WIDTH = 176;
    private static final int IMAGE_ROWS_TO_HEIGHT = 114;

    private static EnderChestKitOverlay active;

    private final Screen screen;
    private final KitControls controls;

    private EnderChestKitOverlay(final Screen screen, final ChestMenu menu) {
        this.screen = screen;
        this.controls = ClientKitsConfig.glassEnderButtons()
            ? new GlassKitControls(screen, menu)
            : new VanillaKitControls(screen, menu);
    }

    public static void onInit(final Screen screen) {
        final ChestMenu menu = EnderChests.menuOf(screen);
        retire();
        if (menu != null) {
            active = new EnderChestKitOverlay(screen, menu);
        }
    }

    public static void onRemoved(final Screen screen) {
        if (active != null && active.screen == screen) {
            retire();
        }
    }

    private static void retire() {
        if (active == null) {
            return;
        }
        active.controls.retire();
        active = null;
    }

    private static EnderChestKitOverlay of(final Screen screen) {
        return active != null && active.screen == screen ? active : null;
    }

    public static void extract(final Screen screen, final WLGraphics2D graphics,
                               final int mouseX, final int mouseY, final float delta) {
        final EnderChestKitOverlay overlay = of(screen);
        if (overlay != null) {
            overlay.controls.extract(graphics, mouseX, mouseY, delta);
        }
    }

    public static boolean capturesPointer(final Screen screen, final double mouseX, final double mouseY) {
        final EnderChestKitOverlay overlay = of(screen);
        return overlay != null && overlay.controls.capturesPointer(mouseX, mouseY);
    }

    public static boolean mouseClicked(final Screen screen, final MouseButtonEvent event) {
        final EnderChestKitOverlay overlay = of(screen);
        return overlay != null && overlay.controls.mouseClicked(event);
    }

    public static boolean mouseReleased(final Screen screen, final MouseButtonEvent event) {
        final EnderChestKitOverlay overlay = of(screen);
        return overlay != null && overlay.controls.mouseReleased(event);
    }

    public static boolean mouseDragged(final Screen screen, final MouseButtonEvent event,
                                       final double dragX, final double dragY) {
        final EnderChestKitOverlay overlay = of(screen);
        return overlay != null && overlay.controls.mouseDragged(event, dragX, dragY);
    }

    public static boolean mouseScrolled(final Screen screen, final double mouseX, final double mouseY,
                                        final double scrollX, final double scrollY) {
        final EnderChestKitOverlay overlay = of(screen);
        return overlay != null && overlay.controls.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    public static boolean keyPressed(final Screen screen, final KeyEvent event) {
        final EnderChestKitOverlay overlay = of(screen);
        return overlay != null && overlay.controls.keyPressed(event);
    }

    public static boolean charTyped(final Screen screen, final CharacterEvent event) {
        final EnderChestKitOverlay overlay = of(screen);
        return overlay != null && overlay.controls.charTyped(event);
    }

    record Placement(int loadX, int loadY, int saveX, int saveY) {
    }

    record ChestRect(int left, int top, int right, int bottom) {
    }

    static ChestRect chestRect(final Screen screen, final ChestMenu menu) {
        final int height = IMAGE_ROWS_TO_HEIGHT + menu.getRowCount() * 18;
        final int left = (screen.width - IMAGE_WIDTH) / 2;
        final int top = (screen.height - height) / 2;
        return new ChestRect(left, top, left + IMAGE_WIDTH, top + height);
    }

    static Placement place(final Screen screen, final ChestMenu menu, final int width, final int height,
                           final int gap, final int stackGap, final int margin, final int topInset) {
        final ChestRect chest = chestRect(screen, menu);

        final int gutterX = chest.right() + gap;
        if (gutterX + width <= screen.width - margin) {
            final int x = clampX(screen, gutterX, width, margin);
            final int top = chest.top() + topInset;
            return new Placement(x, clampY(screen, top, height, margin),
                x, clampY(screen, top + height + stackGap, height, margin));
        }

        final int rowX = chest.right() - (width * 2 + stackGap);
        final int rowY = clampY(screen, chest.top() - gap - height, height, margin);
        return new Placement(clampX(screen, rowX, width, margin), rowY,
            clampX(screen, rowX + width + stackGap, width, margin), rowY);
    }

    private static int clampX(final Screen screen, final int x, final int width, final int margin) {
        return Math.max(margin, Math.min(x, screen.width - margin - width));
    }

    private static int clampY(final Screen screen, final int y, final int height, final int margin) {
        return Math.max(margin, Math.min(y, screen.height - margin - height));
    }

    static boolean isNewKit(final String value) {
        return NEW_KIT.equals(value);
    }

    static String nextKitName() {
        return ClientKitsConfig.freeEnderKitName(NEW_KIT_STEM);
    }

    static List<String> saveValues() {
        final List<String> values = new ArrayList<>();
        values.add(NEW_KIT);
        values.addAll(ClientKitsConfig.enderKitNames());
        return values;
    }

    static String itemCount(final int filled) {
        return filled + (filled == 1 ? " item" : " items");
    }

    static void load(final String name) {
        final String error = KitManager.loadEnderKit(name);
        if (error != null) {
            WalksyLib.getInstance().getToastManager().error("Client Kits", error);
        }
    }

    static void save(final String choice) {
        final boolean fresh = isNewKit(choice);
        final String name = fresh ? nextKitName() : choice;
        final String error = fresh ? KitManager.saveEnderKit(name) : KitManager.replaceEnderKit(name);
        if (error != null) {
            WalksyLib.getInstance().getToastManager().error("Client Kits", error);
            return;
        }
        WalksyLib.getInstance().getToastManager().success("Client Kits",
            (fresh ? "Saved ender chest kit: " : "Saved over ender chest kit: ") + name);
    }
}
