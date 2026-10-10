package walksy.clientkits.kit;

import main.walksy.lib.api.config.value.ItemInventory;
import main.walksy.lib.api.config.value.ItemSpec;
import main.walksy.lib.core.platform.WLClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;

public final class EnderChests {
    public static final int SLOT_COUNT = 27;
    private static final String TITLE_KEY = "container.enderchest";
    private static final String TITLE_EN = "Ender Chest";

    private EnderChests() {
    }

    public static int kitSlot(final int enderSlot) {
        return ItemInventory.STORAGE_START + enderSlot;
    }

    public static boolean isEnderChestScreen(final Screen screen) {
        return menuOf(screen) != null;
    }

    public static ChestMenu menuOf(final Screen screen) {
        if (!(screen instanceof final AbstractContainerScreen<?> container)
            || !(container.getMenu() instanceof final ChestMenu chest)
            || chest.getContainer().getContainerSize() != SLOT_COUNT
            || !isEnderChestTitle(screen.getTitle())) {
            return null;
        }
        return chest;
    }

    private static boolean isEnderChestTitle(final Component title) {
        if (title.getContents() instanceof final TranslatableContents translatable
            && TITLE_KEY.equals(translatable.getKey())) {
            return true;
        }
        final String plain = title.getString();
        return plain.equals(Component.translatable(TITLE_KEY).getString()) || plain.equals(TITLE_EN);
    }

    public static ChestMenu openMenu() {
        final Minecraft client = Minecraft.getInstance();
        final Player player = client.player;
        if (player == null || !isEnderChestScreen(WLClient.screen())) {
            return null;
        }
        return player.containerMenu instanceof final ChestMenu menu
            && menu.getContainer().getContainerSize() == SLOT_COUNT ? menu : null;
    }

    public static ItemInventory capture(final ChestMenu menu) {
        final ItemInventory captured = new ItemInventory();
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            captured.set(kitSlot(slot), ItemSpec.from(menu.getSlot(slot).getItem()));
        }
        return captured;
    }

    public static ItemStack stackAt(final ItemInventory kit, final int enderSlot) {
        return kit.stackAt(kitSlot(enderSlot));
    }

    public static int filledCount(final ChestMenu menu) {
        int filled = 0;
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            if (!menu.getSlot(slot).getItem().isEmpty()) {
                filled++;
            }
        }
        return filled;
    }

    public static int filledCount(final ItemInventory kit) {
        int filled = 0;
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            if (kit.get(kitSlot(slot)) != null) {
                filled++;
            }
        }
        return filled;
    }
}
